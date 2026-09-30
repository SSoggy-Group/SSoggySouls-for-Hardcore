
## 2025-05-18 - Avoid Raw UUID Exposure in Database Exception Logs
**Vulnerability:** Raw UUID logged directly during database query exceptions in `AbstractDatabaseManager.java`.
**Learning:** Logging unique user identifiers (UUIDs) directly in warning log output on database failures could expose sensitive context or target identifier info to log aggregators.
**Prevention:** Use static generic message strings (e.g., "Failed to get player by UUID") when logging exception warnings for query operations.
