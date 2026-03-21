package com.hrm.leave.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEvent {

    private String type;
    private Object data;
    private LocalDateTime createdAt;
}
