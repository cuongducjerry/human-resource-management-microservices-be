package com.hrm.organization.service;

import com.hrm.organization.client.EmployeeClient;
import com.hrm.organization.dto.request.ReqCreatePositionDTO;
import com.hrm.organization.dto.request.ReqUpdatePositionDTO;
import com.hrm.organization.dto.response.ResPositionDTO;
import com.hrm.organization.dto.response.ResultPaginationDTO;
import com.hrm.organization.entity.Organization;
import com.hrm.organization.entity.Position;
import com.hrm.organization.mapper.PaginationMapper;
import com.hrm.organization.mapper.PositionMapper;
import com.hrm.organization.repository.OrganizationRepository;
import com.hrm.organization.repository.PositionRepository;
import com.hrm.organization.specification.PositionSpecification;
import com.hrm.organization.util.error.BadRequestException;
import com.hrm.organization.util.error.IdInvalidException;
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
public class PositionService {

    private final PositionRepository positionRepository;
    private final OrganizationRepository organizationRepository;
    private final PositionMapper positionMapper;
    private final PaginationMapper paginationMapper;
    private final EmployeeClient employeeClient;

    // ================= CREATE =================
    public ResPositionDTO create(ReqCreatePositionDTO req) {

        Organization organization = organizationRepository.findById(req.getOrganizationId())
                .orElseThrow(() -> new IdInvalidException("Organization not found"));

        Position position = Position.builder()
                .name(req.getName())
                .description(req.getDescription())
                .level(req.getLevel())
                .organizationId(organization.getId())
                .build();

        positionRepository.save(position);

        return positionMapper.convertToResPositionDTO(position);
    }

    // ================= LIST =================
    @Transactional(readOnly = true)
    public ResultPaginationDTO getAll(
            UUID organizationId,
            String search,
            Pageable pageable
    ) {

        Specification<Position> spec =
                PositionSpecification.filter(organizationId, search);

        Page<Position> page = positionRepository.findAll(spec, pageable);

        List<ResPositionDTO> list = page.getContent()
                .stream()
                .map(positionMapper::convertToResPositionDTO)
                .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                list
        );
    }

    // ================= UPDATE =================
    public ResPositionDTO update(UUID id, ReqUpdatePositionDTO req) {

        Position position = findPositionById(id);

        // Validate organization tồn tại
        Organization organization = organizationRepository.findById(req.getOrganizationId())
                .orElseThrow(() -> new IdInvalidException("Organization not found"));

        // Validate duplicate names within the same organization
        if (positionRepository.existsByNameAndOrganizationIdAndIdNot(
                req.getName(),
                req.getOrganizationId(),
                id)) {
            throw new BadRequestException("Position name already exists in this organization");
        }

        // Update fields
        position.setName(req.getName());
        position.setDescription(req.getDescription());
        position.setLevel(req.getLevel());
        position.setOrganizationId(organization.getId());

        positionRepository.save(position);

        return positionMapper.convertToResPositionDTO(position);
    }

    // ================= GET BY ID =================
    @Transactional(readOnly = true)
    public ResPositionDTO getById(UUID id) {
        Position p = findPositionById(id);

        ResPositionDTO dto = positionMapper.convertToResPositionDTO(p);
        return positionMapper.convertToResPositionDTO(p);
    }

    // ================= DELETE =================
    @Transactional
    public void delete(UUID id) {

        Position position = findPositionById(id);

        if (employeeClient.existsByPosition(id)) {
            throw new BadRequestException(
                    "Cannot delete position assigned to employees"
            );
        }

        positionRepository.delete(position);
    }

    // ================= PRIVATE COMMON METHOD =================
    private Position findPositionById(UUID id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Position not found"));
    }
}
