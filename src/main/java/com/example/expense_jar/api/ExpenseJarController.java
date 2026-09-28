package com.example.expense_jar.api;

import com.example.expense_jar.api.ApiModels.*;
import com.example.expense_jar.model.*;
import com.example.expense_jar.service.ExpenseJarService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ExpenseJarController {
    private final ExpenseJarService service;

    public ExpenseJarController(ExpenseJarService service) { this.service = service; }

    @Operation(summary = "Create a user", description = "Creates a user with a unique, valid email address.", tags = "Users")
    @PostMapping("/users")
    public AppUser createUser(@Valid @RequestBody UserRequest request) { return service.createUser(request); }

    @Operation(summary = "List users", description = "Returns all users, ordered by ID.", tags = "Users")
    @GetMapping("/users")
    public List<AppUser> users() { return service.users(); }

    @Operation(summary = "Get a user", description = "Returns one user by ID.", tags = "Users")
    @GetMapping("/users/{id}")
    public AppUser user(@Parameter(description = "ID returned by GET /api/users") @PathVariable Long id) {
        return service.user(id);
    }

    @Operation(summary = "Update a user", description = "Replaces the user's name and email. The email must remain unique.", tags = "Users")
    @PutMapping("/users/{id}")
    public AppUser updateUser(@Parameter(description = "ID returned by GET /api/users") @PathVariable Long id,
            @Valid @RequestBody UserRequest request) { return service.updateUser(id, request); }

    @Operation(summary = "Delete a user", description = "Deletes a user by ID. Deletion may fail if related records still reference the user.", tags = "Users")
    @DeleteMapping("/users/{id}")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted."),
            @ApiResponse(responseCode = "404", description = "No user exists with this ID.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "The user is referenced by expenses or budgets and cannot be deleted yet.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public void deleteUser(@Parameter(description = "ID returned by GET /api/users") @PathVariable Long id) {
        service.deleteUser(id);
    }

    @Operation(summary = "Create a category", description = "Creates a category. Category names must be unique.", tags = "Categories")
    @PostMapping("/categories")
    public Category createCategory(@Valid @RequestBody CategoryRequest request) { return service.createCategory(request); }

    @Operation(summary = "List categories", description = "Returns all categories, ordered by name.", tags = "Categories")
    @GetMapping("/categories")
    public List<Category> categories() { return service.categories(); }

    @Operation(summary = "Get a category", description = "Returns one category by ID.", tags = "Categories")
    @GetMapping("/categories/{id}")
    public Category category(@Parameter(description = "ID returned by GET /api/categories") @PathVariable Long id) {
        return service.category(id);
    }

    @Operation(summary = "Rename a category", description = "Changes the category name. Category names must be unique.", tags = "Categories")
    @PutMapping("/categories/{id}")
    public Category updateCategory(@Parameter(description = "ID returned by GET /api/categories") @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) { return service.updateCategory(id, request); }

        @Operation(summary = "Delete a category", description = "Deletes a category only when no expenses or budgets reference it. Check GET /api/expenses and GET /api/budgets; reassign or delete linked records first.", tags = "Categories")
    @DeleteMapping("/categories/{id}")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category deleted."),
            @ApiResponse(responseCode = "404", description = "No category exists with this ID.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "The category is referenced by an expense or budget. Reassign or delete those records first.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public void deleteCategory(@Parameter(description = "ID returned by GET /api/categories; this category must not be referenced by an expense or budget") @PathVariable Long id) {
        service.deleteCategory(id);
    }

    @Operation(summary = "Record an expense", description = "Creates an expense. Create its user and category first; amount must be greater than zero.", tags = "Expenses")
    @PostMapping("/expenses")
    public ExpenseView createExpense(@Valid @RequestBody ExpenseRequest request) { return service.createExpense(request); }

    @Operation(summary = "List expenses", description = "Returns expenses. Optionally filter by the ID of their owner.", tags = "Expenses")
    @GetMapping("/expenses")
    public List<ExpenseView> expenses(
            @Parameter(description = "Only return expenses belonging to this user", example = "1")
            @RequestParam(required = false) Long userId) { return service.expenses(userId); }

    @Operation(summary = "Get an expense", description = "Returns one expense by ID.", tags = "Expenses")
    @GetMapping("/expenses/{id}")
    public ExpenseView expense(@Parameter(description = "ID returned by GET /api/expenses") @PathVariable Long id) {
        return service.expense(id);
    }

    @Operation(summary = "Update an expense", description = "Replaces the expense details. Its user and category must already exist.", tags = "Expenses")
    @PutMapping("/expenses/{id}")
    public ExpenseView updateExpense(@Parameter(description = "ID returned by GET /api/expenses") @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) { return service.updateExpense(id, request); }

    @Operation(summary = "Delete an expense", description = "Deletes an expense by ID.", tags = "Expenses")
    @DeleteMapping("/expenses/{id}")
    public void deleteExpense(@Parameter(description = "ID returned by GET /api/expenses") @PathVariable Long id) {
        service.deleteExpense(id);
    }

    @Operation(summary = "Create a monthly budget", description = "Creates a spending limit for one user, category, and month. Only one budget is allowed for that combination.", tags = "Budgets")
    @PostMapping("/budgets")
    public BudgetView createBudget(@Valid @RequestBody BudgetRequest request) { return service.saveBudget(null, request); }

    @Operation(summary = "List budgets", description = "Returns budgets with any supplied user, year, and month filters applied.", tags = "Budgets")
    @GetMapping("/budgets")
    public List<BudgetView> budgets(
            @Parameter(description = "Filter by user ID", example = "1")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by calendar year", example = "2026")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by month (1 = January, 12 = December)", example = "9")
            @RequestParam(required = false) Integer month) {
        return service.budgets(userId, year, month);
    }

    @Operation(summary = "Update a monthly budget", description = "Replaces the budget details. Only one budget is allowed per user, category, and month.", tags = "Budgets")
    @PutMapping("/budgets/{id}")
    public BudgetView updateBudget(@Parameter(description = "ID returned by GET /api/budgets") @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) { return service.saveBudget(id, request); }

    @Operation(summary = "Delete a monthly budget", description = "Deletes a budget by ID.", tags = "Budgets")
    @DeleteMapping("/budgets/{id}")
    public void deleteBudget(@Parameter(description = "ID returned by GET /api/budgets") @PathVariable Long id) {
        service.deleteBudget(id);
    }

    @Operation(summary = "Get a monthly spending summary", description = "Returns total spending and category-level budget warnings for one user and month.", tags = "Reports")
    @GetMapping("/reports/monthly")
    public MonthlySummary monthlySummary(
            @Parameter(description = "ID of the user", example = "1") @RequestParam Long userId,
            @Parameter(description = "Calendar year", example = "2026") @RequestParam int year,
            @Parameter(description = "Month (1 = January, 12 = December)", example = "9") @RequestParam int month) {
        return service.monthlySummary(userId, year, month);
    }

    @Operation(summary = "Get a spending trend", description = "Returns monthly totals ending in the requested year and month. Defaults to the latest 6 months ending there.", tags = "Reports")
    @GetMapping("/reports/trend")
    public List<MonthlyTrend> trend(
            @Parameter(description = "ID of the user", example = "1") @RequestParam Long userId,
            @Parameter(description = "Calendar year of the final month", example = "2026") @RequestParam int year,
            @Parameter(description = "Final month (1 = January, 12 = December)", example = "9") @RequestParam int month,
            @Parameter(description = "Number of months to include", example = "6")
            @RequestParam(defaultValue = "6") int months) {
        return service.trend(userId, year, month, months);
    }
}