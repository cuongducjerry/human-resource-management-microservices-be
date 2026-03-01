package com.hrm.employee.service;

import com.hrm.employee.dto.request.ReqCreateContractDTO;
import com.hrm.employee.dto.request.ReqUpdateContractDTO;
import com.hrm.employee.dto.response.ResContractDTO;
import com.hrm.employee.dto.response.ResEmployeeDTO;
import com.hrm.employee.dto.response.ResultPaginationDTO;
import com.hrm.employee.entity.Contract;
import com.hrm.employee.mapper.ContractMapper;
import com.hrm.employee.mapper.PaginationMapper;
import com.hrm.employee.repository.ContractRepository;
import com.hrm.employee.specification.ContractSpecification;
import com.hrm.employee.util.constant.ContractStatus;
import com.hrm.employee.util.constant.ContractType;
import com.hrm.employee.util.error.IdInvalidException;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final PaginationMapper paginationMapper;
    private final ContractMapper contractMapper;

    // ================= CREATE =================
    public ResContractDTO create(ReqCreateContractDTO req) {

        // validate date
        if (req.getEndDate() != null
                && req.getEndDate().isBefore(req.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        // check existing active contract
        if (contractRepository.existsByEmployeeIdAndStatus(
                req.getEmployeeId(),
                ContractStatus.ACTIVE)) {

            throw new BadRequestException(
                    "Employee already has an active contract");
        }

        Contract contract = Contract.builder()
                .employeeId(req.getEmployeeId())
                .type(req.getType())
                .status(ContractStatus.ACTIVE)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .salary(req.getSalary())
                .description(req.getDescription())
                .build();

        contractRepository.save(contract);

        return this.contractMapper.convertToResContractDTO(contract);
    }

    // ================= LIST =================
    public ResultPaginationDTO getAll(
            UUID id,
            ContractStatus status,
            ContractType type,
            Pageable pageable
    ) {

        Specification<Contract> spec =
                ContractSpecification.filter(id, status, type);

        Page<Contract> page = contractRepository.findAll(spec, pageable);

        int pageNumber = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        int totalPages = page.getTotalPages();
        long totalElements = page.getTotalElements();

        List<ResContractDTO> list = page.getContent()
                .stream()
                .map(contractMapper::convertToResContractDTO)
                .collect(Collectors.toList());

        return paginationMapper.convertToResultPaginationDTO(
                pageNumber, pageSize, totalPages, totalElements, list);

    }

    // ================= GET BY ID =================
    public ResContractDTO getById(UUID id) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Contract not found"));

        return this.contractMapper.convertToResContractDTO(contract);
    }

    // ================= UPDATE =================
    public ResContractDTO update(UUID id, ReqUpdateContractDTO req) {

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

        contract.setType(req.getType());
        contract.setStartDate(req.getStartDate());
        contract.setEndDate(req.getEndDate());
        contract.setSalary(req.getSalary());
        contract.setDescription(req.getDescription());

        return this.contractMapper.convertToResContractDTO(contract);
    }

    // ================= RENEW =================
    public ResContractDTO renew(UUID id, ReqUpdateContractDTO req) {

        Contract oldContract = contractRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Contract not found"));

        if (oldContract.getStatus() != ContractStatus.ACTIVE) {
            throw new BadRequestException("Only active contract can be renewed");
        }

        // Close old contract
        oldContract.setStatus(ContractStatus.TERMINATED);
        oldContract.setEndDate(LocalDate.now());

        // Create new contract
        Contract newContract = Contract.builder()
                .employeeId(oldContract.getEmployeeId())
                .type(req.getType())
                .status(ContractStatus.ACTIVE)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .salary(req.getSalary())
                .description(req.getDescription())
                .build();

        contractRepository.save(newContract);

        return this.contractMapper.convertToResContractDTO(newContract);
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

}
