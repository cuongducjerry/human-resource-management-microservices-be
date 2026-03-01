package com.hrm.employee.service;

import com.hrm.employee.client.AuthClient;
import com.hrm.employee.dto.request.ReqCreateEmployeeDTO;
import com.hrm.employee.dto.request.ReqCreateKeycloakUserDTO;
import com.hrm.employee.dto.request.ReqEmployeeSelfUpdateDTO;
import com.hrm.employee.dto.request.ReqUpdateUserProfileDTO;
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
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ================= CREATE =================
    @Transactional
    public ResCreateEmployeeDTO createEmployee(ReqCreateEmployeeDTO req) {

        String keycloakUserId = null;

        List<String> currentRoles = SecurityUtil.getCurrentUserRoles();
        boolean isSuperAdmin = currentRoles.contains("ROLE_SUPER_ADMIN");
        boolean isHrAdmin = currentRoles.contains("ROLE_HR_ADMIN");

        if (!isSuperAdmin && !isHrAdmin) {
            throw new ForbiddenException("You do not have permission to create employee");
        }

        if (isHrAdmin) {
            if (req.getRoles().contains("ROLE_HR_ADMIN")
                    || req.getRoles().contains("ROLE_SUPER_ADMIN")
                    || req.getRoles().contains("ROLE_ACCOUNTANT")) {
                throw new ForbiddenException("HR_ADMIN cannot create this role");
            }
        }

        try {

            // ===== Split name =====
            String[] parts = req.getFullName().trim().split("\\s+");
            String firstName = parts[parts.length - 1];
            String lastName = parts.length > 1
                    ? String.join(" ", Arrays.copyOfRange(parts, 0, parts.length - 1))
                    : parts[0];

            // ===== Validate status =====
            EmployeeStatus status = req.getStatus() != null
                    ? req.getStatus()
                    : EmployeeStatus.PROBATION;

            if (!isSuperAdmin && status != EmployeeStatus.PROBATION) {
                throw new BadRequestException("HR_ADMIN can only create PROBATION employee");
            }

            if (status == EmployeeStatus.RESIGNED
                    || status == EmployeeStatus.TERMINATED) {
                throw new BadRequestException("Cannot create employee with final status");
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

            // ===== Sync Keycloak based on status =====
            if (status == EmployeeStatus.PROBATION) {
                authClient.disableUser(keycloakUserId);
            } else if (status == EmployeeStatus.ACTIVE) {
                authClient.enableUser(keycloakUserId);
            }

            // ===== Generate employee code =====
            UUID id = UUID.randomUUID();
            String employeeCode = generateEmployeeCode(id);

            // ===== Save employee =====
            Employee employee = Employee.builder()
                    .keycloakUserId(keycloakUserId)
                    .employeeCode(employeeCode)
                    .fullName(req.getFullName())
                    .email(req.getEmail())
                    .departmentId(req.getDepartmentId())
                    .positionId(req.getPositionId())
                    .status(status)
                    .active(true)
                    .build();

            employeeRepository.save(employee);

            return employeeMapper.convertToResCreateEmployeeDTO(employee);

        } catch (Exception e) {
            if (keycloakUserId != null) {
                try {
                    authClient.deleteUser(keycloakUserId);
                } catch (Exception ignored) {}
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

    // ================= VIEW DETAIL =================
    public ResEmployeeDTO getEmployeeById(UUID id) {

        Employee employee = getEmployeeOrThrow(id);

        return employeeMapper.convertToResEmployeeDTO(employee);
    }

    // ================= UPDATE STATUS =================
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

        employee.setStatus(newStatus);
        employeeRepository.save(employee);

        // ===== Sync Keycloak =====
        if (newStatus == EmployeeStatus.RESIGNED ||
                newStatus == EmployeeStatus.TERMINATED) {
            authClient.disableUser(employee.getKeycloakUserId());
        }

        if (newStatus == EmployeeStatus.ACTIVE) {
            authClient.enableUser(employee.getKeycloakUserId());
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

        List<String> currentRoles = SecurityUtil.getCurrentUserRoles();
        boolean isSuperAdmin = currentRoles.contains("ROLE_SUPER_ADMIN");

        if (!isSuperAdmin) {
            throw new ForbiddenException("Only SUPER_ADMIN can update roles");
        }

        authClient.updateUserRoles(employee.getKeycloakUserId(), newRoles);
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
