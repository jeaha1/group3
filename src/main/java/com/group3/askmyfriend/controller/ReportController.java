package com.group3.askmyfriend.controller;

import com.group3.askmyfriend.dto.ReportRequestDto;
import com.group3.askmyfriend.entity.ReportEntity;
import com.group3.askmyfriend.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    /**
     * 사용자 신고 등록
     */
    @PostMapping
    public ResponseEntity<Void> createReport(@RequestBody ReportRequestDto dto) {
        Long id = service.save(dto);
        return ResponseEntity.created(URI.create("/reports/" + id)).build();
    }

    /**
     * 관리자 전용: 신고 상태 변경
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        ReportEntity.Status newStatus = ReportEntity.Status.valueOf(body.get("status"));
        service.updateStatus(id, newStatus);
        return ResponseEntity.noContent().build();
    }
}
