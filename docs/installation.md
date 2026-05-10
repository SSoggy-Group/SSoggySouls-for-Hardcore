---
layout: default
title: Installation Guide
---

# Installation Guide

This comprehensive guide covers everything you need to know to properly install and configure SSoggySouls. Whether you're running a single server or a full Velocity proxy network with Main + Limbo, this guide has you covered.

## Table of Contents

1. [Prerequisites](#prerequisites)
1. [Network Architecture](#network-architecture)
1. [Database Setup](#database-setup)
1. [Plugin Installation](#plugin-installation)
1. [Proxy Configuration](#proxy-configuration)
1. [Server Configuration](#server-configuration)
1. [Testing Your Setup](#testing-your-setup)
1. [Common Mistakes to Avoid](#common-mistakes-to-avoid)

## Prerequisites

### Server Requirements

- **Minecraft Version:** 1.21.X

- **Server Software:** Spigot, Paper, Purpur, Fabric, Forge, or NeoForge

- **Java Version:** 21 or higher

- **Database:** SQLite (built-in, zero setup) for single server OR MySQL 5.7+ / MariaDB 10.2+ for multi-server

- **Proxy Software:** Velocity (only needed for 2-server setup; BungeeCord/Waterfall untested but may work)

### Server Setup

**Single Server:** Just one Minecraft server. Dead players enter spectator mode. No proxy, no Limbo server, no MySQL needed.

**2-Server Setup:** Two backend servers behind a Velocity proxy:

- **Main Server** - Your primary survival/gameplay server

- **Limbo Server** - Purgatory server for dead players

> **Note:** If using `spectator` death mode on a single server, the Limbo server is not needed.

### Important: Do NOT Use Hardcore Mode

**CRITICAL:** Do **NOT** enable `hardcore=true` in `server.properties` on either server.

Leave it as `false`. The plugin manages hardcore mechanics internally - enabling Minecraft's built-in hardcore mode will break the plugin.

If you already have it enabled:

- Delete your world and regenerate, OR

- Use an NBT editor to change the hardcore flag in `level.dat`

## Network Architecture

### How SSoggySouls Works

```text

                    ┌─────────────────┐
                    │  Velocity Proxy │
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
        ┌─────▼─────┐                 ┌─────▼─────┐
        │   Main    │◄───────────────►│   Limbo   │
        │  Server   │   MySQL/MariaDB │  Server   │
        └───────────┘                 └───────────┘

```

- Both servers connect to the same MySQL database (one database, shared by both)

- Players automatically transfer between servers based on death state

- Limbo server checks database periodically for revivals

- Main server handles deaths and sends players to Limbo

## Database Setup

The plugin supports two database types. Your choice here determines your entire setup:

| Feature                          | `sqlite` (Local)                 | `mysql` (External)                         |
| -------------------------------- | -------------------------------- | ------------------------------------------ |

| **Best For**                     | Single servers                   | 2-Server Proxy Networks                    |

| **Performance**                  | High (Lightweight / Local I/O)   | Maximum (Multi-threaded Dedicated Service) |

| **Network Latency**              | None (Zero Ping)                 | Varies (Dependent on Host)                 |

| **Setup Required**               | None (Plug & Play)               | Advanced (User/Grants)                     |

| **Lives & HRM Systems**          | ✅ Works Perfectly               | ✅ Works Perfectly                         |

| **Main + Limbo Syncing**         | ❌ **NOT SUPPORTED**             | ✅ Supported                               |

| **Plugin Sharing (CoreProtect)** | ❌ No                            | ✅ Yes (Same Database)                     |

| **Data Editing / Viewing**       | Requires 3rd Party SQLite Editor | PhpMyAdmin / Pterodactyl Panels            |

| **Backups**                      | Copy `database.db` file          | Requires `mysqldump`                       |

| **Portability**                  | High (Single `.db` file)         | Low (External Server)                      |

### Option A: SQLite (Zero Setup / Local)

Ideal for a single server setup if you just want lives, HRM features, and extra-life items without worrying about setting up an external database.

- **No external server required**

- **Zero configuration needed**

- The plugin handles the creation of `database.db` inside its data folder automatically.

- *Note:* Does not support true cross-server exile functionality natively.

### Option B: MySQL (Cross-server)

REQUIRED if you are using the 2-server (Main + Limbo) functionality. You set up **one** MySQL database, then both servers connect to it using the same credentials.

SSoggySouls will automatically create the necessary table, but you need to create the database first.

**Using MySQL command line:**

```sql
CREATE DATABASE ssoggysouls CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ssoggysouls_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER ON ssoggysouls.* TO 'ssoggysouls_user'@'localhost';

FLUSH PRIVILEGES;

```

**For shared hosting (Pterodactyl, etc.):**

1. Go to your hosting panel
1. Navigate to the Databases section
1. Create a new database
1. Note down the host, port, database name, username, and password

### Database Configuration

If using SQLite, simply set `type: "sqlite"` and ignore the MySQL fields — the plugin handles everything.

If using MySQL, set up one database and then copy the **same** connection details into both server configs:

```yaml
database:
  type: "mysql"                  # Database type: "mysql" or "sqlite"

  host: "localhost"              # Database host (use panel host for Pterodactyl)

  port: 3306                     # Database port

  name: "ssoggysouls"             # Database name

  username: "ssoggysouls_user"    # Database username

  password: "your_secure_password" # Database password

  pool-size: 5                   # Connection pool size (5 is recommended)

  table-name: "hardcore_players" # Table name (default is fine)

```

> **Tip:** The database can be shared with other plugins like CoreProtect. SSoggySouls uses its own table.

### Connection Pool Settings (MySQL only)

The `pool-size` setting controls how many database connections are maintained:

- **Small servers (1-20 players):** 5 connections

- **Medium servers (20-50 players):** 10 connections

- **Large servers (50+ players):** 15-20 connections

## Plugin Installation

### Download

Get the latest version:

- [GitHub Releases](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/releases)

- [Modrinth](https://modrinth.com/project/Pb03qu6T)

Download `SSoggySouls-4.4.24.jar` (or latest version).

### Installation Steps

1. **Stop your server(s)** if they are running.

1. **Place the JAR file** in the `plugins/` folder:

**For Single Server Setups:**

```text
Server: /plugins/SSoggySouls-4.4.24.jar
```

**For 2-Server Setups (Main + Limbo):**

```text
Main Server: /plugins/SSoggySouls-4.4.24.jar
Limbo Server: /plugins/SSoggySouls-4.4.24.jar
```

1. **Start your server(s)** to generate config files

1. **Stop your server(s)** to edit configuration

1. **Edit `config.yml`** (see next section)

### Critical Setup Rules

**DO NOT install on proxy:**

- SSoggySouls is NOT a proxy plugin

- Install ONLY on backend servers (Main and Limbo)

- Do NOT place in Velocity/BungeeCord plugins folder

- Both servers must use the SAME version

______________________________________________________________________

> **Note:** For singleplayer, simply place the JAR in your client's `mods` folder. Limbo features are disabled in singleplayer automatically.

______________________________________________________________________

## Forge & NeoForge Mod Installation

The Forge and NeoForge versions are installed similarly to the Bukkit version, but use the `mods/` folder.

### Requirements

- **Forge or NeoForge Loader** installed on your server (1.21.1).

### Installation Steps

1. Place `SSoggySouls-Forge-X.X.X.jar` or `SSoggySouls-NeoForge-X.X.X.jar` into the `mods/` folder.
2. Start the server to generate `config/ssoggysouls.toml` (or `ssoggysouls.json` depending on build).
3. Stop the server and configure as needed.

> **Note:** Fabric, Forge, and NeoForge versions are currently in an early testing phase. Expect frequent updates and please report any bugs you find!

______________________________________________________________________

**DO install on both backend servers (if using Limbo):**

- Must be installed on Main server
- Must be installed on Limbo server
- Both servers must use the SAME version

## Proxy Configuration

### For Velocity

Edit `velocity.toml`:

```toml

# Enable modern forwarding

player-info-forwarding-mode = "modern"

[servers]
main = "localhost:25566"   # Your Main server

limbo = "localhost:25567"  # Your Limbo server

try = [
"main"  # Players join Main by default

]

```

### For BungeeCord/Waterfall

> **Note:** BungeeCord/Waterfall support is untested. Please [report](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues) if you test it!

**In BungeeCord `config.yml`:**

```yaml
ip_forward: true

```

**On both backend servers in `spigot.yml`:**

```yaml
settings:
  bungeecord: true

```

**If using Paper, also configure `paper.yml`:**

```yaml
settings:
  velocity-support:
    enabled: false  # Disable for BungeeCord

```

## Server Configuration

> **Note for Single Servers (SQLite):** If you are running a single server with SQLite, you can safely skip the "Server Role" sections below. Just leave `is-limbo-server` and the server names at their defaults — the plugin ignores them when using SQLite.

### Main Server Configuration

Edit `/plugins/SSoggySouls/config.yml` on the **Main server**:

```yaml

# SERVER IDENTIFICATION

is-limbo-server: false           # This is the Main server

# SERVER NAMES (must match velocity.toml)

main-server-name: "main"         # Name of Main server in proxy config

limbo-server-name: "limbo"       # Name of Limbo server in proxy config

# DATABASE (set up one MySQL database, then use the same details here and on Limbo)

database:
  type: "mysql"
  host: "localhost"
  port: 3306
  name: "ssoggysouls"
  username: "ssoggysouls_user"
  password: "your_secure_password"
  pool-size: 5
  table-name: "hardcore_players"

# LIVES SYSTEM

lives:
  default: 2                     # Starting lives for new players

  max-lives: 5                   # Maximum lives cap

  on-revive: 1                   # Lives restored on revival

  grace-period: "24h"            # New player protection

  revive-cooldown-seconds: 30    # Post-revival protection

# DEATH HANDLING

main:
  death-mode: "spectator"        # SQLite uses spectator; MySQL can use hybrid | spectator | limbo

  hybrid-timeout-seconds: 300    # 5 minutes for hybrid mode

  spectator-on-death: false
  detect-hrm-revive: true        # Enable ritual structure detection

  send-to-limbo-delay-ticks: 20  # 1 second delay before transfer

```

### Limbo Server Configuration

Edit `/plugins/SSoggySouls/config.yml` on the **Limbo server**:

```yaml

# SERVER IDENTIFICATION

is-limbo-server: true            # This is the Limbo server

# SERVER NAMES (must match velocity.toml and Main server config)

main-server-name: "main"
limbo-server-name: "limbo"

# DATABASE (must be identical to Main server)

database:
  type: "mysql"
  host: "localhost"
  port: 3306
  name: "ssoggysouls"
  username: "ssoggysouls_user"
  password: "your_secure_password"
  pool-size: 5
  table-name: "hardcore_players"

# LIMBO SETTINGS

limbo:
  check-interval-seconds: 3      # How often to check for revivals

  spawn:
    world: "world"               # Set with /setlimbospawn command

    x: 0.5
    y: 65.0
    z: 0.5
    yaw: 0.0
    pitch: 0.0

```

### Setting Limbo Spawn

1. Start the Limbo server
1. Join the server in-game
1. Stand at the desired spawn location
1. Run `/setlimbospawn`
1. Confirm the coordinates were saved

The spawn location is saved to config and will be used for all dead players.

## Testing Your Setup

### Pre-Flight Checklist

Before testing, verify:

- [ ] Both servers are running SSoggySouls (same version)

- [ ] Both servers have identical database credentials

- [ ] `is-limbo-server` is set correctly (false on Main, true on Limbo)

- [ ] Server names match proxy configuration

- [ ] Limbo spawn has been set

- [ ] Console shows successful database connection on both servers

### Test Procedure

1. **Join Main Server**

- Connect through the proxy

- You should spawn on the Main server

1. **Check Initial Status**

```text

/pstatus

```

Should show: "Lives: 2 - Status: Alive"

1. **Test Death Mechanic**

- Kill yourself (fall, lava, etc.)

- Check lives with `/pstatus`

- Should show: "Lives: 1 - Status: Alive"

1. **Test Limbo Transfer**

- Kill yourself again to lose all lives

- Depending on death mode:

  - **hybrid:** Enter spectator for 5 minutes, then transfer to Limbo (MySQL/proxy only)

  - **spectator:** Enter spectator on Main indefinitely (SQLite default)

  - **limbo:** Immediately transfer to Limbo

- Verify you're on the Limbo server

1. **Test Revival**

- From Main server console, run:

  ```text

  /psadmin revive <your_username>
  ```

- Should automatically return to Main server

- Check lives: Should have 1 life restored

1. **Test Grace Period**

- Create a new player account

- Check status: `/pstatus`

- Should show grace period remaining

- Die several times

- Lives should not decrease during grace period

### Verification

If all tests pass, your installation is complete!

Check console logs for:

- "Database connection established successfully"

- "SSoggySouls version X.X.X enabled"

- No error messages or warnings

## Common Mistakes to Avoid

### Mistake 1: Installing on Proxy

**Wrong:** Installing SSoggySouls on Velocity/BungeeCord

**Right:** Install only on backend servers (Main and Limbo)

### Mistake 2: Different Database Credentials

**Wrong:** Using different database credentials on each server

**Right:** Set up one MySQL database and copy the same credentials to both configs

### Mistake 3: Server Names Don't Match

**Wrong:** Config says "main" but velocity.toml says "survival"

**Right:** Server names must exactly match proxy configuration

### Mistake 4: Wrong is-limbo-server Setting

**Wrong:** Both servers have `is-limbo-server: false`

**Right:** Main = false, Limbo = true

### Mistake 5: Enabling Hardcore Mode

**Wrong:** Setting `hardcore=true` in `server.properties`

**Right:** Keep `hardcore=false` - plugin manages hardcore mechanics

### Mistake 6: Different Plugin Versions

**Wrong:** Main server has v3.2.6, Limbo has v4.4.24

**Right:** Both servers must use the exact same version

### Mistake 7: Not Setting Limbo Spawn

**Wrong:** Forgetting to run `/setlimbospawn`

**Right:** Set spawn point before testing

### Mistake 8: Firewall Blocking Database

**Wrong:** Servers can't reach database due to firewall

**Right:** Ensure firewall allows backend → database connections

## Next Steps

Now that SSoggySouls is installed:

1. **Customize Settings** - Review the [Configuration Reference](configuration)

1. **Learn Commands** - Check out the [Commands Guide](commands)

1. **Understand Revival System** - Read the [Revival System Guide](revival-system)

1. **Prepare for Issues** - Familiarize yourself with [Troubleshooting](troubleshooting)

## Need Help?

- [Configuration Reference](configuration)

- [Troubleshooting Guide](troubleshooting)

- [FAQ](faq)

- [Report Issues](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues)

______________________________________________________________________

[← Quick Start](quick-start) | [Back to Home](index) | [Configuration →](configuration)
