## 2025-10-05 - Data Exposure Prevention in Debug Logging
**Vulnerability:** Logging raw domain objects (`PlayerData`) when debug mode is enabled exposes sensitive player profile metadata (e.g. username, lives count, state) to debug log outputs.
**Learning:** `PlayerData.toString()` includes state information that can inadvertently leak PII or game state to console log aggregators when debug logging is active.
**Prevention:** Avoid logging complete domain objects in debug statements; log only non-sensitive identifiers (such as `UUID`) required for tracing and troubleshooting.
