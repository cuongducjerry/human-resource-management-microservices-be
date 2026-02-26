package com.hrm.organization.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
public class OrganizationController {

//    private final OrganizationService organizationService;
//
//    // Tạo phòng ban
//    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
//    @PostMapping
//    public ResponseEntity<OrganizationResponse> create(
//            @RequestBody OrganizationRequest request) {
//
//        return ResponseEntity.ok(organizationService.create(request));
//    }
//
//    // Cập nhật phòng ban
//    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
//    @PutMapping("/{id}")
//    public ResponseEntity<OrganizationResponse> update(
//            @PathVariable Long id,
//            @RequestBody OrganizationRequest request) {
//
//        return ResponseEntity.ok(organizationService.update(id, request));
//    }
//
//    // Xoá phòng ban
//    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//
//        organizationService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    // Xem danh sách
//    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_HR_ADMIN')")
//    @GetMapping
//    public ResponseEntity<List<OrganizationResponse>> getAll() {
//
//        return ResponseEntity.ok(organizationService.getAll());
//    }
}
