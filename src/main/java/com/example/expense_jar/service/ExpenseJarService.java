package com.example.expense_jar.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.expense_jar.api.ApiModels.BudgetRequest;
import com.example.expense_jar.api.ApiModels.BudgetView;
import com.example.expense_jar.api.ApiModels.CategoryRequest;
import com.example.expense_jar.api.ApiModels.CategorySummary;
import com.example.expense_jar.api.ApiModels.ExpenseRequest;
import com.example.expense_jar.api.ApiModels.ExpenseView;
import com.example.expense_jar.api.ApiModels.MonthlySummary;
import com.example.expense_jar.api.ApiModels.MonthlyTrend;
import com.example.expense_jar.api.ApiModels.UserRequest;
import com.example.expense_jar.model.AppUser;
import com.example.expense_jar.model.Budget;
import com.example.expense_jar.model.Category;
import com.example.expense_jar.model.Expense;
import com.example.expense_jar.repository.BudgetRepository;
import com.example.expense_jar.repository.CategoryRepository;
import com.example.expense_jar.repository.ExpenseRepository;
import com.example.expense_jar.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ExpenseJarService {
    private static final BigDecimal NINETY = new BigDecimal("90");

    private final UserRepository users;
    private final CategoryRepository categories;
    private final ExpenseRepository expenses;
    private final BudgetRepository budgets;

    public ExpenseJarService(UserRepository users, CategoryRepository categories,
                             ExpenseRepository expenses, BudgetRepository budgets) {
        this.users = users;
        this.categories = categories;
        this.expenses = expenses;
        this.budgets = budgets;
    }

    public List<AppUser> users() { return users.findAll(Sort.by("id")); }
    public AppUser user(Long id) { return users.findById(id).orElseThrow(() -> missing("User", id)); }
    public AppUser createUser(UserRequest request) {
        String email = request.email().trim();
        if (users.existsByEmailIgnoreCase(email)) throw conflict("Email is already registered");
        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setEmail(email);
        return users.save(user);
    }
    public AppUser updateUser(Long id, UserRequest request) {
        AppUser user = user(id);
        String email = request.email().trim();
        if (!user.getEmail().equalsIgnoreCase(email) && users.existsByEmailIgnoreCase(email))
            throw conflict("Email is already registered");
        user.setName(request.name().trim());
        user.setEmail(email);
        return user;
    }
    public void deleteUser(Long id) { users.delete(user(id)); }

    public List<Category> categories() { return categories.findAll(Sort.by("name")); }
    public Category category(Long id) { return categories.findById(id).orElseThrow(() -> missing("Category", id)); }
    public Category createCategory(CategoryRequest request) {
        String name = request.name().trim();
        if (categories.existsByNameIgnoreCase(name)) throw conflict("Category already exists");
        Category category = new Category();
        category.setName(name);
        return categories.save(category);
    }
    public Category updateCategory(Long id, CategoryRequest request) {
        Category category = category(id);
        String name = request.name().trim();
        if (!category.getName().equalsIgnoreCase(name) && categories.existsByNameIgnoreCase(name))
            throw conflict("Category already exists");
        category.setName(name);
        return category;
    }
    public void deleteCategory(Long id) { categories.delete(category(id)); }

    public List<ExpenseView> expenses(Long userId) {
        List<Expense> result = userId == null ? expenses.findAll(Sort.by(Sort.Direction.DESC, "date")) : expenses.findByUserId(userId);
        if (userId != null) user(userId);
        return result.stream().map(this::expenseView).toList();
    }
    public ExpenseView expense(Long id) { return expenseView(expenses.findById(id).orElseThrow(() -> missing("Expense", id))); }
    public ExpenseView createExpense(ExpenseRequest request) {
        Expense expense = new Expense();
        applyExpense(expense, request);
        return expenseView(expenses.save(expense));
    }
    public ExpenseView updateExpense(Long id, ExpenseRequest request) {
        Expense expense = expenses.findById(id).orElseThrow(() -> missing("Expense", id));
        applyExpense(expense, request);
        return expenseView(expense);
    }
    public void deleteExpense(Long id) { expenses.delete(expenses.findById(id).orElseThrow(() -> missing("Expense", id))); }

    private void applyExpense(Expense expense, ExpenseRequest request) {
        expense.setAmount(request.amount());
        expense.setDescription(request.description() == null ? null : request.description().trim());
        expense.setDate(request.date());
        expense.setUser(user(request.userId()));
        expense.setCategory(category(request.categoryId()));
    }

    public List<BudgetView> budgets(Long userId, Integer year, Integer month) {
        List<Budget> found = userId == null ? budgets.findAll() : budgets.findByUserId(userId);
        if (userId != null) user(userId);
        return found.stream().filter(b -> year == null || b.getYear() == year)
                .filter(b -> month == null || b.getMonth() == month).map(this::budgetView).toList();
    }
    public BudgetView saveBudget(Long id, BudgetRequest request) {
        AppUser user = user(request.userId());
        Category category = category(request.categoryId());
        Budget budget;
        if (id == null) {
            if (budgets.findByUserIdAndCategoryIdAndYearAndMonth(user.getId(), category.getId(), request.year(), request.month()).isPresent())
                throw conflict("A budget already exists for this user, category, and month");
            budget = new Budget();
        } else {
            budget = budgets.findById(id).orElseThrow(() -> missing("Budget", id));
            Optional<Budget> duplicate = budgets.findByUserIdAndCategoryIdAndYearAndMonth(user.getId(), category.getId(), request.year(), request.month());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) throw conflict("A budget already exists for this user, category, and month");
        }
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmount(request.amount());
        budget.setYear(request.year());
        budget.setMonth(request.month());
        return budgetView(budgets.save(budget));
    }
    public void deleteBudget(Long id) { budgets.delete(budgets.findById(id).orElseThrow(() -> missing("Budget", id))); }

    public MonthlySummary monthlySummary(Long userId, int year, int month) {
        user(userId);
        validatePeriod(year, month);
        YearMonth target = YearMonth.of(year, month);
        List<Expense> monthExpenses = expenses.findByUserIdAndDateBetween(userId, target.atDay(1), target.atEndOfMonth());
        Map<Long, BigDecimal> spent = monthExpenses.stream().collect(Collectors.groupingBy(
                e -> e.getCategory().getId(), Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
        Map<Long, Budget> monthBudgets = budgets.findByUserId(userId).stream()
                .filter(b -> b.getYear() == year && b.getMonth() == month)
                .collect(Collectors.toMap(b -> b.getCategory().getId(), Function.identity()));
        Set<Long> ids = new LinkedHashSet<>();
        ids.addAll(spent.keySet());
        ids.addAll(monthBudgets.keySet());
        List<CategorySummary> breakdown = ids.stream().map(id -> {
            Category category = category(id);
            BigDecimal amountSpent = spent.getOrDefault(id, BigDecimal.ZERO);
            Budget budget = monthBudgets.get(id);
            BigDecimal limit = budget == null ? BigDecimal.ZERO : budget.getAmount();
            BigDecimal percent = percentage(amountSpent, limit);
            return new CategorySummary(id, category.getName(), amountSpent, limit, percent, limit.signum() > 0 && percent.compareTo(NINETY) >= 0);
        }).toList();
        BigDecimal total = monthExpenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MonthlySummary(userId, year, month, total, breakdown);
    }

    public List<MonthlyTrend> trend(Long userId, int year, int month, int months) {
        user(userId);
        if (months < 1 || months > 24) throw new ResponseStatusException(BAD_REQUEST, "months must be between 1 and 24");
        validatePeriod(year, month);
        YearMonth end = YearMonth.of(year, month);
        List<MonthlyTrend> result = new ArrayList<>();
        for (int offset = months - 1; offset >= 0; offset--) {
            YearMonth target = end.minusMonths(offset);
            BigDecimal total = expenses.findByUserIdAndDateBetween(userId, target.atDay(1), target.atEndOfMonth()).stream()
                    .map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            result.add(new MonthlyTrend(target.getYear(), target.getMonthValue(), total));
        }
        return result;
    }

    private BudgetView budgetView(Budget budget) {
        YearMonth target = YearMonth.of(budget.getYear(), budget.getMonth());
        BigDecimal spent = expenses.findByUserIdAndDateBetween(budget.getUser().getId(), target.atDay(1), target.atEndOfMonth()).stream()
                .filter(e -> e.getCategory().getId().equals(budget.getCategory().getId()))
                .map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal percent = percentage(spent, budget.getAmount());
        return new BudgetView(budget.getId(), budget.getUser().getId(), budget.getCategory().getId(), budget.getCategory().getName(),
                budget.getYear(), budget.getMonth(), budget.getAmount(), spent, percent, percent.compareTo(NINETY) >= 0);
    }

    private ExpenseView expenseView(Expense expense) {
        return new ExpenseView(expense.getId(), expense.getAmount(), expense.getDescription(), expense.getDate(),
                expense.getUser().getId(), expense.getCategory().getId(), expense.getCategory().getName());
    }

    private BigDecimal percentage(BigDecimal spent, BigDecimal budget) {
        return budget.signum() == 0 ? BigDecimal.ZERO : spent.multiply(BigDecimal.valueOf(100)).divide(budget, 2, RoundingMode.HALF_UP);
    }

    private void validatePeriod(int year, int month) {
        if (year < 2000 || year > 9999 || month < 1 || month > 12)
            throw new ResponseStatusException(BAD_REQUEST, "year must be between 2000 and 9999 and month must be between 1 and 12");
    }

    private ResponseStatusException missing(String type, Long id) { return new ResponseStatusException(NOT_FOUND, type + " " + id + " was not found"); }
    private ResponseStatusException conflict(String message) { return new ResponseStatusException(CONFLICT, message); }
}