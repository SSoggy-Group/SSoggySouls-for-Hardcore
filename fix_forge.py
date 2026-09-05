import re

filepath = './forge/src/main/java/org/ssoggy/ssoggysouls/command/CommandRegistration.java'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('.sendFailure(MessageUtil.get("usage-revive")\n                    .withStyle', '.sendFailure(MessageUtil.get("usage-revive").copy()\n                    .withStyle')
content = content.replace('MessageUtil.get("click-to-autofill").withStyle', 'MessageUtil.get("click-to-autofill").copy().withStyle')
content = content.replace('.sendFailure(MessageUtil.get("usage-psetlives")\n                    .withStyle', '.sendFailure(MessageUtil.get("usage-psetlives").copy()\n                    .withStyle')
content = content.replace('.sendFailure(MessageUtil.get("usage-psetlives-player", PLAYER, targetName)\n                        .withStyle', '.sendFailure(MessageUtil.get("usage-psetlives-player", PLAYER, targetName).copy()\n                        .withStyle')

with open(filepath, 'w') as f:
    f.write(content)
