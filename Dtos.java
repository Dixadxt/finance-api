package com.example.financeapi.dto;

import com.example.financeapi.model.FinancialRecord;
import com.example.financeapi.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// ─────────────────────────────────────────────
// Auth DTOs
// ─────────────────────────────────────────────
public class Dtos {

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String type = "Bearer";
        private Long userId;
        private String name;
        private String email;
        private String role;
    }

    // ─────────────────────────────────────────────
    // User DTOs
    // ─────────────────────────────────────────────

    @Data
    public static class CreateUserRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
        @NotBlank
        private String name;
        private User.Role role;
    }

    @Data
    public static class UpdateUserRequest {
        private String name;
        private User.Role role;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private String role;
        private boolean active;
        private LocalDateTime createdAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .active(user.isActive())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    // ─────────────────────────────────────────────
    // Financial Record DTOs
    // ─────────────────────────────────────────────

    @Data
    public static class CreateRecordRequest {
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive")
        private BigDecimal amount;
        @NotNull
        private FinancialRecord.RecordType type;
        @NotBlank
        private String category;
        @NotNull
        private LocalDate date;
        private String notes;
    }

    @Data
    public static class UpdateRecordRequest {
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        private BigDecimal amount;
        private FinancialRecord.RecordType type;
        private String category;
        private LocalDate date;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordResponse {
        private Long id;
        private BigDecimal amount;
        private String type;
        private String category;
        private LocalDate date;
        private String notes;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static RecordResponse from(FinancialRecord r) {
            return RecordResponse.builder()
                    .id(r.getId())
                    .amount(r.getAmount())
                    .type(r.getType().name())
                    .category(r.getCategory())
                    .date(r.getDate())
                    .notes(r.getNotes())
                    .createdBy(r.getCreatedBy().getName())
                    .createdAt(r.getCreatedAt())
                    .updatedAt(r.getUpdatedAt())
                    .build();
        }
    }
}
