---
layout: default
title: Troubleshooting
---

# Troubleshooting Guide

This guide covers common issues and their solutions. If you don't find your issue here, check the [FAQ](faq) or [open an issue](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues).

## Table of Contents

1. [Players Not Transferring to Limbo](#players-not-transferring-to-limbo)
1. [Revivals Not Working](#revivals-not-working)
1. [Players Lose Lives During Grace Period](#players-lose-lives-during-grace-period)
1. [Admin Commands Not Working on Limbo Server](#admin-commands-not-working-on-limbo-server)
1. [Version Mismatch Warnings](#version-mismatch-warnings)
1. [Database Connection Errors](#database-connection-errors)
1. [Players Reconnecting Go Straight to Limbo](#players-reconnecting-go-straight-to-limbo)
1. [Extra Life Items Not Working](#extra-life-items-not-working)
1. [Hardcore Hearts Not Showing](#hardcore-hearts-not-showing)
1. [Revival Ritual Structure Not Triggering](#revival-ritual-structure-not-triggering)
1. [Revive Skull Recipe Not Working](#revive-skull-recipe-not-working)
1. [Players Can't Visit Limbo](#players-cant-visit-limbo)
1. [Plugin Not Loading](#plugin-not-loading)

______________________________________________________________________

## Players Not Transferring to Limbo

### Symptoms

- Players lose all lives but stay on Main server

- No automatic transfer to Limbo occurs

- Console shows no errors

### Solutions

#### - Check Server Role Configuration

**On Main server:**

```yaml
is-limbo-server: false

```

**On Limbo server:**

```yaml
is-limbo-server: true

```

#### - Verify Server Names Match Proxy

**In your `config.yml` (both servers):**

```yaml
main-server-name: "main"
limbo-server-name: "limbo"

```

**In your `velocity.toml`:**

```toml
[servers]
  main = "localhost:25566"
  limbo = "localhost:25567"

```

The names must **exactly match** (case-sensitive).

#### - Check Database Connection

Both servers must connect to the same database:

1. Check console logs for "Database connection established"
1. Verify both servers have identical database credentials
1. Test database connectivity from each server

#### - Verify Player Information Forwarding

**For Velocity:**

```toml
player-info-forwarding-mode = "modern"

```

**For BungeeCord:**

```yaml

# config.yml

ip_forward: true

# spigot.yml on both servers

bungeecord: true

```

#### - Check Death Mode Configuration

**In Main server config:**

```yaml
main:
  death-mode: "hybrid"  # or "limbo" for immediate transfer

```

If using `spectator` mode, players won't transfer to Limbo automatically.

#### - Look for Plugin Messaging Errors

Check console logs for:

- "Failed to send player to server"

- "PluginMessageException"

- BungeeCord channel errors

______________________________________________________________________

## Revivals Not Working

### Symptoms

- Using `/revive` or ritual structure doesn't work

- Player stays dead in database

- No automatic return to Main server

### Solutions

#### - Ensure HRM is Enabled

```yaml
hrm:
  enabled: true
  structure-revive: true  # For ritual structures

```

#### - Verify Ritual Structure is Correct

**Structure Requirements:**

- **Bottom layer (3x3):**

  - 4 Soul Sand blocks at corners

  - 4 Stair blocks at edges

  - 1 Ore block in center (Gold/Diamond/Emerald/etc.)

- **Middle layer:**

  - 4 Wither Roses on Soul Sand corners

  - 1 Fence on center ore block

- **Top layer:**

  - Dead player's head on fence

#### - Enable Structure Detection

**In Main server config:**

```yaml
main:
  detect-hrm-revive: true

```

#### - Check Database Connectivity

Try manual revival to test database:

```text

/psadmin revive <player>

```

If this works but structures don't, the issue is with structure detection.

#### - Verify Both Servers Access Same Database

1. Check that both servers show "Database connection established"
1. Confirm identical database credentials on both servers
1. Test database access from both server hosts

#### - Check Limbo Server is Running

The Limbo server must be running to process revivals:

- Verify Limbo server is online

- Check Limbo console for revival check messages

- Default check interval: 3 seconds

______________________________________________________________________

## Players Lose Lives During Grace Period

### Symptoms

- New players lose lives even with grace period configured

- Grace period doesn't seem to be working

- Status shows grace period but lives still decrease

### Solutions

#### - Check Grace Period Configuration

```yaml
lives:
  grace-period: "24h"  # NOT "0" which disables it

```

Formats:

- `"24h"` = 24 hours

- `"2h30m"` = 2 hours 30 minutes

- `"90m"` = 90 minutes

- `"0"` = Disabled

#### - Verify Grace Period Status

```text

/pstatus <player>

```

Should show grace period remaining time.

#### - Set Grace Period Manually

For players who joined before enabling grace period:

```text

/psadmin grace <player> 24

```

#### - Remember: Online Time Only

Grace period only counts while player is online:

- Playing 3 hours → 21 hours remaining

- Logging off → timer pauses

- Logging back in → timer resumes

______________________________________________________________________

## Admin Commands Not Working on Limbo Server

### Symptoms

- `/revive` or `/psadmin` commands don't work on Limbo server

- "Security Error: On the Limbo server, OP status cannot be used..." message

- Commands work on Main server but not Limbo

### Solution

**This is normal security behavior!** By default, OP users are blocked from using admin commands on the Limbo server to prevent abuse.

#### - Use Whitelist (Easiest - No permissions plugin required)

Edit `config.yml`:

```yaml
limbo-trusted-admins:
  - "069a79f4-44e9-4726-a5be-fca90e38aaf5"  # UUID (recommended)

  - "PlayerName"                              # or username

```

Then reload from **console** (or from an already whitelisted/bypass-enabled admin):

```text

/psadmin reload

```

Or simply restart the server to apply the changes.

#### - Grant Bypass Permission (Requires LuckPerms)

Use a permissions plugin like LuckPerms:

```text

/lp user <player> permission set ssoggysouls.bypass-limbo-op-security true

```

This allows the OP to use admin commands on Limbo.

#### - Alternative: Remove OP and Use Explicit Permissions

```text

/deop <player>
/lp user <player> permission set ssoggysouls.admin true
/lp user <player> permission set ssoggysouls.revive true

```

#### - Disable Security Check (Not Recommended)

In `config.yml`:

```yaml
limbo-op-security-check: false

```

**Warning:** This removes the security protection and allows any OP on Limbo to revive/modify player data.

#### - Use Console

Console commands always work on both servers:

```text

/psadmin revive <player>
/revive <player>

```

______________________________________________________________________

## Version Mismatch Warnings

### Symptoms

- Console shows "Version mismatch detected"

- Plugin might not work correctly

- Strange behavior between servers

### Solutions

#### - Update Both Servers to Same Version

1. Download the latest SSoggySouls JAR
1. Stop both servers
1. Replace the JAR on **both** servers

1. Start both servers

#### - Verify Versions Match

Check console logs on both servers:

```text

[SSoggySouls] Version 4.0.11 enabled

```

Both must show the same version number.

#### - Clear Old Database Records (If Needed)

If switching from old version, you may need to:

```sql
DELETE FROM hardcore_players WHERE plugin_version IS NOT NULL;

```

**Warning:** This will reset all player data!

______________________________________________________________________

## Database Connection Errors

**(Note: This largely applies to `type: "mysql"`. If you are using `type: "sqlite"`, make sure your server has write permissions to the `plugins/SSoggySouls/` folder to generate the database.)**

### Symptoms

- "Failed to connect to database"

- "Communications link failure"

- "Access denied for user"

- "Unknown database"

### Solutions

#### - Verify MySQL/MariaDB is Running

```bash

# Linux

sudo systemctl status mysql

# or

sudo systemctl status mariadb

# Windows

Check Services for MySQL

```

#### - Test Database Credentials

Use MySQL client to test:

```bash
mysql -h localhost -P 3306 -u ssoggysouls_user -p

```

Enter password when prompted. If this fails, your credentials are wrong.

#### - Check Firewall Rules

Ensure backend servers can reach database:

```bash

# Test connection

telnet database_host 3306

# or

nc -zv database_host 3306

```

#### - For Pterodactyl: Use Panel Host

Don't use "localhost" - use the host provided by your panel:

```yaml
database:
  host: "mysql.example.com"  # From panel, not "localhost"

  port: 3306

```

#### - Ensure Database Exists

```sql
CREATE DATABASE IF NOT EXISTS ssoggysouls;

```

#### - Check User Permissions

```sql
-- Restrict to the host your backend uses (e.g. 127.0.0.1 or your server IP)

GRANT SELECT, INSERT, UPDATE, DELETE ON ssoggysouls.* TO 'ssoggysouls_user'@'127.0.0.1';

FLUSH PRIVILEGES;

```

#### - Verify Connection Pool Size

For high-traffic servers, increase pool size:

```yaml
database:
  pool-size: 10  # Default is 5

```

______________________________________________________________________

## Players Reconnecting Go Straight to Limbo

### Symptoms

- Player dies and enters spectator mode

- Player disconnects

- Upon reconnecting, player is in Limbo (skipped spectator)

### Solution

**This is intended behavior** for hybrid mode!

In hybrid mode:

- Dying → Spectator mode for timeout period

- Disconnecting while dead → Skip spectator on reconnect

- Reconnecting → Go straight to Limbo

**If you want different behavior:**

Use `spectator` mode instead:

```yaml
main:
  death-mode: "spectator"  # Dead players stay in spectator indefinitely

```

______________________________________________________________________

## Extra Life Items Not Working

### Symptoms

- Crafting Extra Life item doesn't work

- Using Extra Life item does nothing

- No lives gained

### Solutions

#### - Confirm Extra Life is Enabled

```yaml
extra-life:
  enabled: true

```

#### - Check Recipe Configuration

Verify all materials are valid Minecraft material names:

```yaml
extra-life:
  recipe:
    row1: "DED"
    row2: "INI"
    row3: "GEG"
    ingredients:
      G: "GOLD_BLOCK"      # Must be valid material

      E: "EMERALD_BLOCK"
      N: "NETHER_STAR"
      D: "DIAMOND_BLOCK"
      I: "NETHERITE_INGOT"

```

#### - Verify Player is Not at Max Lives

Extra Life cannot exceed max lives:

```yaml
lives:
  max-lives: 5  # Player can't gain lives beyond this

```

Check with:

```text

/pstatus

```

#### - Ensure Player is Alive

Can't use Extra Life while dead.

#### - Test Recipe

Try crafting with exact materials in the exact pattern defined.

______________________________________________________________________

## Hardcore Hearts Not Showing

### Symptoms

- Hearts appear normal, not hardcore style

- Expected hardcore (half-heart) appearance

### Solutions

#### - Enable in Configuration

```yaml
hardcore-hearts: true

```

#### - Understand It's Cosmetic Only

Hardcore hearts:

- Are purely visual

- Don't affect gameplay

- Lives system works regardless

- May require client support or resource pack

#### - Restart Server

After enabling, restart the server:

```text

/psadmin reload

```

#### - Note: Client-Side Feature

This feature may require:

- Specific client mods

- Resource pack support

- May not work for all clients

______________________________________________________________________

## Revival Ritual Structure Not Triggering

### Symptoms

- Built correct structure

- Placed head on top

- Nothing happens

### Solutions

#### - Verify Structure is Exactly Correct

**Common mistakes:**

- Using wrong blocks (must be Soul Sand at corners)

- Fence not placed directly on ore block

- Wither Roses not on Soul Sand corners

- Structure size wrong (must be exactly 3x3x3)

#### - Check Player Head is Correct

- Must be the actual dead player's head

- Use Revive Skull to get correct head

- Or use `/give @s player_head{SkullOwner:"PlayerName"}`

#### - Ensure Detection is Enabled

```yaml
main:
  detect-hrm-revive: true

```

#### - Verify HRM is Enabled

```yaml
hrm:
  enabled: true
  structure-revive: true

```

#### - Check Plugin is Listening for Block Places

Restart server if needed:

```text

/psadmin reload

```

______________________________________________________________________

## Revive Skull Recipe Not Working

### Symptoms

- Can't craft Revive Skull

- Recipe doesn't appear

- Items don't combine

### Solutions

#### - Enable Recipe

```yaml
hrm:
  revive-skull-recipe: true

```

#### - Use Correct Recipe (Shapeless)

Place anywhere in crafting grid:

- 4 Obsidian

- 2 Ghast Tear

- 2 Totem of Undying

- 1 Any Skull/Head

**Note:** Order doesn't matter (shapeless recipe).

#### - Restart Server

After enabling:

```text

/psadmin reload

```

______________________________________________________________________

## Players Can't Visit Limbo

### Symptoms

- `/limbo` command doesn't work

- "You don't have permission" error

- Nothing happens

### Solutions

#### - Check Permission

```yaml
permissions:
  ssoggysouls.visit: true  # Should default to true

```

Grant permission:

```text

/lp user <player> permission set ssoggysouls.visit true

```

#### - Verify Limbo Server is Online

Limbo server must be running for visits.

#### - Check Server Name Configuration

```yaml
limbo-server-name: "limbo"  # Must match proxy config

```

#### - Ensure Player is Alive

Dead players can't use `/limbo` - they're already there!

______________________________________________________________________

## Plugin Not Loading

### Symptoms

- Plugin doesn't show in `/plugins`

- No console messages from SSoggySouls

- Commands don't work

### Solutions

#### - Check Java Version

Requires Java 21 or higher:

```bash
java -version

```

#### - Verify Minecraft Version

Plugin supports 1.21.X only. Check server version:

```text

/version

```

#### - Check Console for Errors

Look for:

- "UnsupportedClassVersionError" → Java version too old

- "NoClassDefFoundError" → Missing dependency

- Other error messages

#### - Verify Plugin File is Not Corrupted

1. Re-download the JAR
1. Check file size matches
1. Replace old JAR with new one

#### - Check Plugins Folder

Ensure JAR is in `plugins/` folder, not a subfolder.

______________________________________________________________________

## Getting Additional Help

If your issue isn't covered here:

1. **Enable Debug Mode**

   ```yaml
   debug: true

```text

Restart both servers and reproduce the issue.

1. **Collect Information**

   - SSoggySouls version

   - Minecraft version

   - Proxy type (Velocity/BungeeCord)

   - Relevant config sections

   - Console errors/logs

   - Steps to reproduce

1. **Search Existing Issues**
   Check [GitHub Issues](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues) for similar problems.

1. **Open a New Issue**
   [Create an issue](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues/new) with all collected information.

## Related Resources

- [Installation Guide](installation)

- [Configuration Reference](configuration)

- [Commands](commands)

- [FAQ](faq)

______________________________________________________________________

[← Back to Home](index) | [FAQ →](faq)
