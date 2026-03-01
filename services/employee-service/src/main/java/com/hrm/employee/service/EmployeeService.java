package com.hrm.employee.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.employee.client.AuthClient;
import com.hrm.employee.dto.request.*;
import com.hrm.employee.dto.response.ResCreateEmployeeDTO;
import com.hrm.employee.dto.response.ResEmployeeDTO;
import com.hrm.employee.dto.response.ResultPaginationDTO;
import com.hrm.employee.entity.Employee;
import com.hrm.employee.mapper.EmployeeMapper;
import com.hrm.employee.mapper.PaginationMapper;
import com.hrm.employee.repository.EmployeeRepository;

import com.hrm.employee.util.SecurityUtil;
import com.hrm.employee.util.constant.EmployeeStatus;
import com.hrm.employee.util.error.IdInvalidException;
import com.hrm.employee.util.error.InternalServerException;
import com.hrm.employee.util.error.RequestException;
import feign.FeignException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuthClient authClient;
    private final EmployeeMapper employeeMapper;
    private final PaginationMapper paginationMapper;
    private final CloudinaryService cloudinaryService;

    // ================= CREATE =================
    @Transactional
    public ResCreateEmployeeDTO createEmployee(ReqCreateEmployeeDTO req) {

        String keycloakUserId = null;

        if (employeeRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (req.getRoles() == null || req.getRoles().isEmpty()) {
            throw new BadRequestException("Roles cannot be empty");
        }

        List<String> currentRoles = SecurityUtil.getCurrentUserRoles();
        boolean isSuperAdmin = currentRoles.contains("ROLE_SUPER_ADMIN");
        boolean isHrAdmin = currentRoles.contains("ROLE_HR_ADMIN");

        if (!isSuperAdmin && !isHrAdmin) {
            throw new ForbiddenException("No permission");
        }

        try {

            // ===== Split name =====
            String[] parts = req.getFullName().trim().split("\\s+");
            String firstName = parts[parts.length - 1];
            String lastName = parts.length > 1
                    ? String.join(" ", Arrays.copyOfRange(parts, 0, parts.length - 1))
                    : "";

            EmployeeStatus status = req.getStatus() != null
                    ? req.getStatus()
                    : EmployeeStatus.PROBATION;

            if (status == EmployeeStatus.RESIGNED
                    || status == EmployeeStatus.TERMINATED) {
                throw new BadRequestException("Cannot create final status employee");
            }

            // ===== Create Keycloak =====
            ReqCreateKeycloakUserDTO userReq = new ReqCreateKeycloakUserDTO();
            userReq.setUsername(req.getEmail());
            userReq.setEmail(req.getEmail());
            userReq.setPassword("123456");
            userReq.setFirstName(firstName);
            userReq.setLastName(lastName);
            userReq.setRoles(req.getRoles());

            keycloakUserId = authClient.createUser(userReq);

            // ===== Sync Keycloak =====
            if (status == EmployeeStatus.ACTIVE
                    || status == EmployeeStatus.ON_LEAVE) {
                authClient.enableUser(keycloakUserId);
            } else {
                authClient.disableUser(keycloakUserId);
            }

            UUID id = UUID.randomUUID();
            String employeeCode = generateEmployeeCode(id);

            Employee employee = Employee.builder()
                    .keycloakUserId(keycloakUserId)
                    .employeeCode(employeeCode)
                    .fullName(req.getFullName())
                    .email(req.getEmail())
                    .organizationId(req.getOrganizationId())
                    .positionId(req.getPositionId())
                    .managerId(req.getManagerId())
                    .hireDate(LocalDate.now())
                    .status(status)
                    .active(true)
                    .build();

            // ===== Probation auto set =====
            if (status == EmployeeStatus.PROBATION) {
                employee.setProbationEndDate(LocalDate.now().plusMonths(2));
            }

            employeeRepository.save(employee);

            return employeeMapper.convertToResCreateEmployeeDTO(employee);

        } catch (Exception e) {
            if (keycloakUserId != null) {
                authClient.deleteUser(keycloakUserId);
            }
            throw e;
        }
    }

    // ================= LIST (PAGINATION) =================
    public ResultPaginationDTO handleListEmployee(
            Specification<Employee> spec,
            Pageable pageable) {

        Page<Employee> page = employeeRepository.findAll(spec, pageable);

        int pageNumber = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        int totalPages = page.getTotalPages();
        long totalElements = page.getTotalElements();

        List<ResEmployeeDTO> list = page.getContent()
                .stream()
                .map(employeeMapper::convertToResEmployeeDTO)
                .collect(Collectors.toList());

        return paginationMapper.convertToResultPaginationDTO(
                pageNumber, pageSize, totalPages, totalElements, list);
    }

//    public void changePassword(ReqChangePasswordDTO req) {
//
//        String keycloakUserId = SecurityUtil.getCurrentUserId();
//
//        try {
//            authClient.changePassword(keycloakUserId, req);
//
//        } catch (FeignException.BadRequest e) {
//
//            throw new RequestException("Current password is incorrect");
//
//        } catch (FeignException.Forbidden e) {
//
//            throw new ForbiddenException("Access denied from auth service");
//
//        } catch (FeignException.NotFound e) {
//
//            throw new IdInvalidException("User not found in auth service");
//
//        } catch (FeignException e) {
//
//            throw new InternalServerException("Auth service is unavailable");
//        }
//    }

    public void changePassword(ReqChangePasswordDTO req) {
        String keycloakUserId = SecurityUtil.getCurrentUserId();
        authClient.changePassword(keycloakUserId, req);
    }

    // ================= VIEW DETAIL =================
    public ResEmployeeDTO getEmployeeById(UUID id) {

        Employee employee = getEmployeeOrThrow(id);

        return employeeMapper.convertToResEmployeeDTO(employee);
    }

    // ================= UPDATE STATUS =================
    @Transactional
    public ResEmployeeDTO updateStatus(UUID id, EmployeeStatus newStatus) {

        Employee employee = getEmployeeOrThrow(id);

        String currentUserId = SecurityUtil.getCurrentUserId();
        List<String> currentUserRoles = SecurityUtil.getCurrentUserRoles();

        boolean isSuperAdmin = currentUserRoles.contains("ROLE_SUPER_ADMIN");
        boolean isHrAdmin = currentUserRoles.contains("ROLE_HR_ADMIN");

        if (!isSuperAdmin && !isHrAdmin) {
            throw new ForbiddenException("You do not have permission");
        }

        if (employee.getKeycloakUserId().equals(currentUserId)) {
            throw new ForbiddenException("Cannot update your own status");
        }

        List<String> targetRoles =
                authClient.getUserRoles(employee.getKeycloakUserId());

        if (!isSuperAdmin && targetRoles.contains("ROLE_SUPER_ADMIN")) {
            throw new ForbiddenException("Cannot modify SUPER_ADMIN");
        }

        validateStatusTransition(employee.getStatus(), newStatus);

        // ===== Lifecycle Handling =====
        LocalDate today = LocalDate.now();

        switch (newStatus) {

            case ACTIVE -> {
                // If the status changes from PROBATION to ACTIVE -> it is considered confirmed.
                if (employee.getProbationEndDate() == null) {
                    employee.setProbationEndDate(today);
                }
            }

            case TERMINATED -> {
                employee.setTerminationDate(today);
                if (employee.getTerminationReason() == null) {
                    employee.setTerminationReason("Company terminated contract");
                }
            }

            case RESIGNED -> {
                employee.setTerminationDate(today);
                if (employee.getTerminationReason() == null) {
                    employee.setTerminationReason("Employee resigned");
                }
            }

            default -> {
                // PROBATION, ON_LEAVE
            }
        }

        employee.setStatus(newStatus);
        employeeRepository.save(employee);

        // ===== Sync Keycloak =====
        if (newStatus == EmployeeStatus.PROBATION
                || newStatus == EmployeeStatus.ACTIVE
                || newStatus == EmployeeStatus.ON_LEAVE) {

            authClient.enableUser(employee.getKeycloakUserId());

        } else {
            // TERMINATED or RESIGNED
            authClient.disableUser(employee.getKeycloakUserId());
        }

        return employeeMapper.convertToResEmployeeDTO(employee);
    }


    // ================= SELF UPDATE =================
    @Transactional
    public ResEmployeeDTO updateSelf(ReqEmployeeSelfUpdateDTO req) {

        String keycloakUserId = SecurityUtil.getCurrentUserId();

        Employee employee = employeeRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new IdInvalidException("Employee not found"));

        if (req.getFullName() != null) {
            String oldFullName = employee.getFullName();
            employee.setFullName(req.getFullName());

            if (!req.getFullName().equals(oldFullName)) {
                String[] parts = req.getFullName().trim().split("\\s+");
                String firstName = parts[parts.length - 1];
                String lastName = parts.length > 1
                        ? String.join(" ", Arrays.copyOfRange(parts, 0, parts.length - 1))
                        : "";

                ReqUpdateUserProfileDTO profileReq = new ReqUpdateUserProfileDTO();
                profileReq.setFirstName(firstName);
                profileReq.setLastName(lastName);
                authClient.updateUserProfile(keycloakUserId, profileReq);
            }
        }

        if (req.getPhone() != null) employee.setPhone(req.getPhone());
        if (req.getAddress() != null) employee.setAddress(req.getAddress());
        if (req.getDateOfBirth() != null) employee.setDateOfBirth(req.getDateOfBirth());
        if (req.getGender() != null) employee.setGender(req.getGender());

        return employeeMapper.convertToResEmployeeDTO(employee);
    }


    // ================= DELETE =================
    @Transactional
    public void deleteEmployee(UUID id) {

        Employee employee = getEmployeeOrThrow(id);

        List<String> currentRoles = SecurityUtil.getCurrentUserRoles();
        List<String> targetRoles =
                authClient.getUserRoles(employee.getKeycloakUserId());

        boolean currentIsSuperAdmin = currentRoles.contains("ROLE_SUPER_ADMIN");
        boolean targetIsSuperAdmin = targetRoles.contains("ROLE_SUPER_ADMIN");

        if (targetIsSuperAdmin) {
            throw new ForbiddenException("Cannot delete SUPER_ADMIN");
        }

        if (!currentIsSuperAdmin && targetRoles.contains("ROLE_HR_ADMIN")) {
            throw new ForbiddenException("Cannot delete HR_ADMIN");
        }

        authClient.disableUser(employee.getKeycloakUserId());
        employeeRepository.delete(employee);
    }

    // ================= UPDATE ROLES =================
    @Transactional
    public void updateRoles(UUID id, List<String> newRoles) {

        Employee employee = getEmployeeOrThrow(id);

        String currentUserId = SecurityUtil.getCurrentUserId();

        if (employee.getKeycloakUserId().equals(currentUserId)) {
            throw new ForbiddenException("Cannot update your own roles");
        }

        List<String> currentRoles = SecurityUtil.getCurrentUserRoles();
        boolean isSuperAdmin = currentRoles.contains("ROLE_SUPER_ADMIN");

        if (!isSuperAdmin) {
            throw new ForbiddenException("Only SUPER_ADMIN can update roles");
        }

        if (newRoles.contains("ROLE_SUPER_ADMIN")) {
            throw new ForbiddenException("Cannot assign SUPER_ADMIN role");
        }

        authClient.updateUserRoles(employee.getKeycloakUserId(), newRoles);
    }

    @Transactional
    public ResEmployeeDTO updateAvatar(MultipartFile file) {

        String keycloakUserId = SecurityUtil.getCurrentUserId();

        Employee employee = employeeRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new IdInvalidException("Employee not found"));

        // Delete old photos if any
        if (employee.getAvatarUrl() != null && !employee.getAvatarUrl().isBlank()) {
            cloudinaryService.deleteAvatar(employee.getAvatarUrl());
        }

        // Upload new photos
        String avatarUrl = cloudinaryService.uploadAvatar(file);

        // Update DB
        employee.setAvatarUrl(avatarUrl);

        return employeeMapper.convertToResEmployeeDTO(employee);
    }

    @Transactional
    public ResEmployeeDTO confirmEmployee(UUID id) {

        Employee employee = getEmployeeOrThrow(id);

        if (employee.getStatus() != EmployeeStatus.PROBATION) {
            throw new BadRequestException("Only PROBATION can confirm");
        }

        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setConfirmedDate(LocalDate.now());

        authClient.enableUser(employee.getKeycloakUserId());

        return employeeMapper.convertToResEmployeeDTO(employee);
    }

    // ================= VALIDATE STATUS =================
    private void validateStatusTransition(EmployeeStatus current,
                                          EmployeeStatus target) {

        if (current == target) return;

        switch (current) {

            case PROBATION -> {
                if (target != EmployeeStatus.ACTIVE &&
                        target != EmployeeStatus.TERMINATED) {
                    throw new BadRequestException("Invalid status transition");
                }
            }

            case ACTIVE -> {
                if (target != EmployeeStatus.ON_LEAVE &&
                        target != EmployeeStatus.RESIGNED &&
                        target != EmployeeStatus.TERMINATED) {
                    throw new BadRequestException("Invalid status transition");
                }
            }

            case ON_LEAVE -> {
                if (target != EmployeeStatus.ACTIVE &&
                        target != EmployeeStatus.RESIGNED) {
                    throw new BadRequestException("Invalid status transition");
                }
            }

            case RESIGNED, TERMINATED ->
                    throw new BadRequestException("Cannot change final status");
        }
    }

    public ResultPaginationDTO listDeleted(Pageable pageable) {

        Page<Employee> page = employeeRepository.findDeletedEmployees(pageable);

        return paginationMapper.convertToResultPaginationDTO(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getContent()
                        .stream()
                        .map(employeeMapper::convertToResEmployeeDTO)
                        .toList()
        );
    }

    // ================= RESTORE =================
    @Transactional
    public ResEmployeeDTO restoreEmployee(UUID id) {

        Employee employee = employeeRepository.findByIdNative(id)
                .orElseThrow(() -> new IdInvalidException("Employee not found"));

        if (employee.getStatus() == EmployeeStatus.RESIGNED
                || employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new BadRequestException("Cannot restore final status employee");
        }

        List<String> targetRoles =
                authClient.getUserRoles(employee.getKeycloakUserId());

        if (targetRoles.contains("ROLE_SUPER_ADMIN")) {
            throw new ForbiddenException("Cannot restore SUPER_ADMIN");
        }

        employee.setActive(true);
        authClient.enableUser(employee.getKeycloakUserId());

        return employeeMapper.convertToResEmployeeDTO(employee);
    }

    private Employee getEmployeeOrThrow(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Employee not found"));
    }

    private String generateEmployeeCode(UUID id) {

        int year = LocalDate.now().getYear();

        // Extract 6 digits from the UUID to ensure uniqueness.
        long numeric = Math.abs(id.getMostSignificantBits());
        long sequence = numeric % 1_000_000; // 6 digits

        return String.format("EMP-%d-%06d", year, sequence);
    }
}
