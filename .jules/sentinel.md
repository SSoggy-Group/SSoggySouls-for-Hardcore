## 2026-05-06 - [Fix OutOfMemoryError reading admin log]
**Vulnerability:** `Files.readAllLines()` was used to read the admin log file, which can grow arbitrarily large and cause an OOM error.
**Learning:** Admin log files should always be read using a streaming approach if only the most recent N lines are needed.
**Prevention:** Use a `Stream` via `Files.lines()` combined with a `Deque` to store only the last N lines.
## 2024-05-07 - [Prevent potential SQL Injection in schema generation]
**Vulnerability:** In database managers (`MySQLManager`, `SQLiteManager`), dynamic DDL methods like `createMetadataTableIfNeeded` directly concatenated string arguments (`metaTable`) into `CREATE TABLE` commands. Even though the table name was internally hardcoded, lack of validation poses an SQL injection risk if the method's signature or usage expands.
**Learning:** Defensive programming requires validating all dynamic identifiers used in non-parameterizable SQL statements (DDL).
**Prevention:** Apply the existing `isValidIdentifier` (whitelisting regex `^\w+$`) check to table and column name parameters before they are concatenated into SQL queries.
## 2026-05-10 - [Prevent JDBC Connection URL Injection]
**Vulnerability:** MySQL JDBC connection parameters like `host`, `dbName`, and `sslMode` were read from the configuration and directly appended into the JDBC URL without validation. This could allow an attacker to inject arbitrary JDBC parameters (e.g., `autoDeserialize=true` or `allowLoadLocalInfile=true`), potentially leading to remote code execution or arbitrary file read vulnerabilities.
**Learning:** All user-controlled parameters, even those coming from configuration files, must be validated before being concatenated into a JDBC connection string.
**Prevention:** Implement strict regex validation (e.g., allowing only alphanumeric characters, periods, hyphens, and underscores) for JDBC connection parameters before appending them to the JDBC URL.
