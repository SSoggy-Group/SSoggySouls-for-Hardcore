## 2024-05-07 - Accessible Navigation Active States
**Learning:** Using a visual class like `is-active` is insufficient for screen readers to understand which page is currently being viewed in a navigation menu. They require an explicit semantic attribute to indicate the current context.
**Action:** Always pair visual active classes with `aria-current="page"` on the active navigation link to provide full context to assistive technologies.
## 2024-05-08 - Skip-to-Content Link Visibility
**Learning:** A "skip-to-content" link was present in the HTML but lacked styling, meaning it wasn't visible when focused via keyboard navigation. This defeats the purpose of the skip link for sighted keyboard users.
**Action:** When adding or maintaining a skip-to-content link, ensure it has specific `:focus` styles that make it visible and easily readable when receiving keyboard focus.
## 2026-05-10 - [Interactive CLI Help Menus]
**Learning:** Admin help menus in legacy command systems (like Bukkit/Paper) are often static text, forcing users to manually type complex subcommands.
**Action:** Upgrade static text help menus to use Rich Components with `ClickEvent.suggestCommand()` and `HoverEvent.showText()` to reduce friction and typos for command-line users.
## 2026-05-10 - [Interactive CLI Usage Error Catching]\n**Learning:** Implementing interactive components (like clickable auto-fill suggestions) on error handling messages transforms static frustration points into helpful recovery paths.\n**Action:** Use `CommandUtil.sendInteractiveUsage()` when catching command formatting errors in Bukkit to allow users to quickly fix their typos with a single click.
## 2026-05-15 - [Interactive CLI Error Messages]
**Learning:** When users mistype a command (like missing an argument in /revive), the default static error message forces them to re-type the whole thing. By using Kyori Adventure Rich Components (via CommandUtil.sendInteractiveUsage) for the usage error message, they can just click the error text to auto-fill the command in their chat bar.
**Action:** Update static command error messages to use sendInteractiveUsage so that mistakes are easily correctable.
## 2026-05-15 - [Interactive CLI Argument Error Catching]
**Learning:** Static argument validation error messages (like "Invalid number" or "Out of bounds") force users to completely re-type the command. By replacing `sendMessage` with `CommandUtil.sendInteractiveUsage()` for these errors, users can simply click the error message to auto-fill the command and correct their mistake, significantly reducing friction.
**Action:** When catching `NumberFormatException` or validating numeric bounds in command arguments, use `sendInteractiveUsage()` with a pre-filled suggestion (e.g. `/psetlives <player> `) to create a helpful recovery path instead of a static frustration point.
## 2026-05-15 - [Interactive CLI Number Parsing Errors]
**Learning:** Number parsing errors (e.g., NumberFormatException) in legacy command systems often default to static "Invalid number" messages. By refactoring methods like `parseIntOrError` to accept a base `suggestCmd`, these static errors can be upgraded to use `CommandUtil.sendInteractiveUsage`, allowing users to quickly correct invalid numeric inputs via a clickable chat component.
**Action:** Always provide the base command context when catching input format errors to build actionable auto-fill error suggestions.

## 2026-05-18 - [Interactive CLI Coordinate Copying]
**Learning:** Players frequently need to share or navigate to coordinates displayed in chat (like death locations in obituaries), but manually transcribing them into waypoints or chat is tedious and error-prone.
**Action:** When displaying coordinates in chat via MiniMessage, wrap them in `<click:copy_to_clipboard:'X Y Z'>` and a `<hover>` prompt to allow instant, frictionless copying.
## 2026-05-29 - [Interactive CLI Coordinate Copying]
**Learning:** Players frequently need to navigate to locations displayed in chat (like death locations in death messages), but manually transcribing coordinates is tedious and error-prone.
**Action:** When displaying coordinates in chat via MiniMessage, wrap them in `<click:copy_to_clipboard:'X Y Z'>` and a `<hover>` prompt to allow instant, frictionless copying for players.
## 2026-05-30 - [Interactive CLI Coordinate Copying]
**Learning:** Players frequently need to navigate to locations displayed in chat (like death locations in death messages), but manually transcribing coordinates is tedious and error-prone.
**Action:** When displaying coordinates in chat via MiniMessage, wrap them in `<click:copy_to_clipboard:'X Y Z'>` and a `<hover>` prompt to allow instant, frictionless copying for players.
## 2026-06-02 - [Configurable Console Error Messages]
**Learning:** Hardcoding standard API error strings (like "Only players can use this command") breaks consistency for server administrators running commands via console.\n**Action:** When migrating hardcoded console-only rejection messages, always use MessageUtil.get("command-only-players") to allow localization.
## 2026-06-03 - [Interactive CLI Number Parsing Errors with RPCommandOutput]
**Learning:** In the `RPConfigCommand` class, number parsing errors (e.g., `NumberFormatException` when parsing timer values) fall back to static error messages. Since this command uses Kyori Adventure MiniMessage implicitly by resolving custom UI strings via `RPCommandOutput`, we can't use the standard `CommandUtil.sendInteractiveUsage` method, which assumes Legacy (`&`) codes and Bukkit CommandSender execution immediately.
**Action:** Replace static error assignments in `RPCommandOutput.message` directly with MiniMessage-compatible `<click:suggest_command:...>` and `<hover:show_text:...>` components so that players can interactively recover from invalid timer setups without re-typing.
## 2026-06-03 - [Interactive CLI Time Formatting Errors]
**Learning:** Static time format parsing errors force users to completely re-type complex commands (like `/psadmin grace set <player> <time>`). By replacing `sendMessage` with `CommandUtil.sendInteractiveUsage()` for these errors, users can click the error message to auto-fill the command with the context pre-filled, significantly reducing friction.
**Action:** When validating time format arguments and catching invalid inputs, use `sendInteractiveUsage()` with a pre-filled suggestion (e.g. `/psadmin grace set <player> `) to create a helpful recovery path instead of a static frustration point.
## 2026-06-11 - [Interactive CLI MiniMessage Errors]
**Learning:** Legacy CLI systems often fail silently when commands are executed from the console but meant only for players. Hardcoded strings for error messages (like `cmdSender.sendMessage("This command can only be run by a player.")`) break consistency across localized servers. Furthermore, when users misconfigure arguments, standard static usage errors force complete retyping of the command, increasing friction.
**Action:** When commands are restricted to players, always fallback to localized strings like `MessageUtil.get("command-only-players")`. For static usage errors (like `/trust <action> [player]`), leverage Kyori Adventure MiniMessage components (`<click:suggest_command:'...'><hover:show_text:'...'>`) to provide clickable auto-fill recovery paths.
