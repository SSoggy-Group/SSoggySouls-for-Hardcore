import os
import glob

# Looks like forge is trying to use Fabric classes or mapping names instead of Forge/NeoForge classes.
# I will just revert my changes to `forge/src/main/java/org/ssoggy/ssoggysouls/database/MySQLManager.java`
# and `forge/src/main/java/org/ssoggy/ssoggysouls/database/SQLiteManager.java` and re-apply them correctly.

# The errors show that GhostBlockEvents uses `net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents` inside the `forge` project directory.
# This means the forge project itself in the codebase is broken or improperly converted from Fabric, or using mixed mappings.
# Wait, this is a port issue. Did my patch to MySQLManager cause 100 errors?
# No, "package net.fabricmc.fabric.api.event.player does not exist".
# The forge port must be currently uncompilable due to unresolved fabric dependencies.
# I will check if it was compilable before my changes.
