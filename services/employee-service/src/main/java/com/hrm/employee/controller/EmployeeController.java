package com.hrm.employee.controller;

import com.hrm.employee.dto.request.ReqAdminUpdateStatusDTO;
import com.hrm.employee.dto.request.ReqCreateEmployeeDTO;
import com.hrm.employee.dto.request.ReqEmployeeSelfUpdateDTO;
import com.hrm.employee.dto.request.ReqUpdateEmployeeRolesDTO;
import com.hrm.employee.dto.response.ResCreateEmployeeDTO;
import com.hrm.employee.dto.response.ResEmployeeDTO;
import com.hrm.employee.dto.response.ResultPaginationDTO;
import com.hrm.employee.entity.Employee;
import com.hrm.employee.service.EmployeeService;

import com.hrm.employee.specification.EmployeeSpecification;
import com.hrm.employee.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // ================= CREATE =================
    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    @ApiMessage("Create employee")
    public ResponseEntity<ResCreateEmployeeDTO> createEmployee(
            @Valid @RequestBody ReqCreateEmployeeDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(request));
    }

    // ================= LIST =================
    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_LIST')")
    @ApiMessage("Fetch all employee")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {

        Specification<Employee> spec = EmployeeSpecification.keyword(keyword);

        return ResponseEntity.status(HttpStatus.OK).body(
                employeeService.handleListEmployee(spec, pageable));
    }

    // ================= VIEW DETAIL =================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_VIEW')")
    @ApiMessage("Fetch employee by id")
    public ResponseEntity<ResEmployeeDTO> getById(@PathVariable UUID id) {

        return ResponseEntity.status(HttpStatus.OK).body(employeeService.getEmployeeById(id));
    }

    // ================= UPDATE STATUS (ADMIN) =================
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE_STATUS')")
    @ApiMessage("Update employee status")
    public ResponseEntity<ResEmployeeDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ReqAdminUpdateStatusDTO req
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                employeeService.updateStatus(id, req.getStatus())
        );
    }

    // ================= SELF UPDATE =================
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE_SELF')")
    @ApiMessage("Update employee profile")
    public ResponseEntity<ResEmployeeDTO> updateSelf(
            @RequestBody ReqEmployeeSelfUpdateDTO req) {

        return ResponseEntity.status(HttpStatus.OK).body(employeeService.updateSelf(req));
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
    @ApiMessage("Delete employee by id")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        employeeService.deleteEmployee(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('EMPLOYEE_LIST_DELETED')")
    @ApiMessage("Fetch all employee deleted")
    public ResponseEntity<ResultPaginationDTO> getDeletedEmployees(
            Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                employeeService.listDeleted(pageable)
        );
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('EMPLOYEE_RESTORE')")
    @ApiMessage("Restore employee")
    public ResponseEntity<ResEmployeeDTO> restore(@PathVariable UUID id) {

        return ResponseEntity.status(HttpStatus.OK).body(
                employeeService.restoreEmployee(id)
        );
    }

    // ================= UPDATE ROLES =================
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE_ROLE')")
    @ApiMessage("Update employee roles")
    public ResponseEntity<Void> updateRoles(
            @PathVariable UUID id,
            @RequestBody @Valid ReqUpdateEmployeeRolesDTO req) {

        employeeService.updateRoles(id, req.getRoles());
        return ResponseEntity.ok().build();
    }

}
