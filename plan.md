1. **Identify the vulnerability:** In `paper/src/main/java/org/ssoggy/ssoggysouls/hrm/dlc/commands/SocialCommand.java`, user-controlled data (usernames) is passed directly to Kyori MiniMessage format strings via `targetPlayer.getName()` and `ctx.player.getName()`. This is a MiniMessage Injection vulnerability (similar to XSS but for Minecraft chat components), allowing malicious users to format chat, insert fake clickable links, or spoof messages.
2. **Implement the fix:**
    - Use `net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().escapeTags(...)` to sanitize `targetPlayer.getName()` and `ctx.player.getName()` before inserting them into string concatenated `RPCommandOutput` messages in `handleBlock`, `handleRevoke`, `handleGrant`, etc.
    - Specifically, update `ctx.output.message = "You already blocked " + ctx.targetPlayer.getName();` and all similar usages in `SocialCommand.java` to escape the names first.
3. **Verify:** Compile and run tests for the paper module to ensure the changes are syntactically correct.
4. **Pre-commit:** Run pre-commit instructions.
5. **Submit:** Submit with Conventional Commits, using the Sentinel persona.
