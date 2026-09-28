package com.example.expense_jar.repository;

import com.example.expense_jar.model.Expense;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    List<Expense> findByUserId(Long userId);
}