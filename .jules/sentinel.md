## 2026-05-06 - [Fix OutOfMemoryError reading admin log]
**Vulnerability:** `Files.readAllLines()` was used to read the admin log file, which can grow arbitrarily large and cause an OOM error.
**Learning:** Admin log files should always be read using a streaming approach if only the most recent N lines are needed.
**Prevention:** Use a `Stream` via `Files.lines()` combined with a `Deque` to store only the last N lines.
## 2024-05-07 - [Prevent potential SQL Injection in schema generation]
**Vulnerability:** In database managers (`MySQLManager`, `SQLiteManager`), dynamic DDL methods like `createMetadataTableIfNeeded` directly concatenated string arguments (`metaTable`) into `CREATE TABLE` commands. Even though the table name was internally hardcoded, lack of validation poses an SQL injection risk if the method's signature or usage expands.
**Learning:** Defensive programming requires validating all dynamic identifiers used in non-parameterizable SQL statements (DDL).
**Prevention:** Apply the existing `isValidIdentifier` (whitelisting regex `^\w+$`) check to table and column name parameters before they are concatenated into SQL queries.

## 2025-02-28 - [Network Payload Denial of Service]
**Vulnerability:** The `BungeeConnectPayload.CODEC` in the Forge `ServerTransferUtil` was directly allocating a byte array using the unbounded `buf.readableBytes()` method.
**Learning:** This is a classic Denial of Service (DoS) vulnerability. A malicious or compromised client could send a crafted packet advertising an enormous size, causing the server to attempt to allocate massive memory chunks, leading to `OutOfMemoryError` and server crashes.
**Prevention:** Always enforce a strict maximum length (e.g., `1024` bytes) when reading variable-length data structures from network buffers before allocating memory or processing the data.
