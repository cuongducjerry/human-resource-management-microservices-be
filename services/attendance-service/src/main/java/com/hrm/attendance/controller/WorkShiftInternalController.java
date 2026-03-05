package com.hrm.attendance.controller;

import com.hrm.attendance.dto.response.ResWorkShiftDTO;
import com.hrm.attendance.service.WorkShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/work-shifts")
@RequiredArgsConstructor
public class WorkShiftInternalController {

    private final WorkShiftService workShiftService;

    @GetMapping("/{id}")
    public ResWorkShiftDTO getWorkShiftById(@PathVariable("id") UUID id) {
        return this.workShiftService.getById(id);
    }

}
