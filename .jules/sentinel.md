## 2026-05-06 - [Fix OutOfMemoryError reading admin log]
**Vulnerability:** `Files.readAllLines()` was used to read the admin log file, which can grow arbitrarily large and cause an OOM error.
**Learning:** Admin log files should always be read using a streaming approach if only the most recent N lines are needed.
**Prevention:** Use a `Stream` via `Files.lines()` combined with a `Deque` to store only the last N lines.
## 2024-05-07 - [Prevent potential SQL Injection in schema generation]
**Vulnerability:** In database managers (`MySQLManager`, `SQLiteManager`), dynamic DDL methods like `createMetadataTableIfNeeded` directly concatenated string arguments (`metaTable`) into `CREATE TABLE` commands. Even though the table name was internally hardcoded, lack of validation poses an SQL injection risk if the method's signature or usage expands.
**Learning:** Defensive programming requires validating all dynamic identifiers used in non-parameterizable SQL statements (DDL).
**Prevention:** Apply the existing `isValidIdentifier` (whitelisting regex `^\w+$`) check to table and column name parameters before they are concatenated into SQL queries.

## 2025-02-28 - [JDBC Connection URL Injection]
**Vulnerability:** The MySQLManager was dynamically building its JDBC url connection string directly from the unvalidated plugin configuration file inputs like database host and database name.
**Learning:** This exposed the system to JDBC Connection URL injection. If a malicious attacker had the ability to edit the `config.yml` or the environment variables configuring the plugin, they could append arbitrary query parameters (like `?autoDeserialize=true` or `&allowLoadLocalInfile=true`) into the database configuration. Depending on the MySQL driver version, these can trigger severe RCE or Arbitrary File Read exploits when the database connection is initialized.
**Prevention:** Always parse, validate, and sanitize connection properties using a whitelist of allowed characters (e.g., regex `^[a-zA-Z0-9_.\-]+$`) BEFORE concatenating them into a JDBC string. Alternatively, apply `addDataSourceProperty` methods explicitly for each configuration value via HikariCP instead of building the JDBC URL as a raw string where possible.
