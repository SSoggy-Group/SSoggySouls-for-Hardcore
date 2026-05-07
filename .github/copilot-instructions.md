CRITICAL WORKFLOW RULE: You must strictly adhere to the Conventional Commits specification for every single commit message and Pull Request title you generate. This repository uses an automated CI/CD pipeline (`auto-release.yml`) that relies on the first word of the commit to trigger Modrinth and GitHub releases. 

1. THE PREFIX (Choose exactly one):
- `feat: ` -> Use ONLY when adding a brand new item, command, or player-facing mechanic. (Triggers a Minor Version Release).
- `fix: ` -> Use ONLY when patching a crash, fixing a bug, or correcting unintended behavior. (Triggers a Patch Version Release).
- `refactor: ` -> Use for internal code restructuring, optimizing databases, or cleaning up file trees where gameplay does not change. 
- `chore: ` -> Use for updating dependencies or modifying GitHub Actions.
- `feat!: ` or `fix!: ` -> Add the exclamation mark ONLY if the change completely breaks older versions (e.g., wiping the database or changing server requirements).

2. THE TITLE:
Keep the title under 50 characters. Write it in the imperative mood, as if giving a command. Do not use capital letters at the start of the description, and do not put a period at the end.

3. THE BODY (The Changelog):
Leave one blank line after the title. Write a concise, bulleted list of the exact changes made. This text will be read directly by players downloading the mod. Keep it professional, easy to understand, and do not include conversational AI filler.

4. THE SILENT OVERRIDE ([skip ci]):
If the task you are assigned ONLY modifies documentation (like README.md or .jules/ logs), comments, or non-compiled text files, you MUST append ` [skip ci]` to the very end of your commit title. 
Example: `docs: update command list in readme [skip ci]`
Do NOT use this tag if you modify any Java code, build scripts (.gradle, pom.xml), JSON configs, or workflow fil
es.
