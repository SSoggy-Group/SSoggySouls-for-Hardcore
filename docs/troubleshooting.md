---
layout: default
title: Troubleshooting
---

# Troubleshooting Guide

If you're running into issues setting up or running SSoggySouls, here is a practical guide to the most common problems and how to solve them.

---

## Table of Contents
1. [Players Not Transferring to Limbo](#1-players-are-not-transferring-to-limbo-on-death)
2. [Revivals Not Working](#2-revivals-are-not-working)
3. [Lives Lost During Grace Period](#3-new-players-are-losing-lives-during-their-grace-period)
4. [Admin Commands Failing on Limbo](#4-admin-commands-are-failing-on-the-limbo-server)
5. [Database Connection Issues](#5-console-shows-database-connection-errors-mysql)
6. [Plugin Not Loading](#6-plugin-not-loading)
7. [Getting Additional Help](#7-getting-additional-help)

---

## 1. Players are not transferring to Limbo on death

### Symptoms
- Players run out of lives but stay on the Main server.
- No automatic transfer to Limbo occurs, and there are no errors in the console.

### Solutions

#### Check your server roles
Make sure your Main and Limbo servers are configured properly in their respective `config.yml` files.
- **On your Main gameplay server:** `is-limbo-server` must be set to `false`.
- **On your Limbo purgatory server:** `is-limbo-server` must be set to `true`.

#### Match your server names with your proxy config
The server names in `config.yml` must match your `velocity.toml` (or BungeeCord config) server names exactly.
```yaml
# In config.yml on BOTH servers:
main-server-name: "main"
limbo-server-name: "limbo"
```
```toml
# In velocity.toml:
[servers]
  main = "localhost:25566"
  limbo = "localhost:25567"
```
*Note: These names are case-sensitive! if they don't match, the proxy won't know where to send the players.*

#### Verify database synchronization
If your servers can't talk to the same database, they won't know when players change status.
- Check both server console logs on startup for "Database connection established successfully".
- Ensure you copied the exact same MySQL database credentials (host, port, name, username, password) to both config files.

#### Check your player info forwarding
Your proxy must forward player information correctly to backend servers so UUIDs stay consistent.
- **For Velocity:** Open your `velocity.toml` and ensure `player-info-forwarding-mode = "modern"` is enabled.
- **For BungeeCord:** Ensure `ip_forward: true` is enabled in Bungee's config, and `bungeecord: true` is enabled in `spigot.yml` on both backend servers.

#### Check your death-mode setting
On your Main server's `config.yml`, make sure `main.death-mode` is set to `"hybrid"` or `"limbo"`. If it's set to `"spectator"`, players will remain on the Main server as spectators forever and will never transfer to Limbo. (SQLite databases force spectator mode automatically because there is no proxy transfer target.)

---

## 2. Revivals are not working

### Symptoms
- Placing a head on a ritual structure or running `/revive` doesn't bring the player back.
- Players stay dead in the database and remain stuck in spectator mode or on the Limbo server.

### Solutions

#### Verify that HRM features are enabled
Ensure the core revival systems are active in your configuration:
```yaml
hrm:
  enabled: true
  structure-revive: true  # Must be true for ritual structures to work
```

#### Check the ritual structure layout
Altar detection is precise. Make sure:
- The base is a 3x3 layout with Soul Sand at the 4 corners, and any stairs at the edges.
- There is a central ore block (like gold or diamond blocks).
- There is a fence post sitting directly *on top* of the ore block.
- There are 4 Wither Roses on top of the Soul Sand corners.
- You place the dead player's head directly on top of the central fence post.

#### Check database connection on Limbo
Limbo needs to check the database for revival entries.
- Ensure your Limbo server can connect to the shared database.
- Check the Limbo console logs on startup for database connections.
- Ensure `limbo.check-interval-seconds` is set to a reasonable number (defaults to 3 seconds).

---

## 3. New players are losing lives during their grace period

### Symptoms
- New players die and lose lives even though you have a grace period configured.

### Solutions

#### Verify the grace-period format
Open your `config.yml` and check `lives.grace-period`. Make sure it isn't set to `"0"` (which disables it completely).
It should be set using a time format, for example:
- `"24h"` (24 hours)
- `"2h30m"` (2 hours and 30 minutes)
- `"90m"` (90 minutes)

#### Note that grace period only counts online play
The grace period timer only decreases while the player is actively connected and playing on your server. If a player has a 24-hour grace period, plays for 2 hours, and logs off for a week, they will still have 22 hours of grace left when they reconnect. Run `/pstatus <player>` to view their remaining time.

#### Set grace periods manually for older players
If you enable a grace period on an existing server, players who have already joined in the past won't receive it automatically. You can manually assign them grace time using this admin command:
```text
/psadmin grace <player> 24
```

---

## 4. Admin commands are failing on the Limbo server

### Symptoms
- You are an Operator on your Limbo server, but typing `/revive` or `/psadmin` throws a security error.

### Solution
This is actually an **intended security feature**. By default, operators on the Limbo server are blocked from running administrative commands. This prevents a potential security gap where someone with OP access solely on your Limbo world could modify player data across your whole network.

If you have trusted admins who need access to commands on Limbo, you can bypass this check in a few ways:
- **Add them to the config whitelist:** Add their UUIDs or usernames directly to `limbo-trusted-admins` in your Limbo server's `config.yml`.
- **Grant permissions via LuckPerms:** Assign the `ssoggysouls.bypass-limbo-op-security` permission to their account or group on the Limbo server.
- **Run the commands from the server console:** Console commands are always trusted on both servers.

---

## 5. Console shows database connection errors (MySQL)

### Symptoms
- Startup logs show "Communications link failure", "Failed to connect to database", or "Access denied for user".

### Solutions

#### Verify MySQL is running
Make sure your database service is active on your host machine. If you are using Linux, you can verify its status:
```bash
sudo systemctl status mysql     # Or status mariadb
```

#### Test credentials locally
Verify your connection credentials by logging into your MySQL server manually from your server host terminal:
```bash
mysql -h <host> -P <port> -u <username> -p
```
If this login fails, your configured credentials, username, or password are incorrect.

#### Check your firewall rules
Ensure your database server's firewall allows incoming connections from your Minecraft backend servers' IP addresses (port 3306 by default). You can test connectivity from your Minecraft server host terminal using netcat:
```bash
nc -zv <database_host> 3306
```

#### Pterodactyl setup tip
If you host through a game panel like Pterodactyl, do not use `localhost` or `127.0.0.1` as your database host. Shared panels usually run databases on a dedicated external host address. Copy the exact connection string and host provided under your panel's "Databases" tab.

---

## 6. Plugin Not Loading

### Symptoms
- Plugin doesn't show in `/plugins`
- No console messages from SSoggySouls
- Commands don't work

### Solutions

#### Check Java Version
Requires Java 21 or higher. Check your running version in the host machine terminal:
```bash
java -version
```

#### Verify Minecraft Version
The plugin supports 1.21.X servers only. Check your server version with:
```text
/version
```

#### Check Console for Errors
Look closely at your server logs during startup for specific Java error codes:
- `UnsupportedClassVersionError` → Your Java version is too old. Upgrade to Java 21.
- `NoClassDefFoundError` → A dependency library is missing or failed to shadow properly.

#### Verify Plugin File is Not Corrupted
1. Re-download the JAR from Modrinth or GitHub.
2. Check that the file size matches exactly.
3. Replace the old JAR with the new one.

#### Check Plugins Folder
Ensure the JAR file is placed directly in the `plugins/` (or `mods/`) directory, not inside a subfolder.

---

## 7. Getting Additional Help

If your issue isn't covered in this guide:

1. **Enable Debug Mode** in `config.yml` on both servers:
   ```yaml
   debug: true
   ```
   Restart both servers and reproduce the issue to capture detailed logs.
2. **Collect Information:**
   - SSoggySouls version
   - Minecraft version
   - Proxy type (Velocity/BungeeCord)
   - Relevant config sections
   - Console errors/logs
   - Steps to reproduce
3. **Search Existing Issues**
   Check [GitHub Issues](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues) for similar problems.
4. **Open a New Issue**
   [Create an issue](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues/new) with all collected information.

---

[← Back to Home](index) | [FAQ Guide →](faq)
