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
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // If it's HR_ADMIN, you shouldn't create HR_ADMIN or SUPER_ADMIN.
        if (isHrAdmin) {
            if (req.getRoles().contains("ROLE_HR_ADMIN")
                    || req.getRoles().contains("ROLE_SUPER_ADMIN")
                    || req.getRoles().contains("ROLE_ACCOUNTANT")) {

                throw new ForbiddenException("HR_ADMIN cannot create this role");
            }
        }

        try {

            // Separate firstName / lastName from fullName
            String fullName = req.getFullName().trim();
            String[] parts = fullName.split("\\s+");

            String firstName;
            String lastName;

            if (parts.length == 1) {
                firstName = parts[0];
                lastName = parts[0];
            } else {
                firstName = parts[parts.length - 1]; // tên
                lastName = String.join(" ",
                        Arrays.copyOfRange(parts, 0, parts.length - 1)); // họ + đệm
            }

            // Create a user on Keycloak
            ReqCreateKeycloakUserDTO userReq = new ReqCreateKeycloakUserDTO();
            userReq.setUsername(req.getEmail());
            userReq.setEmail(req.getEmail());
            userReq.setPassword("123456");
            userReq.setFirstName(firstName);
            userReq.setLastName(lastName);
            userReq.setRoles(req.getRoles());

            keycloakUserId = authClient.createUser(userReq);

            // save db
            Employee employee = new Employee();
            employee.setKeycloakUserId(keycloakUserId);
            employee.setEmployeeCode(generateEmployeeCode());
            employee.setFullName(req.getFullName());
            employee.setEmail(req.getEmail());
            employee.setDepartmentId(req.getDepartmentId());
            employee.setPositionId(req.getPositionId());
            employee.setStatus(EmployeeStatus.ACTIVE);

            employeeRepository.save(employee);

            return employeeMapper.convertToResCreateEmployeeDTO(employee);

        } catch (Exception e) {

            // Rollback user if the database fails.
            if (keycloakUserId != null) {
                try {
                    authClient.deleteUser(keycloakUserId);
                } catch (Exception ex) {

                }
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
    @Transactional
    public ResEmployeeDTO updateStatus(UUID id, EmployeeStatus status) {

        Employee employee = getEmployeeOrThrow(id);

        employee.setStatus(status);

        return employeeMapper.convertToResEmployeeDTO(
                employeeRepository.save(employee));
    }


    // ================= SELF UPDATE =================
    @Transactional
    public ResEmployeeDTO updateSelf(ReqEmployeeSelfUpdateDTO req) {

        String keycloakUserId = SecurityUtil.getCurrentUserId();

        Employee employee = employeeRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new IdInvalidException("Employee not found"));

        String oldFullName = employee.getFullName();

        employee.setFullName(req.getFullName());
        employee.setPhone(req.getPhone());
        employee.setAddress(req.getAddress());
        employee.setDateOfBirth(req.getDateOfBirth());
        employee.setGender(req.getGender());

        if (req.getFullName() != null
                && !req.getFullName().isBlank()
                && !req.getFullName().equals(oldFullName)) {

            String[] parts = req.getFullName().trim().split("\\s+");

            String firstName = parts[parts.length - 1];
            String lastName = parts.length > 1
                    ? String.join(" ",
                    Arrays.copyOfRange(parts, 0, parts.length - 1))
                    : "";

            ReqUpdateUserProfileDTO profileReq = new ReqUpdateUserProfileDTO();
            profileReq.setFirstName(firstName);
            profileReq.setLastName(lastName);

            authClient.updateUserProfile(keycloakUserId, profileReq);
        }

        return employeeMapper.convertToResEmployeeDTO(employee);
    }


    // ================= DELETE =================
    @Transactional
    public void deleteEmployee(UUID id) {

        Employee employee = getEmployeeOrThrow(id);

        List<String> currentUserRoles = SecurityUtil.getCurrentUserRoles();
        List<String> targetUserRoles =
                authClient.getUserRoles(employee.getKeycloakUserId());

        boolean currentIsSuperAdmin =
                currentUserRoles.contains("ROLE_SUPER_ADMIN");

        boolean targetIsSuperAdmin =
                targetUserRoles.contains("ROLE_SUPER_ADMIN");

        // Do not delete SUPER_ADMIN
        if (targetIsSuperAdmin) {
            throw new ForbiddenException("Cannot delete SUPER_ADMIN");
        }

        // HR cannot delete other HRs.
        if (!currentIsSuperAdmin &&
                targetUserRoles.contains("ROLE_HR_ADMIN")) {

            throw new ForbiddenException("You cannot delete HR_ADMIN");
        }

        // Disable bên Keycloak
        authClient.disableUser(employee.getKeycloakUserId());

        // Soft delete
        employeeRepository.delete(employee);
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

    @Transactional
    public ResEmployeeDTO restoreEmployee(UUID id) {

        Employee employee = employeeRepository.findByIdNative(id)
                .orElseThrow(() -> new IdInvalidException("Employee not found"));

        // 1. Update DB
        employee.setActive(true);

        // 2. Enable Keycloak
        authClient.enableUser(employee.getKeycloakUserId());

        return employeeMapper.convertToResEmployeeDTO(employee);
    }

    private Employee getEmployeeOrThrow(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Employee not found"));
    }

    private String generateEmployeeCode() {
        long count = employeeRepository.count() + 1;
        return "EMP" + String.format("%04d", count);
    }
}
