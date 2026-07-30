import re

def fix(path):
    with open(path, 'r') as f:
        content = f.read()

    # In forge and neoforge CommandRegistration:
    # We missed `.withStyle(s -> s.withColor(net.minecraft.ChatFormatting.RED)` which should be `.copy().withStyle(s -> s.withColor(net.minecraft.ChatFormatting.RED)`
    # The annotations show failure at line 116 in forge/CommandRegistration and line 115 in neoforge/CommandRegistration
    # Wait, earlier I did replace MessageUtil.get("usage-revive-player", PLAYER, targetName).copy().withStyle
    # The error "cannot find symbol" might be because .copy() is needed on the `Component` that is returned from MessageUtil.
    # Ah! I see in the log: `MessageUtil.get("usage-revive-player", ...).copy().withStyle(s -> s.withColor(...)`
    # The issue is I replaced `MessageUtil.get(...).withStyle` but in the file, it's `.withStyle` on a new line!
    # E.g. `MessageUtil.get("usage-revive")\n .withStyle`
    pass
