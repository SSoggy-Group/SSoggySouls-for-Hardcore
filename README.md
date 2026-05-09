# SSoggySouls

![SSoggySouls Banner](https://cdn.modrinth.com/data/Pb03qu6T/images/48a03bf24103dde408dbbcad653a3936b5f5255a.png)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=SSoggy-Group_SSoggySouls-for-Hardcore&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=SSoggy-Group_SSoggySouls-for-Hardcore) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SSoggy-Group_SSoggySouls-for-Hardcore&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SSoggy-Group_SSoggySouls-for-Hardcore) [![AI Code Assurance](https://sonarcloud.io/api/project_badges/ai_code_assurance?project=SSoggy-Group_SSoggySouls-for-Hardcore)](https://sonarcloud.io/summary/new_code?id=SSoggy-Group_SSoggySouls-for-Hardcore) [![Auto Bump, Build, and Release](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/actions/workflows/auto-release.yml/badge.svg)](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/actions/workflows/auto-release.yml) [![Continuous Integration](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/actions/workflows/ci.yml/badge.svg)](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/actions/workflows/ci.yml)

**Version 4.4.8** | [Modrinth Project Page](https://modrinth.com/project/Pb03qu6T) | [GitHub Repository](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore)

SSoggySouls brings a highly customizable hardcore lives system to Minecraft 1.21.X (supporting Spigot, Paper, Purpur, Fabric, and Forge). If you run out of lives, you are either stuck spectating (on single servers) or exiled to a dedicated Limbo server (on proxy networks) until your teammates manage to revive you.

> **Note:** Fabric and Forge versions are currently in an early testing phase. Expect frequent updates, and please report any bugs you find on our issue tracker!
>
> **[Full Documentation Wiki →](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/)** - Complete installation guides, configuration details, command listings, and FAQ.

---

## Features

- **Lives System:** Customize starting lives (defaults to 2) and maximum caps (defaults to 5) that are lost upon dying.
- **Death Handling:** Standalone SQLite keeps dead players in spectator mode; MySQL/proxy networks can use instant Limbo transfer or a hybrid spectator timeout.
- **Multiple Revival Methods:** Bring players back using in-game ritual structures (beacon setups), craftable Revive Skulls, or commands.
- **New Player Grace Period:** Protect new players for their first few hours of online play (timer pauses when they're offline).
- **Custom Extra Lives:** Let players craft custom items to gain extra lives, with fully customizable recipes.
- **Flexible Database Support:** Works out of the box with SQLite for single servers, or MySQL/MariaDB to sync across multiple backend servers.
- **Limbo Visiting:** Living players can use `/limbo` to visit and hang out with dead teammates.

---

## How It Works

SSoggySouls supports two main setups depending on your server's style:

1. **Standalone (SQLite):** Everything runs on one server. Dead players are put into spectator mode. No proxy, no complex databases, and no extra servers are needed. Just drop the plugin in and play.
2. **Proxy Network (MySQL):** Runs on two backend servers behind a Velocity proxy: a **Main** server (where active play happens) and a **Limbo** server (where dead players are exiled). Both servers connect to the same MySQL database to keep player records in sync.

### Basic Gameplay Loop:
- Players start with 2 lives (configurable).
- Each death costs 1 life.
- New players are protected by a grace period (defaults to 24 online hours).
- At 0 lives, players are either forced to spectate or exiled to Limbo.
- Teammates can bring players back by:
  - Building a ritual altar and placing the dead player's head on top.
  - Using a Revive Skull to open a GUI menu and retrieve their head.
  - Running `/revive <player>` (requires permission).
- Once revived, players are safe-transferred back to the Main server with 1 life (configurable) and given a brief window of post-revival invincibility.

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

## Death Modes

You can configure what happens when a player loses their last life:

| Mode | What Happens | Best For |
|---|---|---|
| **`spectator`** | Dead players stay on the Main server as spectators indefinitely. They are never sent to Limbo. | SQLite standalone setups, or casual servers where you want dead players to stay and watch. |
| **`limbo`** | As soon as a player runs out of lives, they are instantly exiled to your Limbo server. | Hardcore servers that want strict separation between the living and the dead. |
| **`hybrid`** | Dead players stay as spectators on the Main server for 5 minutes (configurable). If they aren't revived before the timer runs out (or if they log out), they are exiled to the Limbo server. | Dual-server MySQL setups that want to add urgency and a rescue-mission vibe. |

---

## Built-in Hardcore Revive Mode (HRM)

The core revival mechanics are adapted from JakeCCz's excellent [Hardcore Revive Mod](https://modrinth.com/plugin/hardcore-revive-mod). Advanced features like ghost tracking, customizable block tags, and robust database cleanups were ported from the **RevivalPlus DLC by Cera and JakeCCz**.

### Player Head Drops
When a player loses their last life, their coordinates are sent to them in chat, and their player head drops at their death location.

You can configure how these heads drop using `hrm.head-place-as-block`:
- **Block Mode (true, default):** The head is placed as a permanent block. The plugin scans *upward* from their death point to find the first open air pocket on solid ground (so if they die in a lava pool, their head safely emerges on the surface). It cannot burn or despawn. Teammates simply break the block to pick up the skull.
- **Item Entity Mode (false):** The head drops as a standard item on the ground. We recommend turning on `hrm.head-no-despawn` (stops item despawning) and `hrm.head-fireproof` (makes the head invulnerable to fire and lava) if you choose this.

*Automatic Cleanup:* To prevent duplicate heads or chest clutter, the plugin will automatically remove all copies of a player's head from worlds, inventories, and chests once they are revived.

### Revival Altar (Ritual Structure)
To revive a player, build this 3x3x3 structure:
- **Base:** Place an ore block (like gold or diamond blocks) in the center, Soul Sand at the four corners, and stairs of your choice on the remaining sides.
- **Middle:** Place a fence block on the central ore, and Wither Roses on the four Soul Sand corners.
- **Top:** Put the dead player's head on the fence.

The plugin detects the completed structure and revives them instantly! You can choose whether to keep the base intact or destroy it after revival in `config.yml`.

### Craftable Items

#### 1. Revive Skull
Combine these items anywhere in your crafting grid to get a Revive Skull:
- 4× Obsidian
- 2× Ghast Tears
- 2× Totem of Undying
- 1× Any Player or Mob Skull/Head

Right-click the Revive Skull to open a GUI menu of dead players. Selecting a player will drop their custom skull into your inventory so you can use it in a ritual.

#### 2. Extra Life
Craft this item and right-click to eat it and gain +1 life (up to your maximum cap). By default, this item displays as a Nether Star.

The default recipe is:
- **Top Row:** Diamond Block, Emerald Block, Diamond Block
- **Middle Row:** Netherite Ingot, Nether Star, Netherite Ingot
- **Bottom Row:** Gold Block, Emerald Block, Gold Block

You can fully customize this recipe in `config.yml`:
```yaml
extra-life:
  enabled: true
  item-material: "NETHER_STAR"
  recipe:
    row1: "DED"
    row2: "INI"
    row3: "GEG"
    ingredients:
      G: "GOLD_BLOCK"
      E: "EMERALD_BLOCK"
      N: "NETHER_STAR"
      D: "DIAMOND_BLOCK"
      I: "NETHERITE_INGOT"
```

### Head Effects
If a living player wears a dead teammate's head block in their head slot, they receive **Speed II** and **Night Vision** effects to help them carry it safely back to a ritual structure.

---

## Requirements

- **Minecraft Version:** 1.21.X (Spigot, Paper, Purpur, Fabric, or Forge)
- **Java Version:** Java 21 or newer
- **Database:** SQLite (built-in, zero setup) for standalone, OR MySQL 5.7+ / MariaDB 10.2+ for proxy networks
- **Proxy Software (Optional):** Velocity (needed only if you're linking two servers together)

> **Important:** Keep `hardcore=false` in `server.properties` on all servers! SSoggySouls manages the hardcore state internally. Enabling vanilla hardcore mode will break the plugin.

---

## Installation & Setup

### Step 1: Download the plugin
Grab the latest release file (`SSoggySouls-4.4.8.jar`) from our [GitHub Releases](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/releases) or [Modrinth](https://modrinth.com/project/Pb03qu6T).

### Step 2: Install the files
Stop your Minecraft server(s).
- **For single standalone servers:** Drop the JAR file into your `plugins/` (or `mods/`) folder.
- **For proxy networks:** Drop the same JAR file into the `plugins/` folders of **both** your Main and Limbo servers.

### Step 3: Configure your database in `config.yml`
Start the servers up once to generate `plugins/SSoggySouls/config.yml`, then stop them.
- **For standalone servers:** Make sure `database.type` is set to `"sqlite"`. No other database configurations are needed!
- **For proxy networks:** Set `database.type` to `"mysql"`, fill in your database details, and copy those exact connection settings into both server configs.

### Step 4: Configure server roles (Only for proxy networks)
- **On your Main gameplay server's config:** Set `is-limbo-server: false`, and set the server names to match your proxy's `velocity.toml` server names exactly.
- **On your Limbo server's config:** Set `is-limbo-server: true`, and copy the exact same server name settings.

### Step 5: Set your Limbo spawn
1. Start up your Limbo server.
2. Join the Limbo server in-game.
3. Stand exactly where you want dead players to spawn.
4. Run `/setlimbospawn`.

---

## Detailed Configuration Reference

SSoggySouls uses a highly detailed `config.yml` configuration file. Here is a complete copy of the default parameters with explanatory comments:

```yaml
# ==========================================
# SSoggySouls Configuration
# ==========================================

# Identifies whether this server is the Limbo (purgatory) server.
# Set to false on Main server, true on Limbo server.
is-limbo-server: false

# Proxy server identification names. Must match velocity.toml names exactly.
main-server-name: "main"
limbo-server-name: "limbo"

# Security check for operators on the Limbo server.
# Block Limbo-only OPs from executing admin/revival commands.
limbo-op-security-check: true

# Whitelisted admin names/UUIDs who can run admin commands on Limbo regardless of OP check.
limbo-trusted-admins:
  - "069a79f4-44e9-4726-a5be-fca90e38aaf5"

# ------------------------------------------
# Database Settings
# ------------------------------------------
database:
  # "sqlite" for standalone, "mysql" for proxy networks.
  type: "sqlite"
  
  # MySQL connection details (only used when type: "mysql")
  host: "localhost"
  port: 3306
  name: "ssoggysouls"
  username: "root"
  password: "changeme"
  pool-size: 5
  table-name: "hardcore_players"

# ------------------------------------------
# Lives and Grace Period
# ------------------------------------------
lives:
  # Lives new players start with on their first login.
  default: 2
  
  # Absolute maximum lives cap a player can have.
  max-lives: 5
  
  # Lives restored when a player is revived.
  on-revive: 1
  
  # New player protection timer (e.g. "24h", "2h30m", or "0" to disable).
  # Timer only counts down when the player is online.
  grace-period: "24h"
  
  # Invincibility period in seconds immediately after being revived.
  revive-cooldown-seconds: 30

# ------------------------------------------
# Death Handling and Spectator Modes
# ------------------------------------------
main:
  # Death handling mode on the Main server.
  # Options: "spectator", "limbo", or "hybrid"
  death-mode: "spectator"
  
  # Spectate timer in seconds before transfer to Limbo (only for hybrid mode).
  hybrid-timeout-seconds: 300
  
  # Put players in spectator mode immediately on death before sending to Limbo.
  spectator-on-death: false
  
  # Automatically detect completed ritual structures in-game.
  detect-hrm-revive: true
  
  # Tick delay before transferring a dead player to Limbo (20 ticks = 1 second).
  send-to-limbo-delay-ticks: 20

# ------------------------------------------
# Limbo Server Checks
# ------------------------------------------
limbo:
  # Database query check frequency in seconds on Limbo (defaults to 3).
  check-interval-seconds: 3
  
  # Where dead players spawn on Limbo (set in-game using /setlimbospawn)
  spawn:
    world: "world"
    x: 0.5
    y: 65.0
    z: 0.5
    yaw: 0.0
    pitch: 0.0

# ------------------------------------------
# Hardcore Revive Mode (HRM)
# ------------------------------------------
hrm:
  # Master toggle for all revival features.
  enabled: true
  
  # Drop the player's skull block on death.
  drop-heads: true
  
  # Send coordinates of death to the player in chat.
  death-location-message: true
  
  # Enable physical 3x3x3 beacon ritual structures.
  structure-revive: true
  
  # Keep base of ritual structure intact after a successful revival.
  leave-structure-base: true
  
  # Grant Speed II and Night Vision when wearing a player head block.
  head-wearing-effects: true
  
  # Enable crafting of Revive Skull items.
  revive-skull-recipe: true

# ------------------------------------------
# Extra Life Crafting Item
# ------------------------------------------
extra-life:
  # Master toggle for Extra Life crafting.
  enabled: true
  
  # Material icon in chest menus.
  item-material: "NETHER_STAR"
  
  # Crafting grid recipe layout.
  recipe:
    row1: "DED"
    row2: "INI"
    row3: "GEG"
    ingredients:
      G: "GOLD_BLOCK"
      E: "EMERALD_BLOCK"
      N: "NETHER_STAR"
      D: "DIAMOND_BLOCK"
      I: "NETHERITE_INGOT"

# ------------------------------------------
# Custom Messages and Placeholders
# ------------------------------------------
messages:
  prefix: "&8[&4☠&8] &r"
  death-life-lost: "&cYou lost a life! &7Remaining: &e%lives%"
  death-last-life: "&c&l⚠ FINAL WARNING! &cYou are on your last life. Be careful!"
  revive-success: "&a&l✦ REVIVED! &aReturning to the world of the living..."

# ------------------------------------------
# Advanced Options
# ------------------------------------------
check-for-updates: true
hardcore-hearts: true
debug: false
```

---

## Commands & Permissions

### Player Commands

| Command | Description | Permission Node | Default Group |
|---|---|---|---|
| `/pstatus [player]` | View current lives, status, and remaining grace period | `ssoggysouls.status` | Anyone (`true`) |
| `/revive <player>` | Revives a dead player | `ssoggysouls.revive` | Operator (`op`) |
| `/limbo` | Visit the Limbo server as a living player | `ssoggysouls.visit` | Anyone (`true`) |
| `/leavelimbo` | Return from Limbo to the Main server (visitors only) | `ssoggysouls.visit` | Anyone (`true`) |

### Admin Commands

| Command | Description | Permission Node | Default Group |
|---|---|---|---|
| `/psadmin lives <player> <amount>` | Set a player's life count | `ssoggysouls.admin` | Operator (`op`) |
| `/psadmin revive <player>` | Revives a dead player (same as `/revive`) | `ssoggysouls.admin` | Operator (`op`) |
| `/psadmin kill <player>` | Force-kills a player (exiles them) | `ssoggysouls.admin` | Operator (`op`) |
| `/psadmin grace <player> <hours>` | Set a custom grace period for a player | `ssoggysouls.admin` | Operator (`op`) |
| `/psadmin reset <player>` | Reset player's data back to defaults | `ssoggysouls.admin` | Operator (`op`) |
| `/psadmin info <player>` | Look up detailed backend player records | `ssoggysouls.admin` | Operator (`op`) |
| `/psadmin reload` | Reload the configuration file from disk | `ssoggysouls.admin` | Operator (`op`) |
| `/setlimbospawn` | Sets the Limbo spawn point to your location | `ssoggysouls.admin` | Operator (`op`) |
| `/adminlog [lines]` | View recent admin action history logs in-game | `ssoggysouls.adminlog` | Operator (`op`) |

*(Note: You can shorten `/psadmin` to `/psa` in-game.)*

---

## Security Features: Limbo Operator Check

By default, Operators (OPs) on the Limbo server are blocked from running admin commands like `/revive` or `/psadmin` to prevent security loops.

If you have trusted admins who need access to commands on Limbo, you can bypass this check in a few ways:
- **Add them to the config whitelist:** Add their UUIDs or usernames directly to `limbo-trusted-admins` in your Limbo server's `config.yml`.
- **Grant permissions via LuckPerms:** Assign the `ssoggysouls.bypass-limbo-op-security` permission to their account or group on the Limbo server.
- **Run the commands from the server console:** Console commands are always trusted on both servers.

---

## Troubleshooting Quick Tips

- **Players not transferring?** Make sure `is-limbo-server` is configured correctly, verify that server names match your `velocity.toml` server names exactly (case-sensitive), and check that both servers connect to the same MySQL database.
- **Mismatched client hearts?** Turn on `hardcore-hearts: true` in your config. Note that this is a visual cosmetic feature and won't affect the backend mechanics.
- **Need help or debug logs?** Set `debug: true` in your config, restart the servers, and check your console logs. If you're still stuck, feel free to open a ticket on our [GitHub Issue Tracker](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues).

---

## Credits & Contributing

- **Author:** SSoggyTacoMan
- **License:** GPL-3.0
- **Altar & Revival Mechanics:** Based on [JakeCCz's Hardcore Revive Mod](https://modrinth.com/plugin/hardcore-revive-mod).
- Special thanks to GitHub Copilot and all community contributors for various bug fixes!

*(If you are wondering why almost all commits are from a single day, I had to rewrite the repository history because it was a total mess.)*
