## 2024-05-07 - Accessible Navigation Active States
**Learning:** Using a visual class like `is-active` is insufficient for screen readers to understand which page is currently being viewed in a navigation menu. They require an explicit semantic attribute to indicate the current context.
**Action:** Always pair visual active classes with `aria-current="page"` on the active navigation link to provide full context to assistive technologies.
## 2024-05-08 - Skip-to-Content Link Visibility
**Learning:** A "skip-to-content" link was present in the HTML but lacked styling, meaning it wasn't visible when focused via keyboard navigation. This defeats the purpose of the skip link for sighted keyboard users.
**Action:** When adding or maintaining a skip-to-content link, ensure it has specific `:focus` styles that make it visible and easily readable when receiving keyboard focus.
## 2024-05-09 - Sidebar Navigation Context
**Learning:** A `<nav>` element without an accessible name can be confusing for screen reader users, especially when there are multiple navigation landmarks on a page. Linking the `<nav>` to its visual title using `aria-labelledby` provides crucial context about what the navigation section is for (e.g., "Documentation navigation").
**Action:** Always provide an accessible name for `<nav>` elements, either by using `aria-label` or by associating it with a visible heading using `aria-labelledby`.
