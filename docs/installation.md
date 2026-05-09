---
layout: default
title: Installation Guide
---

# Installation Guide

This guide will walk you through installing and configuring SSoggySouls step-by-step. Whether you're running a single standalone server or a multi-server Velocity proxy network with a dedicated Limbo world, we'll cover everything you need to get things running smoothly.

## Prerequisites

Before we start copying files, make sure your environment is ready:

### Server Requirements
- **Minecraft Version:** 1.21.X
- **Server Software:** Spigot, Paper, Purpur, Fabric, or Forge
- **Java Version:** Java 21 or newer
- **Database:** SQLite (built-in, local, zero setup) for standalone servers, OR MySQL 5.7+ / MariaDB 10.2+ for multi-server setups
- **Proxy Software (Optional):** Velocity (only needed if you're linking two servers together; BungeeCord/Waterfall are untested but might work)

### Choosing Your Setup Style

#### 1. Single Server Setup (easiest)
Everything runs on a single server. When players run out of lives, they are put into spectator mode. This is extremely simple to set up, requires no extra servers, and uses a local SQLite database that the plugin manages automatically.

#### 2. Multi-Server Setup (advanced)
We run two separate backend servers behind a Velocity proxy:
- **Main Server:** The primary survival/gameplay server where players spend their time.
- **Limbo Server:** A dedicated purgatory server where players are transferred when they lose all their lives.
This setup uses a shared MySQL/MariaDB database to keep player data and revival requests in sync.

> **Crucial Rule:** Make sure `hardcore=false` is set in your `server.properties` file on all servers. SSoggySouls manages the hardcore and spectator mechanics internally, and enabling vanilla hardcore will break the plugin.

---

## Network Architecture (Multi-Server Setup)

Here's a quick look at how the data and player connections flow when using a 2-server setup:

```text
                    ┌─────────────────┐
                    │  Velocity Proxy │
                    └────────┬────────┘
                             │ (Routes players)
              ┌──────────────┴──────────────┐
              │                             │
        ┌─────▼─────┐                 ┌─────▼─────┐
        │   Main    │◄───────────────►│   Limbo   │
        │  Server   │   MySQL Sync    │  Server   │
        └───────────┘                 └───────────┘
```

- Both servers connect to the exact same MySQL database.
- When players lose their lives on the Main server, the plugin tells the proxy to move them to Limbo.
- While in Limbo, the server checks the database every few seconds for revival entries.
- Once a player is revived (via a ritual or command on Main), the database updates and Limbo safely routes them back to Main.

---

## Database Setup

Your choice of database determines whether you can link servers together or keep things standalone:

| Feature | SQLite (Local) | MySQL / MariaDB (Shared) |
|---|---|---|
| **Best For** | Standalone Single Servers | Linked Multi-Server Proxy Networks |
| **Setup Effort** | Zero (Plug-and-play) | Advanced (Creating databases and user grants) |
| **Sharing Data** | ❌ Standalone only | ✅ Shared between multiple backend servers |
| **How to Edit Data** | Requires a local SQLite browser | PhpMyAdmin, Pterodactyl panel, or any DB client |
| **How to Backup** | Just copy the `database.db` file | Run `mysqldump` or use host backup tools |

### Option A: SQLite (Standalone)
Ideal if you want to run a single server. The plugin handles everything automatically and generates a `database.db` file in the plugin data folder on startup. You don't need to install or run any external database software.

### Option B: MySQL (Linked Network)
Required if you want the Main + Limbo server setup. You must set up **one** MySQL database that both backend servers can access.

If you have shell access to your database server, you can set it up with these SQL commands:
```sql
CREATE DATABASE ssoggysouls CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ssoggysouls_user'@'%' IDENTIFIED BY 'your_secure_password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER ON ssoggysouls.* TO 'ssoggysouls_user'@'%';
FLUSH PRIVILEGES;
```
*(Tip: Replace `%` with your Minecraft servers' actual IP addresses to keep things secure!)*

If you are using shared game hosting (like Pterodactyl):
1. Head over to your hosting panel and click the **Databases** tab.
2. Click **Create Database**.
3. Note down the database name, host, port, username, and password. You will need to put these into the configuration files.

---

## Plugin Installation

### Step 1: Download the JAR
Grab the latest release file (`SSoggySouls-4.4.8.jar`) from the [GitHub Releases](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/releases) or [Modrinth](https://modrinth.com/project/Pb03qu6T).

### Step 2: Install the files
Stop your Minecraft server(s) if they are currently running.

**For Standalone (Single Server):**
Drop the JAR file into your server's `plugins/` (or `mods/`) folder:
```text
/plugins/SSoggySouls-4.4.8.jar
```

**For Linked (Multi-Server):**
Drop the exact same JAR file into the `plugins/` folder of **both** backend servers:
```text
Main Server: /plugins/SSoggySouls-4.4.8.jar
Limbo Server: /plugins/SSoggySouls-4.4.8.jar
```
> **Remember:** Do not put this JAR in your proxy (Velocity/BungeeCord) plugins folder. It will not work.

### Step 3: Generate and edit the config
1. Start your server(s) up once so the default configuration files can generate.
2. Stop the server(s).
3. Open `plugins/SSoggySouls/config.yml` in your favorite text editor.

---

## Proxy & Backend Configuration

### 1. Velocity Proxy Config
Open your proxy's `velocity.toml` file and make sure player info forwarding is set to modern:
```toml
player-info-forwarding-mode = "modern"

[servers]
main = "127.0.0.1:25566"   # Port of your main gameplay server
limbo = "127.0.0.1:25567"  # Port of your limbo server

try = [
  "main"                  # Connect new players to main by default
]
```

### 2. Main Server Configuration
Open `plugins/SSoggySouls/config.yml` on your **Main server**:
```yaml
is-limbo-server: false            # Identifies this as the gameplay server
main-server-name: "main"          # Must match the name used in velocity.toml
limbo-server-name: "limbo"        # Must match the name used in velocity.toml

database:
  type: "mysql"                  # Set to "sqlite" if standalone
  host: "127.0.0.1"              # Your MySQL server IP
  port: 3306
  name: "ssoggysouls"
  username: "ssoggysouls_user"
  password: "your_secure_password"
  pool-size: 5
  table-name: "hardcore_players"

main:
  death-mode: "spectator"         # Options: "spectator", "hybrid", or "limbo"
  hybrid-timeout-seconds: 300     # Time in spectator before exile (for hybrid mode)
  detect-hrm-revive: true         # Set to true to allow in-game ritual structures
```

### 3. Limbo Server Configuration
Open `plugins/SSoggySouls/config.yml` on your **Limbo server**:
```yaml
is-limbo-server: true             # Identifies this as the limbo server
main-server-name: "main"          # Must match the name used in velocity.toml
limbo-server-name: "limbo"        # Must match the name used in velocity.toml

database:
  type: "mysql"                  # MUST be identical to the Main server's DB settings
  host: "127.0.0.1"
  port: 3306
  name: "ssoggysouls"
  username: "ssoggysouls_user"
  password: "your_secure_password"
  pool-size: 5
  table-name: "hardcore_players"

limbo:
  check-interval-seconds: 3       # How often the server queries DB for revivals
```

---

## Setting the Limbo Spawn Point
Once your Limbo server is running, you need to tell the plugin where dead players should spawn:
1. Join your Limbo server.
2. Stand at the exact location, facing the direction you want players to look.
3. Run `/setlimbospawn` in chat.
4. The plugin will save these coordinates directly into the config file.

---

## Testing Your Setup

Once you've configured everything, start up your servers and run through this checklist to make sure things are solid:

1. **Check the database connection:** Look at both server consoles on startup. You should see a message saying "Database connection established successfully".
2. **Verify player status:** Join the Main server and type `/pstatus`. It should show you have 2 lives (by default) and are currently Alive.
3. **Test the death cycle:**
   - Turn off your active grace period if testing with a new account, or use `/psadmin lives <your_name> 0` to set your lives to zero.
   - If you're in **limbo mode**, you should instantly be transferred to the Limbo server.
   - If you're in **hybrid mode**, you will become a spectator on Main. Log out and log back in, or wait for the timer to expire, to verify you get transferred to Limbo.
4. **Test the revival cycle:**
   - From your Main server console (or as an admin on the Main server), run `/psadmin revive <your_name>`.
   - Your Limbo server should notice the database update and safely transfer your character back to the Main server within a few seconds.
   - Run `/pstatus` again to make sure your lives are restored (defaults to 1 life).

---

## Common Mistakes to Avoid

- **Putting the JAR on Velocity:** Always install the plugin on the backend servers (Main & Limbo) and *not* on the proxy itself.
- **Enabling Vanilla Hardcore:** Make sure `hardcore` is set to `false` in `server.properties`. The plugin will not work if vanilla hardcore is enabled.
- **Mismatched Server Names:** The `main-server-name` and `limbo-server-name` inside your `config.yml` must match the server names in your proxy's `velocity.toml` configuration exactly.
- **Using localhost on Shared Hosting:** If you are using a server panel like Pterodactyl, your database host is usually an external IP or subdomain (e.g. `mysql.example.com`) rather than `localhost` or `127.0.0.1`.
- **Mixing Jar Versions:** Both your Main and Limbo servers must run the exact same version of the SSoggySouls JAR file. If they mismatch, you'll see warnings in the console and player routing may fail.

---

[← Quick Start](quick-start) | [Back to Home](index) | [Configuration →](configuration)
