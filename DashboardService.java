package com.example.financeapi.service;

import com.example.financeapi.model.FinancialRecord;
import com.example.financeapi.repository.FinancialRecordRepository;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    public SummaryResponse getSummary() {
        BigDecimal totalIncome = recordRepository.sumByType(FinancialRecord.RecordType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(FinancialRecord.RecordType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        return SummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .build();
    }

    public List<CategoryTotal> getCategoryTotals() {
        return recordRepository.categoryTotals().stream()
                .map(row -> CategoryTotal.builder()
                        .category((String) row[0])
                        .type(row[1].toString())
                        .total((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public List<MonthlyTrend> getMonthlyTrends(int year) {
        Map<String, MonthlyTrend> trendMap = new LinkedHashMap<>();

        recordRepository.monthlyTrends(year).forEach(row -> {
            int monthNum = ((Number) row[0]).intValue();
            String monthName = Month.of(monthNum).name();
            String type = row[1].toString();
            BigDecimal total = (BigDecimal) row[2];

            trendMap.computeIfAbsent(monthName, k -> MonthlyTrend.builder()
                    .month(monthName)
                    .income(BigDecimal.ZERO)
                    .expense(BigDecimal.ZERO)
                    .build());

            MonthlyTrend trend = trendMap.get(monthName);
            if ("INCOME".equals(type)) trend.setIncome(total);
            else trend.setExpense(total);
        });

        return new ArrayList<>(trendMap.values());
    }

    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public List<WeeklyTrend> getWeeklyTrends() {
        LocalDate fourWeeksAgo = LocalDate.now().minusWeeks(4);
        Map<String, WeeklyTrend> trendMap = new LinkedHashMap<>();

        recordRepository.weeklyTrends(fourWeeksAgo).forEach(row -> {
            String week = "Week " + row[0];
            String type = row[1].toString();
            BigDecimal total = (BigDecimal) row[2];

            trendMap.computeIfAbsent(week, k -> WeeklyTrend.builder()
                    .week(k)
                    .income(BigDecimal.ZERO)
                    .expense(BigDecimal.ZERO)
                    .build());

            WeeklyTrend trend = trendMap.get(week);
            if ("INCOME".equals(type)) trend.setIncome(total);
            else trend.setExpense(total);
        });

        return new ArrayList<>(trendMap.values());
    }

    public List<RecentActivity> getRecentActivity(int limit) {
        return recordRepository.findRecentRecords(PageRequest.of(0, limit)).stream()
                .map(r -> RecentActivity.builder()
                        .id(r.getId())
                        .description(r.getCategory() + (r.getNotes() != null ? " — " + r.getNotes() : ""))
                        .type(r.getType().name())
                        .amount(r.getAmount())
                        .date(r.getDate())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Response types ───────────────────────────────────────────────

    @Data @Builder
    public static class SummaryResponse {
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netBalance;
    }

    @Data @Builder
    public static class CategoryTotal {
        private String category;
        private String type;
        private BigDecimal total;
    }

    @Data @Builder
    public static class MonthlyTrend {
        private String month;
        private BigDecimal income;
        private BigDecimal expense;
    }

    @Data @Builder
    public static class WeeklyTrend {
        private String week;
        private BigDecimal income;
        private BigDecimal expense;
    }

    @Data @Builder
    public static class RecentActivity {
        private Long id;
        private String description;
        private String type;
        private BigDecimal amount;
        private LocalDate date;
    }
}
