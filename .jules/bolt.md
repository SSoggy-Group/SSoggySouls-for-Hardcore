## 2026-05-26 - Tab Completion Performance
**Learning:** In Bukkit/Paper, `Bukkit.getOnlinePlayers().stream().map(...)` inside `onTabComplete` is an anti-pattern. Tab completion fires on every keystroke, so using Java Streams over the entire player list generates massive amounts of short-lived objects and GC pressure, leading to responsiveness issues during command entry.
**Action:** Always use `TabCompleteUtil.getOnlinePlayerNames()` which internally uses an allocation-free loop and `String.regionMatches()`, avoiding stream wrappers completely.
