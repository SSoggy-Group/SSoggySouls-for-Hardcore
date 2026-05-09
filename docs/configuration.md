---
layout: default
title: Configuration Reference
---

# Configuration Reference

This guide covers all the options available in `config.yml` (or `ssoggysouls.json` on Fabric/Forge) so you can customize how the lives and revival mechanics work on your server.

**File Location:** `plugins/SSoggySouls/config.yml` (on Bukkit/Paper servers)

> **Tip:** Most changes will apply immediately if you run the `/psadmin reload` command in-game or from your console, although some database or server role changes will require a full server restart.

---

## Table of Contents

1. [Overview](#overview)
2. [Mod Configurations (Fabric & Forge)](#mod-configurations-fabric--forge)
3. [Server Role](#server-role)
4. [Security Settings](#security-settings)
5. [Database Configuration](#database-configuration)
6. [Lives System](#lives-system)
7. [Death Modes](#death-modes)
8. [Limbo Settings](#limbo-settings)
9. [Hardcore Revive Mode (HRM) Features](#hardcore-revive-mode-hrm-features)
10. [Advanced Revival & Ghost Settings (RevivalPlus DLC)](#advanced-revival--ghost-settings)
11. [Extra Life Crafting Recipe](#extra-life-crafting-recipe)
12. [Messages & Customization](#messages--customization)
13. [Advanced Options](#advanced-options)
14. [Configuration Examples](#configuration-examples)

---

## Overview

SSoggySouls uses a `config.yml` file that is automatically generated when you first run the plugin. This file is highly customizable and includes detailed comments explaining each setting.

**Location:** `plugins/SSoggySouls/config.yml` on both servers

> **Important:** After editing the config file, either restart both servers or run `/psadmin reload` to reload without a restart (though some database changes require a full restart).

---

## Mod Configurations (Fabric & Forge)

If you are running a modded server, the configuration file is structurally identical, but uses a different file format and location:

- **Fabric:** Located at `config/ssoggysouls.json`
- **Forge:** Located at `config/ssoggysouls.toml` (or `ssoggysouls.json` depending on your version)

The keys and setting values behave exactly the same way across all platforms.

---

## Server Role

These settings identify what role each server plays in your network.

### Main Server Configuration
```yaml
is-limbo-server: false    # This is the Main server (survival)

# Must match your Velocity/proxy configuration
main-server-name: "main"     # Name of Main server in proxy config
limbo-server-name: "limbo"   # Name of Limbo server in proxy config
```

### Limbo Server Configuration
```yaml
is-limbo-server: true     # This is the Limbo server (purgatory)

# Must match your Velocity/proxy configuration
main-server-name: "main"     # Name of Main server in proxy config
limbo-server-name: "limbo"   # Name of Limbo server in proxy config
```

> **Tip:** The server names must exactly match the server names in your `velocity.toml` file.

---

## Security Settings

### Limbo OP Security Check
```yaml
limbo-op-security-check: true  # Prevent Limbo-only OPs from using admin commands
```

This setting prevents a critical security vulnerability where users with OP status **only on the Limbo server** could execute privileged commands like `/revive` and `/psadmin`.

#### Why This Matters
If you give someone OP on the Limbo server (for example, to manage the Limbo world), they could potentially:
- Revive any dead player using `/revive <player>`
- Modify player lives with `/psadmin lives`
- Force-kill players with `/psadmin kill`
- Grant grace periods with `/psadmin grace`
- Reset player data with `/psadmin reset`

#### How It Works
When enabled (recommended), this security check will:
1. **Block** all OP users on the Limbo server from executing admin/revive commands.
2. **Allow** OP users with the bypass permission `ssoggysouls.bypass-limbo-op-security`.
3. **Allow** non-OP users with explicit permission nodes.
4. **Allow** commands on the Main server regardless of how permissions are granted.
5. **Allow** console to always execute commands.

#### Granting Proper Permissions
To allow a user to execute admin commands on the Limbo server, you have three options:

### Option 1: Use the Whitelist (Easiest - No permissions plugin required)
```yaml
# In config.yml
limbo-trusted-admins:
  - "069a79f4-44e9-4726-a5be-fca90e38aaf5"  # UUID format (recommended)
  - "YourUsername"                          # Username format also works
```
- UUIDs are recommended (won't break if player changes name).
- Usernames are easier to manage.
- No permissions plugin required.
- After editing `config.yml`, apply changes by running `/psadmin reload` from the server console or from an already whitelisted account.

### Option 2: Grant bypass permission (requires LuckPerms or similar)
```text
/lp user <player> permission set ssoggysouls.bypass-limbo-op-security true
```

### Option 3: Grant explicit permissions without OP
```text
# Remove OP status first, then grant explicit permissions
/deop <player>

# For revival commands:
/lp user <player> permission set ssoggysouls.revive true

# For all admin commands:
/lp user <player> permission set ssoggysouls.admin true
```

---

## Database Configuration

This plugin supports two types of databases:

### 1. SQLite (Local - Single Server)
Ideal for standard, single-server setups. No extra setup required. Data is stored locally.
```yaml
database:
  type: "sqlite"
```

### 2. MySQL (Network - 2-Server Setup)
REQUIRED if you are using the true 2-server (Main + Limbo) functionality.
Both servers must use **identical** database credentials or player records will be split!
```yaml
database:
  type: "mysql"
  host: "localhost"              # Database host
  port: 3306                     # Database port (default: 3306)
  name: "ssoggysouls"             # Database name
  username: "root"               # MySQL username
  password: "changeme"           # MySQL password - CHANGE THIS!
  pool-size: 5                   # Connection pool size
  table-name: "hardcore_players" # Table name (default is fine)
```

### Connection Pool Recommendations
- **Small servers (1-20 players):** 5
- **Medium servers (20-50 players):** 10
- **Large servers (50+ players):** 15-20

### For Pterodactyl Hosting
Use the database host provided by your hosting panel, not "localhost":
```yaml
database:
  host: "db-pterodactyl.c9akciq32cpl.us-east-1.rds.amazonaws.com"
  port: 3306
  name: "s123_ssoggysouls"
  username: "u123_admin"
  password: "your_secure_password"
```

---

## Lives System

Configure how the lives mechanic works.

```yaml
lives:
  default: 2                      # Starting lives for new players
  max-lives: 5                    # Maximum lives cap
  on-revive: 1                    # Lives restored when revived
  grace-period: "24h"             # New player protection period
  revive-cooldown-seconds: 30     # Post-revival death protection
```

### Default Lives
Starting number of lives for newly joined players. Common values:
- `1` - Hardcore (one life)
- `2` - Standard (two lives)
- `3` - Casual (three lives)

### Maximum Lives
The highest number of lives a player can have. They cannot use extra life items or commands to exceed this cap.

### Lives on Revival
How many lives a dead player gets back when revived. Options:
- `1` - Revived players get 1 life (requires teamwork to get more)
- `2` - Revived players get 2 lives (more forgiving)

### Grace Period
New player protection that prevents losing lives during the grace period. The timer counts **only when the player is online** (pauses when offline).

**Format Options:**
- `"24h"` = 24 hours
- `"2h30m"` = 2 hours 30 minutes
- `"90m"` = 90 minutes
- `"0"` = Disabled (no grace period)

**Example:** A new player with "24h" grace who plays 3 hours, logs off, then returns will have 21 hours remaining.

### Revive Cooldown
After being revived, a player is protected for this duration and cannot lose lives from deaths. Gives them time to get situated.

---

## Death Modes

Choose how your server handles player death.

```yaml
main:
  death-mode: "spectator"         # SQLite uses spectator; MySQL can use hybrid | spectator | limbo
  hybrid-timeout-seconds: 300     # Timeout for hybrid mode (5 min)
  spectator-on-death: false       # Put in spectator before Limbo
  detect-hrm-revive: true         # Auto-detect ritual structure revivals
  send-to-limbo-delay-ticks: 20   # Delay before Limbo transfer (1 sec)
```

> **SQLite single-server note:** SQLite always uses spectator mode. Use MySQL with a proxy/Limbo server for `hybrid` or `limbo`.

### Hybrid Mode (Dual-Server Recommended)
```yaml
death-mode: "hybrid"
hybrid-timeout-seconds: 300
```
**Behavior:**
- Player loses a life and enters spectator mode on Main.
- Team has 5 minutes (configurable) to revive them.
- If not revived within timeout, player transfers to Limbo.
- If player disconnects while dead, they skip spectator and go straight to Limbo.

**Best For:** Servers wanting tension and urgency - gives teams a limited rescue window.

**Timeout Options:**
- `300` = 5 minutes (default)
- `180` = 3 minutes (faster)
- `600` = 10 minutes (more generous)

### Spectator Mode
```yaml
death-mode: "spectator"
spectator-on-death: true
```
**Behavior:**
- Player loses a life and enters spectator mode.
- Player stays on Main server indefinitely.
- Never auto-transfers to Limbo.
- Can only be sent to Limbo by dying without lives or admin command.

**Best For:** Servers preferring dead players to spectate their team constantly without forced exile.

### Limbo Mode (Strict)
```yaml
death-mode: "limbo"
```
**Behavior:**
- Player loses a life.
- Upon reaching 0 lives, player immediately transfers to Limbo.
- No spectator window.

**Best For:** Hardcore servers enforcing strict separation between living and dead.

### HRM Revival Detection
When enabled, the plugin automatically detects when a ritual structure is completed and performs the revival. Leave this enabled unless troubleshooting.

### Limbo Transfer Delay
Delay in ticks before sending a player to Limbo (20 ticks = 1 second). Gives time for other events to process. Usually no need to change.

---

## Limbo Settings

Configure the Limbo server (only applies on Limbo server).

```yaml
limbo:
  check-interval-seconds: 3       # How often to check for revivals

# --- BUKKIT ONLY ---
  spawn:
    world: "world"
    x: 0.5
    y: 65.0
    z: 0.5
    yaw: 0.0
    pitch: 0.0

# --- FABRIC ONLY (ssoggysouls.json) ---
#  "limboSpawnWorld": "minecraft:overworld",
#  "limboSpawnX": 0.5,
#  "limboSpawnY": 65.0,
#  "limboSpawnZ": 0.5,
#  "limboSpawnYaw": 0.0,
#  "limboSpawnPitch": 0.0
```

### Check Interval
How frequently the Limbo server checks the database for revival requests. Lower = more responsive, higher = less database load.
- `1` - Very responsive (high database load)
- `3` - Balanced (default, recommended)
- `5` - Less responsive (low database load)

### Spawn Location
Set the spawn location where dead players appear on Limbo. **Use the `/setlimbospawn` command** to set this automatically:
1. Stand at desired spawn location on Limbo.
2. Run `/setlimbospawn`.
3. Coordinates are automatically saved.

---

## Hardcore Revive Mode (HRM) Features

Configure the built-in Hardcore Revive Mode system.

```yaml
hrm:
  enabled: true                   # Master HRM toggle
  drop-heads: true                # Drop player heads on death
  death-location-message: true    # Send death coords to player
  structure-revive: true          # Enable 3x3x3 ritual structures
  leave-structure-base: true      # Don't destroy structure after revive
  head-wearing-effects: true      # Speed/night vision when wearing heads
  revive-skull-recipe: true       # Enable Revive Skull crafting
```

### Enable/Disable HRM
Master toggle for all HRM features. Set to `false` to disable everything related to HRM.

### Player Head Drops
When a player loses all lives, their head drops at the death location. These heads are used in ritual structures for revival.

### Death Location Messages
When a player dies, send them a message with the death coordinates to help their teammates locate their dropped head.

**Example Message:**
```text
Death coordinates: X: 1234 Y: 64 Z: -5678
```

### Structure-Based Revival
Enable the 3x3x3 ritual structure revival system. When disabled, ritual structures won't trigger revivals.

### Leave Structure Base
If true, the base structure remains after revival (only the player head is removed).
If false, the entire structure is destroyed upon successful revival.

### Head Wearing Effects
When a player wears a dead player's head, they receive:
- **Speed II** effect
- **Night Vision** effect
Tactical advantage while carrying heads to revival structures.

### Revive Skull Recipe
Enable crafting of Revive Skull items. The recipe is:

| | | |
|---|---|---|
| Obsidian | Ghast Tear | Obsidian |
| Totem of Undying | Any Skull/Head | Totem of Undying |
| Obsidian | Ghast Tear | Obsidian |

---

## Advanced Revival & Ghost Settings

Configure the advanced mechanics ported from the RevivalPlus DLC. You can also configure these entirely in-game using the `/revivalconfig` command.

```yaml
# -- GAMERULES --
lose-inventory: false                 # Do players drop inventory upon hardcore death?
restrict-menu-access: true            # Can ghosts open menus?
creative-players-drop-heads: false    # Do creative players drop heads?
keep-structure-base: true             # Does structure remain after revival?
head-effects: true                    # Speed/Night Vision when wearing heads?
head-burns-in-lava: false             # Do heads burn? (If false, placed as block)
ritual-lightning-strike: true         # Strike lightning on revival?
ritual-totem-effect: true             # Show totem animation on revival?
ghost-mode-particles: false           # Show Dragon Breath particles for ghosts?

# -- TIMERS --
trusted-obituary-after: 60            # Minutes until death is visible to 'trusted' in /deathlist
friends-obituary-after: 600           # Minutes until death is visible to 'friends' in /deathlist
public-obituary-after: 3600           # Minutes until death is visible to everyone in /deathlist
spectator-headrestrict-radius: 16     # Block radius limit for ghosts
revive-resistance-ticks: 100          # Ticks of Resistance given after revive (20 = 1 sec)
revive-glowing-ticks: 100             # Ticks of Glowing given after revive

# -- STRUCTURE BLOCK TAGS --
# Customize the blocks used for the revival structure
soul-sand-blocktag:
  - "CRYING_OBSIDIAN"
  - "OBSIDIAN"
flower-blocktag:
  - "SOUL_TORCH"
  - "REDSTONE_TORCH"
# ... (see config.yml for full lists)
```

---

## Extra Life Crafting Recipe

Configure the craftable item that grants additional lives.

```yaml
extra-life:
  enabled: true
  item-material: "NETHER_STAR"    # Icon displayed in menus

  recipe:
    row1: "DED"    # Row 1: Diamond, Emerald, Diamond
    row2: "INI"    # Row 2: Netherite Ingot, Nether Star, Netherite Ingot
    row3: "GEG"    # Row 3: Gold Block, Emerald Block, Gold Block

    ingredients:
      G: "GOLD_BLOCK"
      E: "EMERALD_BLOCK"
      N: "NETHER_STAR"
      D: "DIAMOND_BLOCK"
      I: "NETHERITE_INGOT"
```

### Enable/Disable
Set to `false` to disable extra life crafting entirely.

### Item Icon
The item used as the icon in menus and crafting results. Any valid Minecraft material works (common choices: `NETHER_STAR`, `GOLDEN_APPLE`, `DIAMOND`, `EMERALD`).

### Custom Recipe
To create a custom recipe:
1. Define the layout in `row1`, `row2`, `row3` using letters.
2. Map each letter to a material in `ingredients`.

**Example: Simple Diamond Recipe**
```yaml
recipe:
  row1: "DDD"
  row2: "DED"
  row3: "DDD"
  ingredients:
    D: "DIAMOND_BLOCK"
    E: "EMERALD_BLOCK"
```
Result: 9 Diamond Blocks with 1 Emerald Block in the center.

**Valid Material Names:**
Any Minecraft material name in SCREAMING_SNAKE_CASE. Check Papermc javadocs or look at the `org.bukkit.Material` enum.

---

## Messages & Customization

Customize all player-facing messages and notification colors.

```yaml
messages:
  prefix: "&8[&4☠&8] &r"
  death-life-lost: "&cYou lost a life! &7Remaining: &e%lives%"
  death-last-life: "&c&l⚠ FINAL WARNING! &cYou are on your last life. Be careful!"
  revive-success: "&a&l✦ REVIVED! &aReturning to the world of the living..."
  # ... many more messages
```

### Color Codes
- `&0` = Black
- `&c` = Red
- `&a` = Green
- `&e` = Yellow
- `&9` = Blue
- `&d` = Magenta
- `&b` = Cyan
- `&f` = White
- `&7` = Gray
- `&8` = Dark Gray

### Formatting Codes
- `&l` = Bold
- `&m` = Strikethrough
- `&n` = Underline
- `&o` = Italic
- `&r` = Reset

### Available Variables
- `%lives%` - Player's current lives
- `%player%` - Player name
- `%timeout%` - Timeout duration (hybrid mode)
- `%time_remaining%` - Grace period remaining
- `%max%` - Maximum lives

### Example: Custom Messages
```yaml
messages:
  prefix: "&8[&cDEATH&8] &r"
  death-life-lost: "&cOh no! &7You lost a life. &eRemaining: %lives%/%max%"
  death-last-life: "&c&l💀 CRITICAL! &cThis is your LAST life!"
  revive-success: "&a&l✓ &aYou have been revived! Stay alive this time!"
  grace-remaining: "&eYou are protected for &a%time_remaining%"
```

---

## Advanced Options

```yaml
check-for-updates: true          # Check for new versions on Modrinth
hardcore-hearts: true            # Display hardcore hearts cosmetically
debug: false                     # Enable debug logging (dev only)
```

### Update Checking
When enabled, the plugin checks Modrinth on startup for newer versions and displays update notices in console.

### Hardcore Hearts
Display Minecraft's hardcore-style hearts (half-hearts instead of full hearts). This is **cosmetic only** and doesn't affect gameplay. Requires client mod support or resource pack to display properly.

### Debug Logging
Enable detailed debug logging to console. Only useful for troubleshooting.

---

## Configuration Examples

### Hardcore Server (One Life)
```yaml
lives:
  default: 1
  max-lives: 1
  on-revive: 0  # No lives on revive (need to craft extra life)

main:
  death-mode: "limbo"  # Immediate exile
```

### Casual Server (Three Lives)
```yaml
lives:
  default: 3
  max-lives: 5
  on-revive: 2
  grace-period: "48h"  # Extended grace for new players

main:
  death-mode: "hybrid"
  hybrid-timeout-seconds: 600  # 10 minute timeout
```

### Competitive Server (Spectator Focus)
```yaml
lives:
  default: 2
  max-lives: 3
  grace-period: "0"  # No grace period

main:
  death-mode: "spectator"  # Dead players watch forever
```

---

## Reloading Configuration

To reload configuration without restarting:
```bash
/psadmin reload
```
**Note:** Some changes take effect immediately, others may require player rejoin, server restart, or plugin reload.

---

## Need Help?
- [Installation Guide](installation)
- [Troubleshooting Guide](troubleshooting)
- 📋 [Commands Reference](commands)
- [FAQ](faq)

---

[← Commands Reference](commands) | [Back to Home](index) | [Revival System →](revival-system)
