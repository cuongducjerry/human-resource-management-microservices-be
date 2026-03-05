package com.hrm.attendance.controller;

import com.hrm.attendance.dto.request.ReqCheckInDTO;
import com.hrm.attendance.dto.request.ReqCheckOutDTO;
import com.hrm.attendance.dto.response.ResAttendanceDTO;
import com.hrm.attendance.dto.response.ResultPaginationDTO;
import com.hrm.attendance.service.AttendanceService;
import com.hrm.attendance.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ===== CHECK IN =====
    @PostMapping("/check-in")
    @PreAuthorize("hasAuthority('ATTENDANCE_CHECK')")
    @ApiMessage("Check in attendance")
    public ResponseEntity<ResAttendanceDTO> checkIn() {
        return ResponseEntity.ok(
                attendanceService.checkIn()
        );
    }

    // ===== CHECK OUT =====
    @PostMapping("/check-out")
    @PreAuthorize("hasAuthority('ATTENDANCE_CHECK')")
    @ApiMessage("Check out attendance")
    public ResponseEntity<ResAttendanceDTO> checkOut() {

        return ResponseEntity.ok(
                attendanceService.checkOut()
        );
    }

    // ================= LIST =================
    @GetMapping
    @PreAuthorize("hasAuthority('ATTENDANCE_LIST')")
    @ApiMessage("Fetch attendance list")
    public ResponseEntity<ResultPaginationDTO> listAttendance(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            Pageable pageable
    ) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(attendanceService.listAttendances(
                        employeeId,
                        startDate,
                        endDate,
                        pageable
                ));
    }

    // ================= VIEW DETAIL =================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTENDANCE_VIEW')")
    @ApiMessage("Fetch attendance by id")
    public ResponseEntity<ResAttendanceDTO> getById(
            @PathVariable UUID id) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(attendanceService.getById(id));
    }

}
