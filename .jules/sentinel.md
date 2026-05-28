## 2026-05-06 - [Fix OutOfMemoryError reading admin log]
**Vulnerability:** `Files.readAllLines()` was used to read the admin log file, which can grow arbitrarily large and cause an OOM error.
**Learning:** Admin log files should always be read using a streaming approach if only the most recent N lines are needed.
**Prevention:** Use a `Stream` via `Files.lines()` combined with a `Deque` to store only the last N lines.
## 2024-05-07 - [Prevent potential SQL Injection in schema generation]
**Vulnerability:** In database managers (`MySQLManager`, `SQLiteManager`), dynamic DDL methods like `createMetadataTableIfNeeded` directly concatenated string arguments (`metaTable`) into `CREATE TABLE` commands. Even though the table name was internally hardcoded, lack of validation poses an SQL injection risk if the method's signature or usage expands.
**Learning:** Defensive programming requires validating all dynamic identifiers used in non-parameterizable SQL statements (DDL).
**Prevention:** Apply the existing `isValidIdentifier` (whitelisting regex `^\w+$`) check to table and column name parameters before they are concatenated into SQL queries.

## 2025-05-15 - Prevent Log Forging / CRLF Injection in Custom Loggers
**Vulnerability:** Unsanitized user inputs (e.g., `sender`, `action` parameters) were passed directly to `PrintWriter.println()` and `Logger.info()` in `AdminLogger.java` across all module implementations (`paper`, `fabric`, `forge`, `neoforge`).
**Learning:** This exposes the application to Log Forging (CRLF Injection), where a malicious user could supply inputs containing newline (`\n`) and carriage return (`\r`) characters to insert fake log entries, obscuring legitimate audit trails or tricking log parsing systems.
**Prevention:** Always implement an input sanitization step—such as replacing `\n` and `\r` with an underscore `_`—before writing user-controlled strings to application or system logs.
## 2026-05-28 - [Prevent DoS via unbounded array allocation in network payload]
**Vulnerability:** In `ServerTransferUtil` across `fabric`, `forge`, and `neoforge` modules, `new byte[buf.readableBytes()]` was being called when decoding a custom network payload. A malicious client could send an extremely large payload length, causing the server to allocate a massive byte array and crash with an OutOfMemoryError.
**Learning:** `buf.readableBytes()` is user-controlled input when decoding packets and must be explicitly validated against a sane maximum limit before being used for memory allocation.
**Prevention:** Always enforce a strict maximum length (e.g., 1024 bytes) when reading variable-length data structures from network buffers before allocating memory.
