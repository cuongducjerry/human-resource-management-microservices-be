package com.hrm.organization.service;

import com.hrm.organization.client.EmployeeClient;
import com.hrm.organization.dto.request.ReqCreateOrganizationDTO;
import com.hrm.organization.dto.request.ReqUpdateOrganizationDTO;
import com.hrm.organization.dto.response.ResOrganizationDTO;
import com.hrm.organization.dto.response.ResOrganizationTreeDTO;
import com.hrm.organization.dto.response.ResultPaginationDTO;
import com.hrm.organization.entity.Organization;
import com.hrm.organization.mapper.OrganizationMapper;
import com.hrm.organization.mapper.PaginationMapper;
import com.hrm.organization.repository.OrganizationRepository;
import com.hrm.organization.repository.PositionRepository;
import com.hrm.organization.specification.OrganizationSpecification;
import com.hrm.organization.util.constant.OrganizationStatus;
import com.hrm.organization.util.error.BadRequestException;
import com.hrm.organization.util.error.IdInvalidException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final PositionRepository positionRepository;
    private final OrganizationMapper organizationMapper;
    private final PaginationMapper paginationMapper;
    private final EmployeeClient employeeClient;

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

        if (organizationRepository.existsByParentId(id)) {
            throw new BadRequestException(
                    "Cannot delete organization with child organizations");
        }

        if (positionRepository.existsByOrganizationId(id)) {
            throw new BadRequestException(
                    "Cannot delete organization with positions");
        }

        if (employeeClient.existsByOrganization(id)) {
            throw new BadRequestException(
                    "Cannot delete organization with employees");
        }

        organizationRepository.delete(org);
    }

    // ================= UPDATE =================
    public ResOrganizationDTO update(UUID id, ReqUpdateOrganizationDTO req) {

        Organization org = findOrganizationById(id);

        // Check for duplicate codes (excluding the code itself).
        if (organizationRepository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new BadRequestException("Code already exists");
        }

        // No self-parent allowed.
        if (req.getParentId() != null && req.getParentId().equals(id)) {
            throw new BadRequestException("Organization cannot be its own parent");
        }

        // If a parent exists, check if it does
        if (req.getParentId() != null) {
            Organization parent = findOrganizationById(req.getParentId());

            // Basic loop check: prevents the set parent from being a child of it.
            if (isCircularDependency(id, parent.getId())) {
                throw new BadRequestException("Circular parent relationship detected");
            }
        }

        // Update field
        org.setCode(req.getCode());
        org.setName(req.getName());
        org.setParentId(req.getParentId());

        if (req.getStatus() != null) {
            org.setStatus(req.getStatus());
        }

        organizationRepository.save(org);

        return organizationMapper.convertToResOrganizationDTO(org);
    }

    // ================= TREE =================
    @Transactional(readOnly = true)
    public List<ResOrganizationTreeDTO> getOrganizationTree() {

        List<Organization> organizations =
                organizationRepository.findAllByActiveTrue();

        if (organizations.isEmpty()) {
            return Collections.emptyList();
        }

        // Map entity -> DTO
        Map<UUID, ResOrganizationTreeDTO> dtoMap = new HashMap<>();

        for (Organization org : organizations) {
            dtoMap.put(
                    org.getId(),
                    ResOrganizationTreeDTO.builder()
                            .id(org.getId())
                            .name(org.getName())
                            .children(new ArrayList<>())
                            .build()
            );
        }

        List<ResOrganizationTreeDTO> roots = new ArrayList<>();

        for (Organization org : organizations) {

            if (org.getParentId() == null) {
                roots.add(dtoMap.get(org.getId()));
            } else {
                ResOrganizationTreeDTO parent =
                        dtoMap.get(org.getParentId());

                if (parent != null) {
                    parent.getChildren()
                            .add(dtoMap.get(org.getId()));
                }
            }
        }

        return roots;
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

    private boolean isCircularDependency(UUID currentId, UUID newParentId) {

        UUID parentId = newParentId;

        while (parentId != null) {

            if (parentId.equals(currentId)) {
                return true;
            }

            Organization parent = organizationRepository.findById(parentId)
                    .orElse(null);

            if (parent == null) {
                break;
            }

            parentId = parent.getParentId();
        }

        return false;
    }

}
