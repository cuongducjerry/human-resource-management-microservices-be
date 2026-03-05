package com.hrm.attendance.mapper;

import com.hrm.attendance.dto.response.ResWorkShiftDTO;
import com.hrm.attendance.entity.WorkShift;
import org.springframework.stereotype.Component;

@Component
public class WorkShiftMapper {

    public ResWorkShiftDTO toDTO(WorkShift shift) {
        return ResWorkShiftDTO.builder()
                .id(shift.getId())
                .name(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .allowLateMinutes(shift.getAllowLateMinutes())
                .standardWorkHours(shift.getStandardWorkHours())
                .createdAt(shift.getCreatedAt())
                .updatedAt(shift.getUpdatedAt())
                .createdBy(shift.getCreatedBy())
                .updatedBy(shift.getUpdatedBy())
                .build();
    }

}
