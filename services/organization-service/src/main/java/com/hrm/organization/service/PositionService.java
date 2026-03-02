package com.hrm.organization.service;

import com.hrm.organization.dto.request.ReqCreatePositionDTO;
import com.hrm.organization.dto.response.ResPositionDTO;
import com.hrm.organization.dto.response.ResultPaginationDTO;
import com.hrm.organization.entity.Organization;
import com.hrm.organization.entity.Position;
import com.hrm.organization.mapper.PaginationMapper;
import com.hrm.organization.mapper.PositionMapper;
import com.hrm.organization.repository.OrganizationRepository;
import com.hrm.organization.repository.PositionRepository;
import com.hrm.organization.specification.PositionSpecification;
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

    // ================= GET BY ID =================
    @Transactional(readOnly = true)
    public ResPositionDTO getById(UUID id) {
        return positionMapper.convertToResPositionDTO(findPositionById(id));
    }

    // ================= DELETE =================
    public void delete(UUID id) {
        Position position = findPositionById(id);
        positionRepository.delete(position);
    }

    // ================= PRIVATE COMMON METHOD =================
    private Position findPositionById(UUID id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Position not found"));
    }
}
