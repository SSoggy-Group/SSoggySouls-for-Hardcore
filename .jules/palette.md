## 2024-05-07 - Accessible Navigation Active States
**Learning:** Using a visual class like `is-active` is insufficient for screen readers to understand which page is currently being viewed in a navigation menu. They require an explicit semantic attribute to indicate the current context.
**Action:** Always pair visual active classes with `aria-current="page"` on the active navigation link to provide full context to assistive technologies.
## 2024-05-08 - Skip-to-Content Link Visibility
**Learning:** A "skip-to-content" link was present in the HTML but lacked styling, meaning it wasn't visible when focused via keyboard navigation. This defeats the purpose of the skip link for sighted keyboard users.
**Action:** When adding or maintaining a skip-to-content link, ensure it has specific `:focus` styles that make it visible and easily readable when receiving keyboard focus.
## 2026-05-10 - [Interactive CLI Help Menus]
**Learning:** Admin help menus in legacy command systems (like Bukkit/Paper) are often static text, forcing users to manually type complex subcommands.
**Action:** Upgrade static text help menus to use Rich Components with `ClickEvent.suggestCommand()` and `HoverEvent.showText()` to reduce friction and typos for command-line users.
