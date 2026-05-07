---
layout: default
title: Quick Start Guide
---

## Quick Start Guide

This guide will help you get SSoggySouls up and running in 8 simple steps. For more detailed instructions, see the [Installation Guide](installation).

> **Note:** Fabric and Forge versions are currently in an early testing phase. Expect frequent updates and please report any bugs you find!

## Prerequisites

**For a Single Server Setup (Easy):**

- One Minecraft 1.21.X server (Spigot, Paper, Purpur, Fabric, or Forge)
- Java 21+

**For a 2-Server Setup (Advanced):**

- Two Minecraft 1.21.X servers (Spigot, Paper, Purpur, Fabric, or Forge)
- A Velocity Proxy connecting them
- MySQL 5.7+ / MariaDB 10.2+ database
- Java 21+

## Installation Steps

### Step 1: Download

Download the latest `SSoggySouls-4.3.4.jar` from:

- [GitHub Releases](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/releases)

- [Modrinth](https://modrinth.com/project/Pb03qu6T)

### Step 2: Install Plugin

Place `SSoggySouls-4.3.4.jar` in your server's `plugins/` folder.

If using a 2-server setup, install it on **both** backend servers:

- Main server: `/plugins/SSoggySouls-4.3.4.jar`
- Limbo server: `/plugins/SSoggySouls-4.3.4.jar`

> **Important:** If using a proxy, install on backend servers only, NOT on Velocity!

### Step 3: Generate Config

1. Start both servers to generate default `config.yml` files
1. Stop both servers after generation

You should see a `plugins/SSoggySouls/config.yml` file on each server.

### Step 4: Configure Database

**Option A: Single Server (SQLite)**
If you only run one server, just verify your `config.yml` is set to `sqlite`:

```yaml
database:
  type: "sqlite"
```

**Option B: 2-Server Setup (MySQL)**
If you run Main + Limbo, you need one MySQL database. Set it up once, then put the same connection details in `config.yml` on **both servers**:

```yaml
database:
  type: "mysql"
  host: "localhost"        # Your database host
  port: 3306               # Your database port
  name: "ssoggysouls"       # Your database name
  username: "root"         # Your MySQL username
  password: "your_password" # Your MySQL password
  pool-size: 5
  table-name: "hardcore_players"
```

**For Pterodactyl users:**

1. Go to your panel → Databases tab
1. Create a new database
1. Use the provided host, port, username, and password in config

### Step 5: Configure Server Roles (2-Server Setup Only)

If you are only running a single server with SQLite, skip this step — leave the defaults.

**On Main server (`config.yml`):**

```yaml
is-limbo-server: false        # This is the Main server

main-server-name: "main"      # Must match your Velocity config

limbo-server-name: "limbo"    # Must match your Velocity config

```

**On Limbo server (`config.yml`):**

```yaml
is-limbo-server: true         # This is the Limbo server

main-server-name: "main"      # Must match your Velocity config

limbo-server-name: "limbo"    # Must match your Velocity config

```

> **Tip:** The server names must exactly match the server names in your `velocity.toml` file.

### Step 6: Set Limbo Spawn

1. Start the Limbo server
1. Join the Limbo server in-game
1. Stand where you want dead players to spawn
1. Run `/setlimbospawn`
1. Verify the spawn location is saved in config

### Step 7: Configure Proxy Forwarding

**For Velocity:**

Edit `velocity.toml`:

```toml
player-info-forwarding-mode = "modern"

```

**For BungeeCord/Waterfall:**

- Enable IP forwarding in BungeeCord's `config.yml`

- Set `bungeecord: true` in `spigot.yml` on both backend servers

- Configure Paper forwarding if using Paper

### Step 8: Restart & Test

1. Restart both servers
1. Check console for successful database connection messages
1. Test the complete flow:
   - Join the Main server

   - Use `/pstatus` to check your lives

   - Kill yourself enough times to lose all lives

   - Verify you're transferred to Limbo

   - Use `/psadmin revive <player>` to revive yourself

   - Verify you're returned to Main

## What's Next?

### Customize Your Settings

Review and customize these important settings in `config.yml`:

- **Lives settings** (`default`, `max-lives`, `on-revive`)

- **Death mode** (`limbo`, `spectator`, or `hybrid`)

- **Grace period** duration for new players

- **Extra Life recipe** materials

- **Messages** and colors

See the [Configuration Reference](configuration) for details on all options.

### Learn About Features

- [Revival System](revival-system) - Learn how to revive players using ritual structures and items

- [Commands](commands) - Full list of available commands

- [Death Modes](configuration.md#death-modes) - Understand the three death mode options

### Troubleshooting

Having issues? Check the [Troubleshooting Guide](troubleshooting) for solutions to common problems.

## Quick Reference

### Death Modes

| Mode                 | Behavior                                                                              |
| -------------------- | ------------------------------------------------------------------------------------- |

| **hybrid** (default) | Dead players get 5 minutes in spectator mode to be revived, then transferred to Limbo |

| **spectator**        | Dead players stay on Main in spectator mode indefinitely until revived                |

| **limbo**            | Dead players immediately transferred to Limbo upon losing all lives                   |

### Essential Commands

| Command                            | Description            |
| ---------------------------------- | ---------------------- |

| `/pstatus [player]`                | Check lives and status |
| `/revive <player>`                 | Revive a dead player   |
| `/psadmin lives <player> <amount>` | Set player's lives     |
| `/psadmin grace <player> <hours>`  | Set grace period       |
| `/setlimbospawn`                   | Set Limbo spawn point  |

For the complete command list, see [Commands](commands).

## Need Help?

- [Full Installation Guide](installation)

- [Configuration Reference](configuration)

- [Troubleshooting](troubleshooting)

- [FAQ](faq)

- [Report Issues](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues)

______________________________________________________________________

[← Back to Home](index) | [Installation Guide →](installation)
