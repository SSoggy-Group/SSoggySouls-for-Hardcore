CRITICAL WORKFLOW & ARCHITECTURE RULES:
You are an autonomous AI developer managing a cross-platform Minecraft project (Paper, Fabric, Forge). You must adhere to strict architectural guidelines and the Conventional Commits specification.

--- PART 1: ARCHITECTURE & CODE EXECUTION ---

1. THE COMMON MODULE:
Core logic, data structures, and database management (MySQL/SQLite) belong in the `common/` module. If you write a new feature here, you MUST ensure it is properly hooked into the `fabric/`, `forge/`, and `paper/` modules if required.

2. CROSS-PLATFORM PARITY VS. SPECIFICITY:
Before modifying code, you must analyze if the issue is universal or platform-specific.
- If adding a universal feature, apply it to the `common/` module.
- If a bug or feature is DEFINITELY platform-specific (e.g., a Forge Event Bus crash or a Fabric Entrypoint issue), do NOT attempt to apply the fix to all platforms. Write the fix ONLY in the respective loader's module.

3. MOBILE WORKFLOW DEPENDENCY:
The user is managing this repository remotely. You must completely finish your code modifications across all required folders before generating the commit. Do not leave "TODO" comments or expect the user to manually sync files between loaders.

--- PART 2: COMMIT MESSAGES & CI/CD ---

1. THE PREFIX & SCOPE (Choose exactly one):
- `feat(scope): ` -> Brand new mechanic. (Minor Release).
- `fix(scope): ` -> Patching a bug. (Patch Release).
- `refactor(scope): ` -> Internal code changes only.
- `chore(scope): ` -> Build scripts or actions.
*Valid Scopes:* `(paper)`, `(fabric)`, `(forge)`, `(common)`, `(github)`. If the change breaks older versions, add an exclamation mark: `feat!(common): `.

2. THE TITLE:
Under 50 characters, imperative mood, lowercase, no period at the end.

3. THE BODY (The Changelog):
Leave one blank line after the title. Write a concise, bulleted list of the exact changes. Wrap text at 72 characters. No conversational filler.

4. THE SILENT OVERRIDE ([skip ci]):
If modifying ONLY documentation (.md), logs, or comments, append ` [skip ci]` to the title (e.g., `docs: update readme [skip ci]`). NEVER use this if modifying Java, JSON, YAML, or build
 scripts.
