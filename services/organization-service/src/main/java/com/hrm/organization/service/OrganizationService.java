package com.hrm.organization.service;

import com.hrm.organization.dto.request.ReqCreateOrganizationDTO;
import com.hrm.organization.dto.response.ResOrganizationDTO;
import com.hrm.organization.dto.response.ResultPaginationDTO;
import com.hrm.organization.entity.Organization;
import com.hrm.organization.mapper.OrganizationMapper;
import com.hrm.organization.mapper.PaginationMapper;
import com.hrm.organization.repository.OrganizationRepository;
import com.hrm.organization.repository.PositionRepository;
import com.hrm.organization.specification.OrganizationSpecification;
import com.hrm.organization.util.constant.OrganizationStatus;
import com.hrm.organization.util.error.IdInvalidException;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final PositionRepository positionRepository;
    private final OrganizationMapper organizationMapper;
    private final PaginationMapper paginationMapper;

    // ================= CREATE =================
    public ResOrganizationDTO create(ReqCreateOrganizationDTO req) {

        validateCodeUnique(req.getCode());
        validateParentExists(req.getParentId());

        Organization org = Organization.builder()
                .code(req.getCode())
                .name(req.getName())
                .parentId(req.getParentId())
                .status(OrganizationStatus.ACTIVE)
                .build();

        organizationRepository.save(org);

        return organizationMapper.convertToResOrganizationDTO(org);
    }

    // ================= LIST =================
    @Transactional(readOnly = true)
    public ResultPaginationDTO getAll(
            UUID id,
            OrganizationStatus status,
            String search,
            Pageable pageable
    ) {

        Specification<Organization> spec =
                OrganizationSpecification.filter(id, status, search);

        Page<Organization> page =
                organizationRepository.findAll(spec, pageable);

        List<ResOrganizationDTO> list = page.getContent()
                .stream()
                .map(organizationMapper::convertToResOrganizationDTO)
                .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                list
        );
    }

    // ================= GET BY ID =================
    @Transactional(readOnly = true)
    public ResOrganizationDTO getById(UUID id) {
        return organizationMapper.convertToResOrganizationDTO(
                findOrganizationById(id)
        );
    }

    // ================= DELETE =================
    public void delete(UUID id) {

        Organization org = findOrganizationById(id);

        if (positionRepository.existsByOrganizationId(id)) {
            throw new BadRequestException(
                    "Cannot delete organization with positions");
        }

        organizationRepository.delete(org);
    }

    // ================= PRIVATE COMMON METHODS =================

    private Organization findOrganizationById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() ->
                        new IdInvalidException("Organization not found"));
    }

    private void validateCodeUnique(String code) {
        if (organizationRepository.existsByCode(code)) {
            throw new BadRequestException("Code already exists");
        }
    }

    private void validateParentExists(UUID parentId) {
        if (parentId != null &&
                !organizationRepository.existsById(parentId)) {
            throw new IdInvalidException("Parent organization not found");
        }
    }
}
