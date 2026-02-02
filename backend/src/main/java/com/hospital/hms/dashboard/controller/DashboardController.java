package com.hospital.hms.dashboard.controller;

import com.hospital.hms.dashboard.dto.DashboardStatsDto;
import com.hospital.hms.dashboard.service.DashboardStatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST API for hospital dashboard statistics (date filter, totals, print data).
 * Base path: /api (context) + /dashboard (mapping).
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    public DashboardController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        DashboardStatsDto stats = dashboardStatsService.getStats(fromDate, toDate);
        return ResponseEntity.ok(stats);
    }
}
