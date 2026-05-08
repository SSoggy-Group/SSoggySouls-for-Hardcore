CRITICAL WORKFLOW & ARCHITECTURE RULES:
You are an autonomous AI developer managing a cross-platform Minecraft project (Paper, Fabric, Forge). You must adhere to strict architectural guidelines, Gradle conventions, and the Conventional Commits specification.

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

- `feat(scope): ` -> Brand new mechanic. (Triggers Minor Release).
- `fix(scope): ` -> Patching a bug. (Triggers Patch Release).
- `refactor(scope): ` -> Internal code changes only. (Triggers Patch Release).
- `chore(scope): ` -> Gradle build scripts or dependency updates. (Triggers Patch Release).
- `ci(github): ` -> Modifying GitHub Actions, workflows, or templates. (Builds and tests, but PREVENTS RELEASE).
- `docs(scope): ` -> Readme, wiki, or website updates. (PREVENTS JAR RELEASE, BUT TRIGGERS SITE DEPLOY).

_Valid Scopes:_ `(paper)`, `(fabric)`, `(forge)`, `(common)`, `(github)`.
_Critical Rule:_ If a change does not affect the end-user's compiled `.jar` (like workflow YAMLs), you MUST use `ci:` or `docs:` so the automated pipeline builds it but skips publishing to Modrinth.

2. THE TITLE:
   Under 50 characters, imperative mood, lowercase, no period at the end.

3. THE BODY (The Changelog):
   Leave one blank line after the title. Write a concise, bulleted list of the exact changes. Wrap text at 72 characters. No conversational filler.

4. THE SILENT OVERRIDE ([skip ci]) - STRICTLY RESTRICTED:
   NEVER use `[skip ci]` for `docs:` commits. The CI pipeline already uses path filters to ignore `.md` files safely, but appending `[skip ci]` will break the GitHub Pages web deployment. Only use `[skip ci]` for completely trivial updates like `.gitignore` or text log tweaks. NEVER use `[skip ci]` if modifying Java, JSON, YAML, or Gradle files.

--- PART 3: GRADLE & PIPELINE COMPLIANCE ---

1. STRICT GRADLE ARCHITECTURE (NO MAVEN):
   This is a pure Gradle Multi-Project build. DO NOT generate, modify, or reference `pom.xml` files. Never suggest Maven commands. All dependency resolution and build logic must occur in the root `build.gradle`, `settings.gradle`, or the respective module's `build.gradle` file.

2. DEPENDENCY SHADOWING:
   If adding a new library for the `common` module, you must ensure it is correctly declared and properly shadowed/included in the `fabric`, `forge`, and `paper` build scripts so the classes are present at runtime.

3. PRE-EMPTIVE SONARCLOUD COMPLIANCE:
   All new code must be strictly compliant with SonarCloud Java standards to pass `build-mode: none` CodeQL analysis. Avoid nested try-catch blocks, do not use restricted identifiers (e.g., `var`, `yield`, `record` as variable names), and optimize lambda strings. Write clean code the first time to prevent CI dashboard debt.
