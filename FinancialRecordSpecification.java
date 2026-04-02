package com.example.financeapi.repository;

import com.example.financeapi.model.FinancialRecord;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinancialRecordSpecification {

    public static Specification<FinancialRecord> withFilters(
            FinancialRecord.RecordType type,
            String category,
            LocalDate from,
            LocalDate to) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted
            predicates.add(cb.isFalse(root.get("deleted")));

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("category")),
                        "%" + category.toLowerCase() + "%"));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
