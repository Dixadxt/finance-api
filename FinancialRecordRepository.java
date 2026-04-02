package com.example.financeapi.repository;

import com.example.financeapi.model.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>,
        JpaSpecificationExecutor<FinancialRecord> {

    // Total by type (non-deleted)
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = :type AND r.deleted = false")
    BigDecimal sumByType(@Param("type") FinancialRecord.RecordType type);

    // Category-wise totals
    @Query("SELECT r.category, r.type, SUM(r.amount) FROM FinancialRecord r WHERE r.deleted = false GROUP BY r.category, r.type ORDER BY r.category")
    List<Object[]> categoryTotals();

    // Monthly trends for a given year
    @Query("SELECT MONTH(r.date), r.type, SUM(r.amount) FROM FinancialRecord r " +
           "WHERE YEAR(r.date) = :year AND r.deleted = false GROUP BY MONTH(r.date), r.type ORDER BY MONTH(r.date)")
    List<Object[]> monthlyTrends(@Param("year") int year);

    // Recent N records
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false ORDER BY r.date DESC, r.createdAt DESC")
    List<FinancialRecord> findRecentRecords(org.springframework.data.domain.Pageable pageable);

    // Weekly trends (last 4 weeks)
    @Query("SELECT WEEK(r.date), r.type, SUM(r.amount) FROM FinancialRecord r " +
           "WHERE r.date >= :from AND r.deleted = false GROUP BY WEEK(r.date), r.type ORDER BY WEEK(r.date)")
    List<Object[]> weeklyTrends(@Param("from") LocalDate from);
}
