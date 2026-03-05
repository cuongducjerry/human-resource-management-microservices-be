package com.hrm.attendance.service;

import com.hrm.attendance.dto.request.ReqCreateWorkShiftDTO;
import com.hrm.attendance.dto.request.ReqUpdateWorkShiftDTO;
import com.hrm.attendance.dto.response.ResWorkShiftDTO;
import com.hrm.attendance.dto.response.ResultPaginationDTO;
import com.hrm.attendance.entity.WorkShift;
import com.hrm.attendance.mapper.PaginationMapper;
import com.hrm.attendance.mapper.WorkShiftMapper;
import com.hrm.attendance.repository.AttendanceRepository;
import com.hrm.attendance.repository.WorkShiftRepository;
import com.hrm.attendance.specification.WorkShiftSpecification;
import com.hrm.attendance.util.error.BadRequestException;
import com.hrm.attendance.util.error.IdInvalidException;
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
public class WorkShiftService {

    private final AttendanceRepository attendanceRepository;
    private final WorkShiftMapper workShiftMapper;
    private final WorkShiftRepository workShiftRepository;
    private final PaginationMapper paginationMapper;

    // ===== CREATE =====
    @Transactional
    public ResWorkShiftDTO create(ReqCreateWorkShiftDTO request) {

        if (workShiftRepository.existsByName(request.getName())) {
            throw new BadRequestException("Shift name already exists");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        WorkShift shift = WorkShift.builder()
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .allowLateMinutes(request.getAllowLateMinutes())
                .standardWorkHours(request.getStandardWorkHours())
                .build();

        workShiftRepository.save(shift);

        return workShiftMapper.toDTO(shift);
    }

    // ===== UPDATE =====
    @Transactional
    public ResWorkShiftDTO update(UUID id, ReqUpdateWorkShiftDTO request) {

        WorkShift shift = getEntityById(id);

        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setAllowLateMinutes(request.getAllowLateMinutes());
        shift.setStandardWorkHours(request.getStandardWorkHours());

        return workShiftMapper.toDTO(shift);
    }

    // ===== DELETE (SOFT) =====
    @Transactional
    public void delete(UUID id) {

        WorkShift shift = getEntityById(id);

        // Không cho xóa nếu đã có attendance dùng
        boolean used = attendanceRepository.existsByShiftId(id);

        if (used) {
            throw new BadRequestException("Cannot delete shift already used in attendance");
        }

        workShiftRepository.delete(shift);
    }

    // ===== GET BY ID =====
    public ResWorkShiftDTO getById(UUID id) {
        return workShiftMapper.toDTO(getEntityById(id));
    }

    public WorkShift getEntityById(UUID id) {
        return workShiftRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Shift not found"));
    }

    // ===== LIST =====
    public ResultPaginationDTO list(String name, Pageable pageable) {

        Specification<WorkShift> spec =
                WorkShiftSpecification.hasName(name);

        Page<WorkShift> page = workShiftRepository.findAll(spec, pageable);

        int pageNumber = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        int totalPages = page.getTotalPages();
        long totalElements = page.getTotalElements();

        List<ResWorkShiftDTO> list = page.getContent()
                .stream()
                .map(workShiftMapper::toDTO)
                .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageNumber, pageSize, totalPages, totalElements, list);
    }

}