import re

files = [
    'fabric/src/main/java/org/ssoggy/ssoggysouls/hrm/dlc/command/DlcCommandRegistration.java',
    'forge/src/main/java/org/ssoggy/ssoggysouls/hrm/dlc/command/DlcCommandRegistration.java',
    'neoforge/src/main/java/org/ssoggy/ssoggysouls/hrm/dlc/command/DlcCommandRegistration.java'
]

# The format method is also highly duplicated. The only difference is Text.literal (Fabric) vs Component.literal (Forge/NeoForge)
# We can't extract the WHOLE method to common without doing cross-platform stuff.
# Wait, look at the annotations for the failure!
# [FAILURE] File: neoforge/src/main/java/org/ssoggy/ssoggysouls/command/CommandRegistration.java, Line: 152
# [FAILURE] File: forge/src/main/java/org/ssoggy/ssoggysouls/command/CommandRegistration.java, Line: 117
# [FAILURE] File: fabric/src/main/java/org/ssoggy/ssoggysouls/hrm/dlc/command/DlcCommandRegistration.java, Line: 127
# [FAILURE] File: forge/src/main/java/org/ssoggy/ssoggysouls/hrm/dlc/command/DlcCommandRegistration.java, Line: 129
# [FAILURE] File: neoforge/src/main/java/org/ssoggy/ssoggysouls/command/CommandRegistration.java, Line: 116
# [FAILURE] File: neoforge/src/main/java/org/ssoggy/ssoggysouls/hrm/dlc/command/DlcCommandRegistration.java, Line: 129
# Wait, Line 127 in fabric DlcCommandRegistration.java is `sendResult(context.getSource(), DlcCommandResult.missingArgs(USAGE_TRUST, SUGGEST_TRUST));`
# What is it duplicating with? It's duplicating across platforms.
# Since my previous commit extracted constants, Sonar should see those constants instead of strings.
# But wait, wait! The lines 127, 129, etc. were flagged BEFORE my last commit.
# The PR check from the instruction says:
# URL: https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/runs/98467315147
# "Failed Check Run 1: SonarCloud Code Analysis"
# I made 9 commits, and each ran tests.
