# ExpenseJar

ExpenseJar is a Spring Boot REST API for recording categorized expenses, setting monthly category budgets, and viewing monthly totals and spending trends.

## Run

Requirements: Java 21 and MySQL 8 or compatible.

The default connection is `jdbc:mysql://localhost:3306/expense_jar` with username `root` and password `root`. The application creates the database if it does not exist and updates the tables at startup. Override the defaults with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.

On Windows:

```powershell
.\mvnw.cmd -s .maven-mirror-settings.xml -Dspring-boot.run.arguments=--server.port=8081 spring-boot:run
```

Swagger UI: <http://localhost:8081/swagger-ui/index.html>
OpenAPI JSON: <http://localhost:8081/v3/api-docs>

Run tests with:

```powershell
.\mvnw.cmd test
```

## API

All endpoints are under `/api`. JSON validation errors return HTTP 400, missing records return HTTP 404, and duplicate/conflicting records return HTTP 409.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST`, `GET` | `/users` | Create/list users |
| `GET`, `PUT`, `DELETE` | `/users/{id}` | Read/update/delete a user |
| `POST`, `GET` | `/categories` | Create/list categories |
| `GET`, `PUT`, `DELETE` | `/categories/{id}` | Read/update/delete a category |
| `POST`, `GET` | `/expenses` | Create/list expenses; list accepts optional `userId` |
| `GET`, `PUT`, `DELETE` | `/expenses/{id}` | Read/update/delete an expense |
| `POST`, `GET` | `/budgets` | Create/list monthly budgets; list accepts `userId`, `year`, and `month` filters |
| `PUT`, `DELETE` | `/budgets/{id}` | Update/delete a budget |
| `GET` | `/reports/monthly?userId=1&year=2026&month=9` | Monthly category totals and budget warnings |
| `GET` | `/reports/trend?userId=1&year=2026&month=9&months=6` | Monthly totals ending in the requested month |

Create users and categories before adding expenses or budgets. Expense amounts must be positive. A category warning is `true` when its monthly spending reaches at least 90% of its budget.
