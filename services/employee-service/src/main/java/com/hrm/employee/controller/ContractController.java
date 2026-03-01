package com.hrm.employee.controller;

import com.hrm.employee.dto.request.ReqCreateContractDTO;
import com.hrm.employee.dto.request.ReqUpdateContractDTO;
import com.hrm.employee.dto.request.ReqUpdateContractStatusDTO;
import com.hrm.employee.dto.response.ResContractDTO;
import com.hrm.employee.dto.response.ResultPaginationDTO;
import com.hrm.employee.service.ContractService;
import com.hrm.employee.util.annotation.ApiMessage;
import com.hrm.employee.util.constant.ContractStatus;
import com.hrm.employee.util.constant.ContractType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    // ================= CREATE =================
    @PostMapping
    @PreAuthorize("hasAuthority('CONTRACT_CREATE')")
    @ApiMessage("Create contract")
    public ResponseEntity<ResContractDTO> create(
            @Valid @RequestBody ReqCreateContractDTO req) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contractService.create(req));
    }

    // ================= LIST =================
    @GetMapping
    @PreAuthorize("hasAuthority('CONTRACT_LIST')")
    @ApiMessage("Fetch all contracts")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) ContractType type,
            Pageable pageable
    ) {

        return ResponseEntity.status(HttpStatus.OK).body(
                contractService.getAll(id, status, type, pageable)
        );
    }

    // ================= VIEW DETAIL =================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_VIEW')")
    @ApiMessage("Fetch contract by id")
    public ResponseEntity<ResContractDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(contractService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE')")
    @ApiMessage("Update contract")
    public ResponseEntity<ResContractDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReqUpdateContractDTO req
    ) {

        return ResponseEntity.ok(contractService.update(id, req));
    }

    // ================= RENEW =================
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAuthority('CONTRACT_RENEW')")
    @ApiMessage("Renew contract")
    public ResponseEntity<ResContractDTO> renew(
            @PathVariable UUID id,
            @Valid @RequestBody ReqUpdateContractDTO req
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contractService.renew(id, req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('CONTRACT_UPDATE_STATUS')")
    @ApiMessage("Update contract status")
    public ResponseEntity<ResContractDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ReqUpdateContractStatusDTO req
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                contractService.updateStatus(id, req.getStatus())
        );
    }

}
