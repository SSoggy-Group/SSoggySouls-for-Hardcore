# SSoggySouls

<img src="https://cdn.modrinth.com/data/Pb03qu6T/images/70ce5f45786d4716bb6d47d242ee3238a2b4ec4a.jpeg" alt="SSoggySouls Banner">
**Version 4.5.1** | Minecraft 1.21.X | Spigot/Paper/Purpur/Fabric/Forge/NeoForge

A hardcore lives system plugin. When you die enough times, you get exiled to a Limbo server (multi-server) or enter spectator mode (single-server) until your teammates revive you.
> **Note:** Fabric, Forge, and NeoForge versions are currently in an early testing phase. Expect frequent updates and please report any bugs you find!

> **[Read the Full Documentation Wiki →](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/)** - Complete installation guides, configuration reference, troubleshooting, and more!

---

## Features

- **Lives System** - Start with 2 lives (configurable), max 5.
- **Death Handling** - SQLite keeps dead players as spectators; MySQL/proxy setups can use instant Limbo or hybrid timeout.
- **Multiple Revival Methods** - Ritual structures, Revive Skull, or admin commands.
- **Grace Period** - Newbies get protected time (only counts online time).
- **Extra Life Items** - Craftable items for more lives (fully customizable).
- **Flexible Database** - Use built-in SQLite for a single server, or MySQL to sync between Main and Limbo.
- **Limbo Visiting** - Living players can visit dead teammates.

---

## Built-in Revival System

> **Credit:** The revival mechanics—including player head drops, ritual structures, and the Revive Skull are forked from the [Hardcore Revive Mod by JakeCCz](https://modrinth.com/plugin/hardcore-revive-mod).

**Player Head Drops:** When you lose all your lives, your head drops where you died and your coordinates are posted in chat.

**Revival Ritual:**
Build a 3x3x3 beacon-like structure:

- **Bottom:** 4 Soul Sand corners, 4 Stairs at edges, 1 Ore block in the center.
- **Middle:** 4 Wither Roses on the Soul Sand, 1 Fence on the ore.
- **Top:** Place the dead player's head on the fence to trigger a revival.

**Craftable Items:**

- **Revive Skull:** Right-click to open a GUI menu of dead players and receive their head for rituals.
- **Extra Life:** Right-click to gain +1 life (max cap applies). The recipe is fully customizable.

**Head Effects:**
Wearing a dead player's head grants Speed II and Night Vision to help you deliver it to a ritual safely.

---

## Requirements

- **Minecraft:** 1.21.X (Spigot, Paper, Purpur, Fabric, Forge, or NeoForge)
- **Java:** 21+
- **Database:** SQLite (built-in, zero setup) OR MySQL/MariaDB (required for multi-server)
- *(Optional)* **Proxy:** Velocity with two backend servers

> **Important:** Do NOT enable `hardcore=true` in `server.properties`. Keep it `false` - the plugin natively handles these mechanics.

---

## Quick Start Summary

1. **Download** the plugin and place it in your `plugins/` folder.
2. **Start your server** to generate the `config.yml`.
3. **Select your database:**
   - *Single server:* Set `type: "sqlite"`. Done — ignore the MySQL fields.
   - *Two servers:* Set up one MySQL database, then set `type: "mysql"` and copy the same connection details into both server configs.
4. **If using two servers**, set `is-limbo-server: false` on Main and `true` on Limbo.
5. **Restart and test!**

For the full, detailed step-by-step setup (including proxy configuration and Limbo spawn setting), refer to our **[Quick Start Guide on the Wiki!](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/quick-start) This is also where you can find other detailed guides about installing, configurating, troubleshooting, and more.**

---

## Complete Documentation

To save space and avoid cluttering this page, the full configuration files, installation steps, and command lists have been moved to our comprehensive wiki.

Please visit the **[SSoggySouls Wiki](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/)** for:

- [Quick Start Guide](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/quick-start)
- [Configuration Reference](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/configuration)
- [Commands & Permissions](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/commands)
- [Troubleshooting & FAQ](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/faq)

---

## Credits

- Revival mechanics heavily inspired by and adapted from the [Hardcore Revive Mod](https://modrinth.com/plugin/hardcore-revive-mod) by JakeCCz.
- Author: SSoggyTacoMan
- GitHub: [SSoggy-Group/SSoggySouls-for-Hardcore](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore)
