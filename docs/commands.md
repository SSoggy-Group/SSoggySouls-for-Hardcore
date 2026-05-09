---
layout: default
title: Commands Reference
---

# Commands Reference

Here is a full list of commands available in SSoggySouls, complete with examples and permission setups for both players and administrators.

---

## Table of Contents

1. [Player Commands](#player-commands)
2. [Admin Commands](#admin-commands)
3. [Command Examples & Usage Scenarios](#command-examples--usage-scenarios)
4. [Permission Nodes Summary](#permission-nodes-summary)
5. [Troubleshooting & Tips](#troubleshooting--tips)

---

## Player Commands

These commands are available to standard players by default (or with minimal permissions).

### `/pstatus [player]`
Check how many lives you (or another player) have left, your current status, and whether your grace period is still active.

- **Permission:** `ssoggysouls.status` (Defaults to true for all players)
- **Usage:**
  ```bash
  /pstatus           # Check your own status
  /pstatus Notch     # Check Notch's status
  ```
- **Example Output:**
  ```text
  Notch - Lives: 2 - Status: Alive
  ```

### `/revive <player>`
Revive a dead teammate and pull them back from Limbo to the Main server. Useful if you want to let moderators or specific players revive others without giving them full admin commands.

- **Permission:** `ssoggysouls.revive` (Defaults to operator only)
- **Usage:**
  ```bash
  /revive Steve
  ```

### `/limbo` (or `/visitlimbo`)
Visit the Limbo server as a living player so you can chat and hang out with dead teammates.

- **Permission:** `ssoggysouls.visit` (Defaults to true for all players)
- **Usage:**
  ```bash
  /limbo
  ```
- **Note:** Living players can visit and leave whenever they want, but dead players are stuck in Limbo until they are revived.

### `/leavelimbo` (or `/hub`)
Return to the Main server after visiting Limbo. This command will *only* work if you are alive.

- **Permission:** `ssoggysouls.visit` (Defaults to true for all players)
- **Usage:**
  ```bash
  /leavelimbo
  ```

---

## Admin Commands

These commands require administrator access and the `ssoggysouls.admin` permission node (defaults to operator). You can also shorten `/psadmin` to `/psa` in-game.

### `/psadmin lives <player> <amount>`
Directly set a player's life count. Setting a player to `0` lives will trigger the death mechanics and send them to spectator or Limbo.

- **Usage:**
  ```bash
  /psadmin lives Notch 5    # Set lives to 5
  /psadmin lives Steve 0    # Set lives to 0 (kills them)
  ```

### `/psadmin revive <player>`
Revive a dead player. This does the exact same thing as `/revive <player>` but uses the admin command hierarchy.

- **Usage:**
  ```bash
  /psadmin revive Steve
  ```

### `/psadmin kill <player>`
Force-kill a player immediately. This sets their lives to 0 and sends them straight to Limbo or spectator mode depending on your death mode configuration.

- **Usage:**
  ```bash
  /psadmin kill Steve
  ```

### `/psadmin grace <player> <hours>`
Manually set or override a player's grace period.

- **Usage:**
  ```bash
  /psadmin grace Steve 24   # Give Steve 24 hours of grace
  /psadmin grace Steve 0    # Remove Steve's grace period completely
  ```

### `/psadmin reset <player>`
Reset a player's data back to defaults. This restores their starting lives, clears any remaining grace period, clears their revive cooldown, and sets their status to alive.

- **Usage:**
  ```bash
  /psadmin reset Steve
  ```

### `/psadmin info <player>`
Look up detailed, backend player information. This is useful for checking joining timestamps, UUIDs, and exact status records.

- **Usage:**
  ```bash
  /psadmin info Steve
  ```
- **Example Output:**
  ```text
  === Player Information ===
  Username: Steve
  UUID: a1b2c3d4-e5f6-7890-abcd-ef1234567890
  Lives: 2
  Status: Alive
  Last Seen: 2026-05-09 10:12:00
  Joined: 2026-05-01 08:15:30
  Grace Period Remaining: 18h 45m
  ```

### `/psadmin reload`
Reload the `config.yml` configuration file directly from the disk. This allows you to apply most changes without restarting the server.

- **Usage:**
  ```bash
  /psadmin reload
  ```

### `/setlimbospawn`
Set the spawn point for dead players on the Limbo server. Make sure to run this while standing on your Limbo server instance!

- **Usage:**
  ```bash
  /setlimbospawn
  ```

### `/adminlog [lines]`
View recent admin actions (such as setting lives, reviving, or force-killing) directly in-game. It reads the last 15 lines by default.

- **Permission:** `ssoggysouls.adminlog` (Defaults to operator only)
- **Usage:**
  ```bash
  /adminlog       # Reads last 15 actions
  /adminlog 25    # Reads last 25 actions
  ```
- **Note:** You can allow non-operators to view logs by adding them to `admin-log.trusted-viewers` in the config file.

### `/psetlives <player> <amount>`
Directly set a player's life count (alias for `/psadmin lives <player> <amount>`).

- **Usage:**
  ```bash
  /psetlives Steve 3
  ```

---

## Command Examples & Usage Scenarios

### Scenario 1: Checking player status before a revival
Always double check status records before triggering a revival block:
```bash
/pstatus Steve
```
If the status output reads `Steve - Lives: 0 - Status: Dead`, proceed to revive:
```bash
/revive Steve
```

### Scenario 2: Overriding a player's grace period
To give an older player 12 hours of protection, or remove grace for a testing account:
```bash
/psadmin grace Notch 12    # Give 12 hours of protection
/psadmin grace Steve 0     # Remove protection
```

### Scenario 3: Granting a moderator access to /revive
If you want to allow a Moderator to revive dead players without granting them full admin access, assign them the `ssoggysouls.revive` node in LuckPerms:
```bash
/lp group moderator permission set ssoggysouls.revive true
```

---

## Permission Nodes Summary

If you are using a permission plugin like LuckPerms, here are the nodes you can assign to players or groups:

| Permission Node | Description | Default Group |
|---|---|---|
| `ssoggysouls.status` | Allows checking player lives using `/pstatus` | Anyone (`true`) |
| `ssoggysouls.visit` | Allows visiting Limbo using `/limbo` or `/leavelimbo` | Anyone (`true`) |
| `ssoggysouls.revive` | Allows reviving players using `/revive` | Operator (`op`) |
| `ssoggysouls.admin` | Access to all `/psadmin` and `/setlimbospawn` commands | Operator (`op`) |
| `ssoggysouls.adminlog` | Allows viewing admin action history with `/adminlog` | Operator (`op`) |
| `ssoggysouls.bypass` | Bypasses all death and lives mechanics entirely | Nobody (`false`) |
| `ssoggysouls.bypass-limbo-op-security` | Allows operators to run admin commands on Limbo | Nobody (`false`) |

---

## Troubleshooting & Tips

- **Command not recognized:** Ensure you have installed the same JAR version on the backend server you are playing on.
- **Can't use admin commands on Limbo:** This is a security check. Put your UUID in the `limbo-trusted-admins` list in the Limbo server's `config.yml` or grant yourself `ssoggysouls.bypass-limbo-op-security`.
- **LuckPerms context:** Note that if you are using LuckPerms across a network, you can define permissions per-server or globally.

---

[← Back to Home](index) | [Configuration Reference →](configuration)
