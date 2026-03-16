package com.hrm.employee.service;

import com.hrm.employee.client.AuthClient;
import com.hrm.employee.dto.request.ReqCreateContractDTO;
import com.hrm.employee.dto.request.ReqUpdateContractDTO;
import com.hrm.employee.dto.response.ResContractDTO;
import com.hrm.employee.dto.response.ResEmployeeDTO;
import com.hrm.employee.dto.response.ResultPaginationDTO;
import com.hrm.employee.entity.Contract;
import com.hrm.employee.entity.Employee;
import com.hrm.employee.mapper.ContractMapper;
import com.hrm.employee.mapper.EmployeeMapper;
import com.hrm.employee.mapper.PaginationMapper;
import com.hrm.employee.repository.ContractRepository;
import com.hrm.employee.repository.EmployeeRepository;
import com.hrm.employee.specification.ContractSpecification;
import com.hrm.employee.util.SecurityUtil;
import com.hrm.employee.util.constant.ContractStatus;
import com.hrm.employee.util.constant.ContractType;
import com.hrm.employee.util.error.ForbiddenException;
import com.hrm.employee.util.error.IdInvalidException;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final PaginationMapper paginationMapper;
    private final EmployeeRepository employeeRepository;
    private final ContractMapper contractMapper;
    private final EmployeeMapper employeeMapper;
    private final AuthClient authClient;
    private final CloudinaryService cloudinaryService;
    private final EmployeeService employeeService;

    // ================= CREATE =================
    public ResContractDTO create(ReqCreateContractDTO req, MultipartFile file) {

        if (req.getEndDate() != null
                && req.getEndDate().isBefore(req.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        if (contractRepository.existsByEmployeeIdAndStatus(
                req.getEmployeeId(),
                ContractStatus.ACTIVE)) {

            throw new BadRequestException(
                    "Employee already has an active contract");
        }

        String fileUrl = null;

        if (file != null && !file.isEmpty()) {
            fileUrl = cloudinaryService.uploadContractFile(file);
        }

        Contract contract = Contract.builder()
                .employeeId(req.getEmployeeId())
                .type(req.getType())
                .status(ContractStatus.ACTIVE)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .salary(req.getSalary())
                .description(req.getDescription())
                .fileUrl(fileUrl)
                .build();

        contractRepository.save(contract);

        return contractMapper.convertToResContractDTO(contract);
    }

    // ================= LIST =================
    public ResultPaginationDTO getAll(
            UUID id,
            ContractStatus status,
            ContractType type,
            Pageable pageable
    ) {

        UUID currentEmployeeId =
                UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        Specification<Contract> spec;

        // ===== MANAGER =====
        if (SecurityUtil.hasRole("ROLE_MANAGER")) {

            List<UUID> subordinates = employeeService.getSubordinateIds(currentEmployeeId);

            spec = ContractSpecification.filterForManager(
                    id,
                    subordinates,
                    status,
                    type
            );
        }

        // ===== HR / ADMIN =====
        else {

            spec = ContractSpecification.filterForAdmin(
                    id,
                    status,
                    type
            );
        }

        Page<Contract> page =
                contractRepository.findAll(spec, pageable);

        int pageNumber = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        int totalPages = page.getTotalPages();
        long totalElements = page.getTotalElements();

        List<ResContractDTO> list = page.getContent()
                .stream()
                .map(contractMapper::convertToResContractDTO)
                .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageNumber,
                pageSize,
                totalPages,
                totalElements,
                list
        );
    }


    public ResultPaginationDTO getAllPersonal(
            UUID id,
            ContractStatus status,
            ContractType type,
            Pageable pageable
    ) {

        UUID currentEmployeeId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        Specification<Contract> spec = null;

        // ===== EMPLOYEE =====
        if (SecurityUtil.hasRole("ROLE_EMPLOYEE") || SecurityUtil.hasRole("ROLE_MANAGER")) {

            spec = ContractSpecification.filter(
                    currentEmployeeId,
                    status,
                    type
            );
        }

        Page<Contract> page =
                contractRepository.findAll(spec, pageable);

        int pageNumber = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        int totalPages = page.getTotalPages();
        long totalElements = page.getTotalElements();

        List<ResContractDTO> list = page.getContent()
                .stream()
                .map(contractMapper::convertToResContractDTO)
                .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageNumber,
                pageSize,
                totalPages,
                totalElements,
                list
        );
    }

    // ================= GET BY ID =================
    public ResContractDTO getById(UUID id) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() ->
                        new IdInvalidException("Contract not found"));

        UUID currentEmployeeId =
                UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        // ===== EMPLOYEE =====
        if (SecurityUtil.hasRole("ROLE_EMPLOYEE")) {

            if (!contract.getEmployeeId().equals(currentEmployeeId)) {
                throw new ForbiddenException("You cannot access this contract");
            }
        }

        // ===== MANAGER =====
        if (SecurityUtil.hasRole("ROLE_MANAGER")) {

            List<UUID> subordinates = employeeService.getSubordinateIds(currentEmployeeId);

            subordinates.add(currentEmployeeId);

            if (!subordinates.contains(contract.getEmployeeId())) {
                throw new ForbiddenException("You cannot access this contract");
            }
        }

        return contractMapper.convertToResContractDTO(contract);
    }

    // ================= UPDATE =================
    public ResContractDTO update(UUID id, ReqUpdateContractDTO req, MultipartFile file) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Contract not found"));

        if (contract.getStatus() == ContractStatus.TERMINATED
                || contract.getStatus() == ContractStatus.EXPIRED) {
            throw new BadRequestException("Cannot update closed contract");
        }

        if (req.getEndDate() != null
                && req.getEndDate().isBefore(req.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        // ===== UPDATE BASIC INFO =====
        contract.setType(req.getType());
        contract.setStartDate(req.getStartDate());
        contract.setEndDate(req.getEndDate());
        contract.setSalary(req.getSalary());
        contract.setDescription(req.getDescription());

        // ===== UPDATE FILE =====
        if (file != null && !file.isEmpty()) {

            // delete old file
            if (contract.getFileUrl() != null && !contract.getFileUrl().isBlank()) {
                cloudinaryService.deleteContractFile(contract.getFileUrl());
            }

            // upload new file
            String newFileUrl = cloudinaryService.uploadContractFile(file);

            contract.setFileUrl(newFileUrl);
        }

        contractRepository.save(contract);

        return contractMapper.convertToResContractDTO(contract);
    }

    // ================= RENEW =================
    public ResContractDTO renew(UUID id, ReqUpdateContractDTO req, MultipartFile file) {

        Contract oldContract = contractRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Contract not found"));

        if (oldContract.getStatus() != ContractStatus.ACTIVE) {
            throw new BadRequestException("Only active contract can be renewed");
        }

        // close old contract
        oldContract.setStatus(ContractStatus.TERMINATED);
        oldContract.setEndDate(LocalDate.now());

        String fileUrl = null;

        // upload new contract file
        if (file != null && !file.isEmpty()) {
            fileUrl = cloudinaryService.uploadContractFile(file);
        }

        // create new contract
        Contract newContract = Contract.builder()
                .employeeId(oldContract.getEmployeeId())
                .type(req.getType())
                .status(ContractStatus.ACTIVE)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .salary(req.getSalary())
                .description(req.getDescription())
                .fileUrl(fileUrl)
                .build();

        contractRepository.save(oldContract);
        contractRepository.save(newContract);

        return contractMapper.convertToResContractDTO(newContract);
    }

    public ResContractDTO updateStatus(UUID id, ContractStatus status) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Contract not found"));

        // Lifecycle control
        if (contract.getStatus() == ContractStatus.TERMINATED
                || contract.getStatus() == ContractStatus.EXPIRED) {
            throw new BadRequestException("Closed contract cannot change status");
        }

        // Rule
        if (status == ContractStatus.ACTIVE &&
                contractRepository.existsByEmployeeIdAndStatus(
                        contract.getEmployeeId(), ContractStatus.ACTIVE)) {
            throw new BadRequestException("Employee already has active contract");
        }

        contract.setStatus(status);

        if (status == ContractStatus.TERMINATED) {
            contract.setEndDate(LocalDate.now());
        }

        return this.contractMapper.convertToResContractDTO(contract);
    }

    public List<ResEmployeeDTO> getEmployeesAvailableForContract() {

        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
                .filter(emp ->
                        !contractRepository.existsByEmployeeIdAndStatus(
                                emp.getId(),
                                ContractStatus.ACTIVE
                        )
                )
                .filter(emp -> {
                    List<String> roles = authClient.getUserRoles(emp.getKeycloakUserId());

                    return roles.stream().noneMatch(role ->
                            role.equals("ROLE_SUPER_ADMIN") ||
                                    role.equals("ROLE_HR_ADMIN")
                    );
                })
                .map(employeeMapper::convertToResEmployeeDTO)
                .collect(Collectors.toList());
    }

    public ResContractDTO getActiveContractByEmployee(UUID employeeId) {

        Optional<Contract> optional = contractRepository
                .findByEmployeeIdAndStatus(employeeId, ContractStatus.ACTIVE);

        System.out.println("=================================== CONTRACT =================================");
        System.out.println("EMPLOYEE: " + employeeId);
        System.out.println("CONTRACT FOUND: " + optional.isPresent());

        if (optional.isEmpty()) {
            return null;
        }

        Contract contract = optional.get();

        return ResContractDTO.builder()
                .id(contract.getId())
                .employeeId(contract.getEmployeeId())
                .type(contract.getType())
                .status(contract.getStatus())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .salary(contract.getSalary())
                .description(contract.getDescription())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .createdBy(contract.getCreatedBy())
                .updatedBy(contract.getUpdatedBy())
                .build();
    }

}
