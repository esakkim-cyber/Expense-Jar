package com.example.expense_jar.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ApiModels {
    private ApiModels() {}

        public record UserRequest(
            @Schema(description = "Name to display for this user", example = "Taylor Morgan")
            @NotBlank String name,
            @Schema(description = "Valid, unique email address", example = "taylor@example.com")
            @NotBlank @Email String email) {}

        public record CategoryRequest(
            @Schema(description = "Unique category name", example = "Groceries")
            @NotBlank String name) {}

        public record ExpenseRequest(
            @Schema(description = "Expense amount; must be greater than zero", example = "24.50")
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @Schema(description = "Optional note, up to 250 characters", example = "Weekly groceries")
            @Size(max = 250) String description,
            @Schema(description = "Date of the expense in YYYY-MM-DD format", example = "2026-09-28")
            @NotNull LocalDate date,
            @Schema(description = "ID of an existing user", example = "1")
            @NotNull Long userId,
            @Schema(description = "ID of an existing category", example = "1")
            @NotNull Long categoryId) {}

        public record BudgetRequest(
            @Schema(description = "Monthly limit; must be greater than zero", example = "500.00")
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @Schema(description = "Calendar year", example = "2026", minimum = "2000")
            @Min(2000) int year,
            @Schema(description = "Month number: 1 = January, 12 = December", example = "9", minimum = "1", maximum = "12")
            @Min(1) @Max(12) int month,
            @Schema(description = "ID of an existing user", example = "1")
            @NotNull Long userId,
            @Schema(description = "ID of an existing category", example = "1")
            @NotNull Long categoryId) {}
    public record ExpenseView(Long id, BigDecimal amount, String description, LocalDate date,
                              Long userId, Long categoryId, String categoryName) {}
    public record BudgetView(Long id, Long userId, Long categoryId, String categoryName,
                             int year, int month, BigDecimal budget, BigDecimal spent,
                             BigDecimal percentUsed, boolean warning) {}
    public record CategorySummary(Long categoryId, String categoryName, BigDecimal spent,
                                  BigDecimal budget, BigDecimal percentUsed, boolean warning) {}
    public record MonthlySummary(Long userId, int year, int month, BigDecimal totalSpent,
                                 List<CategorySummary> categories) {}
    public record MonthlyTrend(int year, int month, BigDecimal totalSpent) {}
    @Schema(description = "Standard error response returned when a request cannot be completed")
    public record ErrorResponse(
            @Schema(description = "Machine-readable error type", example = "data_conflict")
            String error,
            @Schema(description = "Explanation of why the request failed", example = "The record is referenced or violates a database constraint")
            String message) {}
}