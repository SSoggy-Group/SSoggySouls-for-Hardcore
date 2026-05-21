---
layout: default
title: Frequently Asked Questions
---

## Frequently Asked Questions (FAQ)

Quick answers to common questions about SSoggySouls installation, configuration, and gameplay.

## Installation & Setup

### Q: Can I install SSoggySouls on my Velocity proxy?

**A:** No! SSoggySouls is **NOT a proxy plugin**. Install it **only on your backend servers** (Main and Limbo). Installing on Velocity/BungeeCord/Waterfall will NOT work.

- - Install on Main server

- - Install on Limbo server

- - Do NOT install on Velocity proxy

______________________________________________________________________

### Q: What servers do I need?

**A:** You need:

1. **Main Server** - Your survival/gameplay server (1.21.X Spigot, Paper, Purpur, Fabric, Forge, or NeoForge)

1. **Limbo Server** - Purgatory server for dead players (same version)

1. **Velocity Proxy** - Routes players between servers

1. **Database** - Stores player data (MySQL for 2-server networks, or built-in SQLite for single-server)

**Exception:** If using `spectator` death mode, the Limbo server is optional.

______________________________________________________________________

### Q: Can I use BungeeCord instead of Velocity?

**A:** Maybe. BungeeCord/Waterfall support is **untested**. If you test it, please [report your findings](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues)!

Currently supported: **Velocity (recommended)**

______________________________________________________________________

### Q: Do I need to enable Minecraft's hardcore mode?

**A:** No! **Keep `hardcore=false`** in `server.properties`. The plugin manages hardcore mechanics internally.

**DO NOT set `hardcore=true`** - this will break the plugin!

______________________________________________________________________

### Q: Which version of Minecraft is supported?

**A:** SSoggySouls requires **Minecraft 1.21.X** with:

- Spigot, Paper, Purpur, Fabric, Forge, or NeoForge server software

- Java 21 or higher

______________________________________________________________________

### Q: Can I share my database with other plugins?

**A:** Yes, if using MySQL! The database can be shared with other plugins (CoreProtect, etc.). SSoggySouls creates its own table and won't interfere. (The SQLite option is local and dedicated only to SSoggySouls).

______________________________________________________________________

## Gameplay & Death Modes

### Q: What happens when I die?

**A:** It depends on your death mode setting:

**Hybrid Mode (default):**

1. You lose 1 life
1. Enter spectator mode on Main server
1. Your team has 5 minutes to revive you
1. If not revived, you're transferred to Limbo
1. If you disconnect while dead, you go straight to Limbo

**Spectator Mode:**

1. You lose 1 life
1. Enter spectator mode on Main
1. Stay there indefinitely until revived
1. Never auto-transfer to Limbo

**Limbo Mode:**

1. You lose 1 life
1. If you reach 0 lives, immediately transfer to Limbo
1. No spectator window

______________________________________________________________________

### Q: Can I come back from 0 lives?

**A:** Yes! If you reach 0 lives, you're exiled to Limbo (or become a spectator depending on death mode). Your teammates can revive you by:

- Building a ritual structure with your head

- Using the Revive Skull item menu

- Running `/revive <your_name>` command

- Using `/psadmin revive <your_name>` (admin)

Once revived, you're restored to 1 life (or configured amount) and return to Main server.

______________________________________________________________________

### Q: How long is the grace period?

**A:** The grace period is **configurable** (default: 24 hours).

**Important:** The timer counts **only when you're online** (pauses when you log off).

Example: New player with "24h" grace who plays 3 hours, logs off, then returns will have 21 hours remaining.

**Format Options:**

- `"24h"` = 24 hours

- `"2h30m"` = 2 hours 30 minutes

- `"90m"` = 90 minutes

- `"0"` = Disabled (no grace)

______________________________________________________________________

### Q: Do deaths during grace period cost lives?

**A:** No! During the grace period:

- You can die as much as you want

- You don't lose lives

- Your grace timer counts down (only while online)

- Once grace expires, normal rules apply

______________________________________________________________________

### Q: Can I visit my dead teammates in Limbo?

**A:** Yes! If you're alive, you can visit Limbo:

```bash
/limbo

# You teleport to Limbo server

/leavelimbo

# or /hub

# You teleport back to Main

```

**Restrictions:**

- - Living players can visit anytime

- - Living players can leave anytime

- - Dead players cannot use `/leavelimbo`

- - Dead players cannot visit Main until revived

______________________________________________________________________

### Q: What's the revive cooldown?

**A:** After being revived, you're protected for **30 seconds** (configurable):

- You cannot lose lives during this time

- Gives you time to get oriented after revival

- Prevents accidental deaths right after revival

______________________________________________________________________

### Q: Can I get my head back to use for revival?

**A:** Yes! Two ways:

1. **Original head** - Drops at your death location, teammates can find it

1. **Revive Skull** - Teammates can use this item's menu to get your head anytime

If your original head is lost, the Revive Skull menu will still give you another copy.

______________________________________________________________________

## Revival & Revival Structures

### Q: How do I revive someone?

**A:** Four methods (pick any):

1. **Ritual Structure** - Build 3x3x3 beacon with player head

1. **Revive Skull** - Use GUI menu to get head

1. **Command** - `/revive <player>` (requires permission)

1. **Admin Command** - `/psadmin revive <player>` (admin only)

______________________________________________________________________

### Q: What's the ritual structure recipe?

**A:** It's a 3x3x3 structure with three layers:

**Layer 1 (Base):**

```text

Soul Sand | Stair | Soul Sand
Stair    | Ore   | Stair
Soul Sand | Stair | Soul Sand

```

**Layer 2 (Middle):**

```text

Wither Rose | (air) | Wither Rose
(air)       | Fence | (air)
Wither Rose | (air) | Wither Rose

```

**Layer 3 (Top):**

```text

(air)  | (air) | (air)
(air)  | Head  | (air)
(air)  | (air) | (air)

```

Once the head is placed, the plugin auto-detects and revives the player!

______________________________________________________________________

### Q: What ore block should I use?

**A:** Any ore block works! Choose for:

- **Difficulty** - Diamond = harder, Iron = easier

- **Aesthetics** - Pick your favorite color

- **Availability** - Use what you have most of

Common choices:

- Gold Ore = gold color, easy to find

- Diamond Ore = blue, harder to find

- Emerald Ore = green, matches nature theme

- Any other ore works fine too!

______________________________________________________________________

### Q: How do I get the Revive Skull?

**A:** Craft it with this recipe (shapeless):

|                  |                |                  |
| ---------------- | -------------- | ---------------- |

| Obsidian         | Ghast Tear     | Obsidian         |
| Totem of Undying | Any Skull/Head | Totem of Undying |
| Obsidian         | Ghast Tear     | Obsidian         |

**Materials needed:**

- 4× Obsidian (craft with water + lava, or find in Nether)

- 2× Ghast Tear (kill Ghasts in Nether)

- 2× Totem of Undying (kill Evokers in mansions)

- 1× Any skull or head

______________________________________________________________________

### Q: Can I customize the Extra Life recipe?

**A:** Yes! Edit the config:

```yaml
extra-life:
  recipe:
    row1: "DED"
    row2: "INI"
    row3: "GEG"
    ingredients:
      D: "DIAMOND_BLOCK"
      E: "EMERALD_BLOCK"
      N: "NETHER_STAR"
      G: "GOLD_BLOCK"
      I: "NETHERITE_INGOT"

```

Change letters and materials to create your own recipe!

______________________________________________________________________

### Q: What happens when I wear a dead player's head?

**A:** You receive:

- ⚡ **Speed II** effect (faster movement)

- 👁️ **Night Vision** (see in darkness)

This gives tactical advantage while carrying the head to a revival structure. The effects disappear when you remove the head.

______________________________________________________________________

## Commands & Permissions

### Q: What's the difference between `/revive` and `/psadmin revive`?

**A:** Both work the same way, but:

- **`/revive`** - Requires `ssoggysouls.revive` permission (default: op)

- **`/psadmin revive`** - Requires `ssoggysouls.admin` permission (admin only)

Use `/revive` for moderators, `/psadmin revive` for admins only.

______________________________________________________________________

### Q: Can I use commands from console?

**A:** Yes! Run from server console:

```bash

# From server console

psadmin revive PlayerName
psadmin lives PlayerName 5
psadmin info PlayerName
psadmin grace PlayerName 48

```

(Drop the `/` when running from console)

______________________________________________________________________

### Q: What permissions do I need?

**A:**

| Permission           | Description            | Default |
| -------------------- | ---------------------- | ------- |

| `ssoggysouls.admin`  | Full admin access      | op      |
| `ssoggysouls.revive` | Revive players         | op      |
| `ssoggysouls.status` | Check status           | true    |
| `ssoggysouls.visit`  | Visit Limbo            | true    |
| `ssoggysouls.bypass` | Bypass death mechanics | false   |

______________________________________________________________________

### Q: Can I give mod permissions without op?

**A:** Yes! Use a permission plugin (LuckPerms, PermissionsEx, etc.):

```bash

# Give revive permission to a moderator

/lp user ModName permission set ssoggysouls.revive true

# Give admin permission to a trusted admin

/lp user AdminName permission set ssoggysouls.admin true

```

______________________________________________________________________

## Configuration & Customization

### Q: How do I customize messages?

**A:** Edit the messages section in `config.yml`:

```yaml
messages:
  prefix: "&8[&4☠&8] &r"
  death-life-lost: "&cYou lost a life! &7Remaining: &e%lives%"
  death-last-life: "&c&l⚠ FINAL WARNING!"
  # ... many more

```

**Color codes:**

- `&c` = Red, `&a` = Green, `&e` = Yellow

- `&l` = Bold, `&m` = Strikethrough

- `%lives%`, `%player%`, `%max%` = variables

______________________________________________________________________

### Q: Can I disable the grace period?

**A:** Yes! Set to `"0"`:

```yaml
lives:
  grace-period: "0"

```

New players will lose lives immediately on death.

______________________________________________________________________

### Q: Can I change how many lives are restored on revival?

**A:** Yes!

```yaml
lives:
  on-revive: 1  # Change to any number

```

- `0` = Revived with no lives (must use Extra Life item)

- `1` = Revived with 1 life (standard)

- `2+` = More forgiving

- Can't exceed `max-lives`

______________________________________________________________________

### Q: How do I set Limbo spawn?

**A:**

1. Go to Limbo server in-game
1. Stand at desired spawn location
1. Run `/setlimbospawn`
1. Coordinates saved to config

(Only needs to be done once)

______________________________________________________________________

### Q: Can I reload config without restarting?

**A:** Yes!

```bash
/psadmin reload

```

Most settings reload immediately. Some may require player rejoin or server restart.

______________________________________________________________________

## Troubleshooting

### Q: Players aren't being sent to Limbo

**A:** Check:

- [ ] `is-limbo-server` is correct (false on Main, true on Limbo)

- [ ] Both servers have identical database credentials

- [ ] Server names match proxy config (`main-server-name`, `limbo-server-name`)

- [ ] Velocity proxy has modern forwarding enabled

- [ ] Console shows no database errors

______________________________________________________________________

### Q: Ritual structures aren't working

**A:** Check:

- [ ] Structure is exactly 3x3x3

- [ ] Head is placed on the fence (top layer center)

- [ ] Using correct blocks (Soul Sand, Wither Roses, Ore, etc.)

- [ ] `hrm.structure-revive: true` in config

- [ ] `detect-hrm-revive: true` in config

Try `/revive <player>` command instead to test if database works.

______________________________________________________________________

### Q: Database connection errors

**A:** Check:

- [ ] MySQL/MariaDB is running

- [ ] Database credentials are correct

- [ ] Same credentials on both servers

- [ ] Firewall allows connection

- [ ] Database exists

- [ ] For Pterodactyl: Use panel host, not "localhost"

______________________________________________________________________

### Q: Players losing lives during grace period

**A:** Check:

- [ ] Grace period is configured: `grace-period: "24h"` (not "0")

- [ ] Grace period counts only online time (check with `/pstatus`)

- [ ] If player joined before grace was enabled, manually set:

  ```bash
  /psadmin grace PlayerName 48

```text

______________________________________________________________________

### Q: Both servers running different versions

**A:** **Critical issue!** Both servers MUST run the same SSoggySouls version:

- Download same `.jar` for both

- Update both servers simultaneously

- Check console for version mismatch warnings

______________________________________________________________________

### Q: Hardcore hearts not showing

**A:** Check:

- [ ] `hardcore-hearts: true` in config

- [ ] Requires client mod or resource pack

- [ ] Cosmetic only - doesn't affect gameplay

- [ ] Works regardless of setting

______________________________________________________________________

### Q: Getting help with other issues

**A:** If stuck:

1. Enable debug mode: `debug: true` in config
1. Restart servers and reproduce issue
1. Check console for errors
1. Gather info:
   - SSoggySouls version

   - Minecraft version

   - Proxy type

   - Relevant config sections

   - Console errors/logs

1. [Open an issue](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues) with details

______________________________________________________________________

## General Questions

### Q: Is this only for hardcore servers?

**A:** No! SSoggySouls works for any server style:

- **Hardcore** - One life mode

- **Casual** - Multiple lives with forgiving grace periods

- **Competitive** - Tight 2-3 life setup

- **SMP** - Standard survival multiplayer

Customize to your needs!

______________________________________________________________________

### Q: Can I use this with other death plugins?

**A:** Probably not - conflicts likely. SSoggySouls manages all death mechanics internally.

______________________________________________________________________

### Q: Does this work on 1.20 or earlier versions?

**A:** No. SSoggySouls requires **Minecraft 1.21.X**.

______________________________________________________________________

### Q: Can I customize the Limbo world?

**A:** Yes! The Limbo server is a normal Minecraft server. You can:

- Build structures and terrain

- Set themes and decorations

- Place signs with rules

- Add anything else you want

Just remember to set spawn with `/setlimbospawn` after customizing.

______________________________________________________________________

### Q: Are there any performance concerns?

**A:** SSoggySouls is optimized for performance:

- Efficient database queries

- Configurable check intervals

- Connection pooling

- No significant server impact

**Recommendations:**

- Use an SSD for database for best performance

- Set pool-size based on server size

- Monitor database load if you have 100+ players

______________________________________________________________________

### Q: Can I disable specific HRM features?

**A:** Yes! Toggle each individually:

```

hrm:
  enabled: true              # Master toggle

  drop-heads: false          # Don't drop heads

  structure-revive: false    # Disable ritual structures

  revive-skull-recipe: false # Disable Revive Skull crafting

  head-wearing-effects: false # Disable Speed/Night Vision

```text

Or set `enabled: false` to disable everything.

______________________________________________________________________

## Need More Help?

- [Installation Guide](installation)

- 📋 [Commands Reference](commands)

- [Configuration Reference](configuration)

- 🏗️ [Revival System Guide](revival-system)

- [Report Issues](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues)

______________________________________________________________________

[← Revival System Guide](revival-system) | [Back to Home](index)
