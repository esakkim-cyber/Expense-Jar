package com.example.expense_jar;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "ExpenseJar API",
				version = "1.0",
				description = "Track expenses, monthly budgets, and spending reports. Quick start: create a user, create a category, then use their IDs to add expenses or budgets. Dates use YYYY-MM-DD."
		),
		tags = {
				@Tag(name = "Users", description = "People who own expenses and budgets."),
				@Tag(name = "Categories", description = "Labels used to organize expenses and budgets."),
				@Tag(name = "Expenses", description = "Create and manage individual spending records."),
				@Tag(name = "Budgets", description = "Monthly spending limits by user and category."),
				@Tag(name = "Reports", description = "Monthly summaries and spending trends.")
		}
)
public class ExpenseJarApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpenseJarApplication.class, args);
	}

}
