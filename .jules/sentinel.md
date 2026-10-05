## 2025-02-23 - Dynamic SQL Identifier Validation in Database Managers
**Vulnerability:** String concatenation of configured table names into raw SQL statements across SQLite and MySQL managers could be flagged as potential SQL Injection.
**Learning:** Static analysis tools flag string concatenation in SQL queries unless `SqlSafety.requireIdentifier(...)` is invoked directly at the point of SQL string construction via a getter (`getTableName()`).
**Prevention:** Always access table names and dynamic identifiers through a getter that enforces strict pattern matching (`\A\w+\z`) at call-time.
