import re

files = [
    'fabric/src/main/java/org/ssoggy/ssoggysouls/hrm/dlc/command/DlcCommandRegistration.java',
    'forge/src/main/java/org/ssoggy/ssoggysouls/hrm/dlc/command/DlcCommandRegistration.java',
    'neoforge/src/main/java/org/ssoggy/ssoggysouls/hrm/dlc/command/DlcCommandRegistration.java'
]

# Looking at the Sonar CPD failure
# "76.6% Duplication on New Code"
# Sonar CPD identifies code fragments that are identical across files.
# It seems `DlcCommandRegistration.java` is duplicated across fabric, forge, and neoforge.
# BUT `.sonarcloud.properties` excludes `fabric/**/*,forge/**/*,neoforge/**/*` from CPD.
# Wait, let's check `.sonarcloud.properties` again.
