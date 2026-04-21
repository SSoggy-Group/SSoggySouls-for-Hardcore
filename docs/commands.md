---
layout: default
title: Commands Reference
---

## Commands Reference

Complete list of all SSoggySouls commands for players and administrators, with examples and permission requirements.

## Table of Contents

1. [Player Commands](#player-commands)
1. [Admin Commands](#admin-commands)
1. [Command Examples](#command-examples)
1. [Permissions](#permissions)

## Player Commands

Player commands are available to all players by default or with minimal permissions.

### `/pstatus [player]`

Check your or another player's lives, death status, and grace period remaining.

**Permission:** `ssoggysouls.status` (default: true)\
**Aliases:** None

**Usage:**

````bash
# Check your own status
/pstatus

# Check another player's status
/pstatus YourUsername
```text
**Output Example:**

```text
YourUsername - Lives: 2 - Status: Alive
```text
______________________________________________________________________

### `/revive <player>`

Revive a dead player and return them from Limbo to the Main server. Can be used by players with the appropriate permission or through command blocks.

**Permission:** `ssoggysouls.revive` (default: op)\
**Aliases:** None

**Usage:**

```bash
/revive YourUsername
```text
**Output:**

- Success: Revived player is automatically teleported back to Main server
- Only works if the player is currently dead

______________________________________________________________________

### `/limbo`

Visit the Limbo server as a living player to interact with dead teammates. You can freely come and go.

**Permission:** `ssoggysouls.visit` (default: true)\
**Aliases:** `/visitlimbo`

**Usage:**

```bash
/limbo
```text
**Features:**

- - Alive players can visit anytime
- - Can return to Main with `/leavelimbo`
- - Dead players cannot use this command
- - Dead players cannot leave Limbo until revived

______________________________________________________________________

### `/leavelimbo`

Return from Limbo to Main server. Only works for living players visiting Limbo.

**Permission:** `ssoggysouls.visit` (default: true)\
**Aliases:** `/hub`

**Usage:**

```bash
/leavelimbo
# or
/hub
```text
______________________________________________________________________

## Admin Commands

Administrator commands require the `ssoggysouls.admin` permission (op by default).

### `/psadmin lives <player> <amount>`

Set a player's life count to a specific value.

**Permission:** `ssoggysouls.admin`\
**Aliases:** `/psa lives`

**Usage:**

```bash
# Give a player 5 lives
/psadmin lives YourUsername 5

# Set player to 1 life
/psadmin lives NewPlayer 1

# Reset to 0 lives (sends to Limbo)
/psadmin lives TestPlayer 0
```text
______________________________________________________________________

### `/psadmin revive <player>`

Revive a dead player and return them to the Main server with restored lives.

**Permission:** `ssoggysouls.admin`\
**Aliases:** `/psa revive`

**Usage:**

```bash
/psadmin revive YourUsername
```text
**Result:**

- Player is marked as alive in database
- Lives are restored (default: 1, configurable)
- Player is automatically teleported back to Main server
- Works even if player is offline (they revive upon next login)

______________________________________________________________________

### `/psadmin kill <player>`

Force-kill a player by setting their lives to 0 and sending them to Limbo immediately.

**Permission:** `ssoggysouls.admin`\
**Aliases:** `/psa kill`

**Usage:**

```bash
# Force kill a player for testing
/psadmin kill TestPlayer
```text
**Result:**

- Player's lives set to 0
- Player is marked as dead
- Player is transferred to Limbo (depending on death mode)

______________________________________________________________________

### `/psadmin grace <player> <hours>`

Set or modify a player's grace period manually.

**Permission:** `ssoggysouls.admin`\
**Aliases:** `/psa grace`

**Usage:**

```bash
# Set a new player to have 48 hours of grace
/psadmin grace NewPlayer 48

# Give extended grace (72 hours = 3 days)
/psadmin grace PlayerName 72

# Remove grace period
/psadmin grace PlayerName 0
```text
**Format Options:**

- Number only (e.g., `24`) = hours
- Can be 0 to disable grace entirely

______________________________________________________________________

### `/psadmin reset <player>`

Reset a player to defaults: restore default lives count and clear any custom grace period.

**Permission:** `ssoggysouls.admin`\
**Aliases:** `/psa reset`

**Usage:**

```bash
/psadmin reset YourUsername
```text
**Result:**

- Lives reset to default (configured in `config.yml`)
- Grace period cleared
- Death status reset to alive
- Revive cooldown cleared

______________________________________________________________________

### `/psadmin info <player>`

View detailed player information including UUID, lives, death state, timestamps, and grace period.

**Permission:** `ssoggysouls.admin`\
**Aliases:** `/psa info`

**Usage:**

```bash
/psadmin info YourUsername
```text
**Output Example:**

```text
=== Player Information ===
Username: YourUsername
UUID: a1b2c3d4-e5f6-7890-abcd-ef1234567890
Lives: 2
Status: Alive
Last Seen: 2024-01-15 14:30:00
Joined: 2024-01-10 08:15:30
Grace Period Remaining: 18h 45m
```text
______________________________________________________________________

### `/psadmin reload`

Reload the configuration file from disk without restarting the server.

**Permission:** `ssoggysouls.admin`\
**Aliases:** `/psa reload`

**Usage:**

```bash
/psadmin reload
```text
**Result:**

- Configuration file is reloaded
- Most settings take effect immediately
- Some changes may require player rejoins

______________________________________________________________________

### `/adminlog [lines]`

View the admin abuse action log directly in-game. By default, reads the last 15 lines.

**Permission:** `ssoggysouls.adminlog` (default: op)\
**Aliases:** None

**Usage:**

```bash
/adminlog
/adminlog 25
```text
**Result:**

- Displays recent admin actions (such as giving lives, reviving, killing).
- This command is restricted to OPs by default.
- To allow specific non-ops to view logs without giving them OP, add them to `admin-log.trusted-viewers` in the config or grant the `ssoggysouls.adminlog` permission. Set `allow-all-players: true` to make it public.

______________________________________________________________________

### `/psetlives <player> <amount>`

**Legacy Command** - Use `/psadmin lives` instead

This command still works but is deprecated in favor of the newer admin command structure.

**Permission:** `ssoggysouls.admin`

**Usage:**

```bash
/psetlives YourUsername 5
```text
______________________________________________________________________

### `/setlimbospawn`

Set the Limbo server spawn location to your current position. Must be executed on the Limbo server.

**Permission:** `ssoggysouls.admin`\
**Aliases:** None

**Usage:**

```bash
# Stand at desired location, then:
/setlimbospawn
```text
**Result:**

- Spawn location saved to config
- All dead players will spawn here upon transfer to Limbo
- Can be run multiple times to update location

______________________________________________________________________

## Command Examples

### Player Status Management

```bash
# Check your own status
/pstatus
Output: You - Lives: 2 - Status: Alive

# Check another player
/pstatus Steve
Output: Steve - Lives: 1 - Status: Dead

# A new player with grace period
/pstatus NewPlayer
Output: NewPlayer - Lives: 2 - Status: Alive (Grace: 23h 45m remaining)
```text
### Revival Operations

```bash
# Revive a teammate
/revive DeadPlayer

# Revive via admin command
/psadmin revive DeadPlayer

# Revive multiple players (run command multiple times)
/psadmin revive Player1
/psadmin revive Player2
/psadmin revive Player3
```text
### Life Management

```bash
# Give a player extra lives
/psadmin lives Player 5

# Give max lives
/psadmin lives Player 5

# Set to 1 life (critical)
/psadmin lives Player 1

# Emergency: set to 0 (sends to Limbo)
/psadmin kill Player
```text
### Grace Period Configuration

```bash
# Set 48-hour grace for new player
/psadmin grace NewPlayer 48

# Extended grace (72 hours = 3 days)
/psadmin grace LongGrace 72

# Remove grace period
/psadmin grace Player 0

# Check remaining grace
/pstatus Player
```text
### Player Information Lookup

```bash
# Get detailed player info
/psadmin info YourUsername

# Output includes:
# - UUID
# - Current lives
# - Death status
# - Last seen timestamp
# - Join date
# - Grace period remaining
```text
### Limbo Visits

```bash
# Alive player visits Limbo
/limbo
# You are teleported to Limbo server

# Return to Main
/leavelimbo
# or
/hub
# You are teleported back to Main server

# Note: Dead players cannot use /leavelimbo - only alive visitors can leave
```text
### Configuration Management

```bash
# Reload config after making changes
/psadmin reload

# Set new Limbo spawn point
/setlimbospawn
```text
______________________________________________________________________

## Permissions

| Permission             | Description                  | Default | Commands                                  |
| ---------------------- | ---------------------------- | ------- | ----------------------------------------- |
| `ssoggysouls.admin`    | Full admin access            | op      | All `/psadmin` commands, `/setlimbospawn` |
| `ssoggysouls.adminlog` | View admin logs              | op      | `/adminlog`                               |
| `ssoggysouls.revive`   | Revive dead players          | op      | `/revive`, `/psadmin revive`              |
| `ssoggysouls.status`   | Check player status          | true    | `/pstatus`                                |
| `ssoggysouls.visit`    | Visit Limbo as living player | true    | `/limbo`, `/leavelimbo`, `/hub`           |
| `ssoggysouls.bypass`   | Bypass all death mechanics   | false   | (Passive - no commands)                   |

### Setting Permissions (Example)

**Using LuckPerms:**

```bash
# Give admin permission
/lp user YourUsername permission set ssoggysouls.admin true

# Give revive permission only
/lp user ModName permission set ssoggysouls.revive true

# Remove admin permission
/lp user PlayerName permission set ssoggysouls.admin false
```text
**Using PermissionsEx:**

```bash
/pex user YourUsername add ssoggysouls.admin
/pex user ModName add ssoggysouls.revive
```text
______________________________________________________________________

## Tips & Tricks

### Batch Reviving Multiple Players

If you need to revive multiple dead players:

```bash
/psadmin revive Player1
/psadmin revive Player2
/psadmin revive Player3
```text
Each revive will succeed independently.

### Checking Before Revival

Always check player status before reviving:

```bash
/pstatus DeadPlayer
# If output shows "Status: Dead", they can be revived
/revive DeadPlayer
```text
### Grace Period Management

Monitor new players' grace periods:

```bash
/pstatus NewPlayer
# If grace remaining, they're protected from losing lives

# Extend grace if needed
/psadmin grace NewPlayer 48
```text
### Limbo Visiting Restrictions

- - Living players can visit Limbo anytime
- - Living players can return to Main anytime
- - Dead players cannot use `/leavelimbo`
- - Dead players cannot use `/limbo` to teleport away
- - Dead players must be revived to leave Limbo

______________________________________________________________________

## Command Help In-Game

For quick help in-game, many servers support:

```bash
/help ssoggysouls
/help revive
/help psadmin
```text
(Availability depends on your help plugin)

______________________________________________________________________

[← Back to Home](index) | [Configuration Reference →](configuration)
````
