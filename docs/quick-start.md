---
layout: default
title: Quick Start Guide
---

## Quick Start Guide

This guide will get SSoggySouls up and running on your server as quickly as possible. For a more detailed walkthrough, see our full [Installation Guide](installation).

> **Note:** Fabric and Forge versions are still in early testing. Expect frequent updates, and please report any bugs you find on GitHub!

## Prerequisites

Before getting started, make sure you meet the requirements for your chosen setup:

### Single Server Setup (Standalone)
- One Minecraft 1.21.X server (Spigot, Paper, Purpur, Fabric, or Forge)
- Java 21 or newer

### 2-Server Setup (Main + Limbo Network)
- Two Minecraft 1.21.X servers (one for active play, one for Limbo)
- A Velocity proxy to connect them
- A MySQL 5.7+ or MariaDB 10.2+ database
- Java 21 or newer

---

## Installation Steps

### Step 1: Download the plugin
Grab the latest `SSoggySouls-4.4.8.jar` from:
- [GitHub Releases](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/releases)
- [Modrinth](https://modrinth.com/project/Pb03qu6T)

### Step 2: Drop it in your plugins folder
Put the JAR in your server's `plugins/` (or `mods/`) folder.

If you are running a 2-server setup, you must install the JAR on **both** backend servers:
- Main server: `/plugins/SSoggySouls-4.4.8.jar`
- Limbo server: `/plugins/SSoggySouls-4.4.8.jar`

> **Important:** Do NOT install the plugin on your proxy (Velocity/BungeeCord). It only goes on the backend servers.

### Step 3: Generate the default config
1. Start up your server(s) so the default configuration can generate.
2. Once the server finishes starting up, stop it.
3. You will now find a `plugins/SSoggySouls/config.yml` file on each server.

### Step 4: Configure your database

#### Option A: Standalone Single Server (SQLite)
If you're running just one server, make sure the database type is set to `sqlite` in your `config.yml`. No other database settings are needed:
```yaml
database:
  type: "sqlite"
```

#### Option B: 2-Server Network Setup (MySQL)
If you are running a Main + Limbo proxy setup, you need a shared MySQL database. Set up your database, then put these identical connection details into `config.yml` on **both servers**:
```yaml
database:
  type: "mysql"
  host: "localhost"        # Change this to your DB host
  port: 3306
  name: "ssoggysouls"
  username: "root"
  password: "your_password" # Change this to your actual password
  pool-size: 5
  table-name: "hardcore_players"
```
*(If you are hosting through a panel like Pterodactyl, copy the database connection details from your panel's Databases tab.)*

### Step 5: Configure server roles (Only for 2-Server networks)
If you're on a single standalone server with SQLite, skip this step.

**On your Main server (`config.yml`):**
```yaml
is-limbo-server: false
main-server-name: "main"      # Must match your velocity.toml server name
limbo-server-name: "limbo"    # Must match your velocity.toml server name
```

**On your Limbo server (`config.yml`):**
```yaml
is-limbo-server: true
main-server-name: "main"      # Must match your velocity.toml server name
limbo-server-name: "limbo"    # Must match your velocity.toml server name
```
*Tip: Ensure these names match the server names in your `velocity.toml` configuration exactly.*

### Step 6: Set the Limbo spawn point (Only for Limbo servers)
1. Start your Limbo server.
2. Join the Limbo server in-game.
3. Stand exactly where you want dead players to spawn.
4. Type `/setlimbospawn`.
5. The coordinates will save directly to your configuration.

### Step 7: Configure proxy forwarding
Make sure player info forwarding is set up correctly so UUIDs are shared properly.

**For Velocity:**
Open `velocity.toml` and verify this line:
```toml
player-info-forwarding-mode = "modern"
```

**For BungeeCord/Waterfall:**
- Set `ip_forward: true` in your BungeeCord `config.yml`.
- Set `bungeecord: true` in `spigot.yml` on both backend servers.
- If using Paper, disable Velocity support in `paper.yml` to prevent conflicts.

### Step 8: Restart and test it out!
1. Start both servers up.
2. Check your console logs to make sure the database connected successfully.
3. Join your Main server and test the flow:
   - Join the Main server.
   - Run `/pstatus` to view your current lives.
   - Kill your character until you run out of lives.
   - Verify you are sent to the spectator mode or exiled to Limbo (depending on your setup).
   - From your console, type `psadmin revive <your_name>` to bring yourself back.
   - Check that you are sent back to the Main server with 1 life.

---

## What's Next?

Now that the basics are working, you can customize the gameplay settings in `config.yml`:
- **Lives & Cap:** Set the starting lives, maximum lives, and lives restored on revival.
- **Death Modes:** Choose between `limbo` (immediate transfer), `spectator` (stay on Main in spectator mode), or `hybrid` (spectate on Main for a few minutes before transferring).
- **Grace Period:** Give new players some breathing room before deaths start counting.
- **Recipes:** Customize the crafting ingredients for Extra Lives.

For full configuration options, head over to the [Configuration Reference](configuration).

---

## Quick Reference

### Death Modes

| Mode | Behavior |
|---|---|
| **`hybrid`** | Dead players get 5 minutes in spectator mode to be revived, then transferred to Limbo. |
| **`spectator`** | Dead players stay on Main in spectator mode indefinitely until revived (SQLite default). |
| **`limbo`** | Dead players are immediately transferred to Limbo upon losing all lives. |

### Essential Commands

| Command | Description |
|---|---|
| `/pstatus [player]` | Check lives and status |
| `/revive <player>` | Revive a dead player |
| `/psadmin lives <player> <amount>` | Set player's lives |
| `/psadmin grace <player> <hours>` | Set grace period |
| `/setlimbospawn` | Set Limbo spawn point |

For the complete command list, see [Commands](commands).

---

[← Back to Home](index) | [Installation Guide →](installation)
