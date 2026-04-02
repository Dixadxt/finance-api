package com.example.financeapi.controller;

import com.example.financeapi.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // GET /api/dashboard/summary — all roles (VIEWER+)
    // Returns: total income, total expenses, net balance
    @GetMapping("/summary")
    public ResponseEntity<DashboardService.SummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    // GET /api/dashboard/categories — all roles (VIEWER+)
    // Returns: per-category totals broken down by type
    @GetMapping("/categories")
    public ResponseEntity<List<DashboardService.CategoryTotal>> getCategoryTotals() {
        return ResponseEntity.ok(dashboardService.getCategoryTotals());
    }

    // GET /api/dashboard/trends/monthly?year=2025 — ANALYST+ (enforced via @PreAuthorize in service)
    @GetMapping("/trends/monthly")
    public ResponseEntity<List<DashboardService.MonthlyTrend>> getMonthlyTrends(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        return ResponseEntity.ok(dashboardService.getMonthlyTrends(year));
    }

    // GET /api/dashboard/trends/weekly — ANALYST+ (last 4 weeks)
    @GetMapping("/trends/weekly")
    public ResponseEntity<List<DashboardService.WeeklyTrend>> getWeeklyTrends() {
        return ResponseEntity.ok(dashboardService.getWeeklyTrends());
    }

    // GET /api/dashboard/recent?limit=10 — all roles (VIEWER+)
    @GetMapping("/recent")
    public ResponseEntity<List<DashboardService.RecentActivity>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        if (limit < 1 || limit > 50) limit = 10;
        return ResponseEntity.ok(dashboardService.getRecentActivity(limit));
    }
}
