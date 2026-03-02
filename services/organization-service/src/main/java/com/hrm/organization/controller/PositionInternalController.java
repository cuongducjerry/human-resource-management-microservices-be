package com.hrm.organization.controller;

import com.hrm.organization.dto.response.ResPositionDTO;
import com.hrm.organization.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/positions")
@RequiredArgsConstructor
public class PositionInternalController {

    private final PositionService positionService;

    @GetMapping("/{id}")
    public ResPositionDTO getById(@PathVariable UUID id) {
        return positionService.getById(id);
    }
}
