package com.example.expense_jar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseJarApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void expenseOverNinetyPercentOfBudgetAppearsInMonthlyReportAndTrend() throws Exception {
		String userResponse = mockMvc.perform(post("/api/users")
				.contentType("application/json")
				.content("""
					{"name":"Sam Student","email":"sam@example.com"}
					"""))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		JsonNode user = objectMapper.readTree(userResponse);
		long userId = user.get("id").asLong();

		String categoryResponse = mockMvc.perform(post("/api/categories")
				.contentType("application/json")
				.content("""
					{"name":"Food"}
					"""))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		long categoryId = objectMapper.readTree(categoryResponse).get("id").asLong();

		mockMvc.perform(post("/api/budgets")
				.contentType("application/json")
				.content("""
					{"amount":0,"year":2026,"month":9,"userId":%d,"categoryId":%d}
					""".formatted(userId, categoryId)))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/budgets")
				.contentType("application/json")
				.content("""
					{"amount":100,"year":2026,"month":9,"userId":%d,"categoryId":%d}
					""".formatted(userId, categoryId)))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/budgets")
				.param("userId", Long.toString(userId)).param("year", "2026").param("month", "9"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].budget").value(100));

		mockMvc.perform(post("/api/expenses")
				.contentType("application/json")
				.content("""
					{"amount":91,"description":"Groceries","date":"2026-09-15","userId":%d,"categoryId":%d}
					""".formatted(userId, categoryId)))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/reports/monthly")
				.param("userId", Long.toString(userId)).param("year", "2026").param("month", "9"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalSpent").value(91))
			.andExpect(jsonPath("$.categories[0].warning").value(true));

		mockMvc.perform(get("/api/reports/trend")
				.param("userId", Long.toString(userId)).param("year", "2026").param("month", "9").param("months", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].month").value(8))
			.andExpect(jsonPath("$[1].totalSpent").value(91));

		mockMvc.perform(post("/api/expenses")
				.contentType("application/json")
				.content("""
					{"amount":-1,"description":"Invalid","date":"2026-09-15","userId":%d,"categoryId":%d}
					""".formatted(userId, categoryId)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("validation_error"));
	}

}
