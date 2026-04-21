# SSoggySouls

<img src="https://cdn.modrinth.com/data/Pb03qu6T/images/70ce5f45786d4716bb6d47d242ee3238a2b4ec4a.jpeg" alt="SSoggySouls Banner">

**Version 1.3.6** | Minecraft 1.21.X | Spigot/Paper/Purpur

A hardcore **lives system** plugin for Minecraft 1.21.X servers.
When you lose all your lives, you get exiled to a dedicated **Limbo** server (or placed into spectator mode) until your teammates manage to revive you through dark rituals!

> **[📝 Read the Full Documentation Wiki →](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/)**
> Complete installation guides, configuration reference, and troubleshooting!

---

## ✨ Features

- ❤️ **Configurable Lives System:** Start with 2 lives (customizable, up to a max cap).
- 👻 **Three configurable Death Modes:**
  - `Hybrid`: Dead players spectate their team for a timeout window, then get transferred to Limbo.
  - `Spectator`: Dead players stay on the server as spectators forever.
  - `Limbo`: Instant exile for dead players. No spectating allowed.
- 🛡️ **Grace Period:** Protects newbies with an online-only timer.
- 🔗 **Cross-Server Integration:** Seamlessly banish players to a Limbo server using MySQL and Velocity.
- 💻 **Single-Server Support:** Don't want a Limbo server? Use the built-in drop-in SQLite database instead!
- 🕯️ **Extensive Revival System:** Teammates can rescue you through ritual structures or items.

---

## 🕯️ Hardcore Revive Mechanics

> **Credit:** The revival mechanics (player head drops, ritual structures, and revive items) are adapted from the excellent [Hardcore Revive Mod by JakeCCz](https://modrinth.com/plugin/hardcore-revive-mod).

When a player dies, their **head drops** at the location of their death (fireproof and persistent!). Their teammates can recover the head and use it to bring them back!

- **Ritual Structures:** Build a mystical 3x3x3 beacon (using blocks like Soul Sand, Wither Roses, and Ore) and place the dead player's head on top to trigger an instant revival!
- **Revive Skull:** Craftable item that provides a GUI menu to easily select dead players to receive their head for rituals.
- **Extra Life:** Craftable item that grants an extra life up to the configured max limit.
- **Wield the Head:** Wearing a fallen teammate's head grants Speed II and Night Vision to help you deliver it safely.

---

## 🚀 Quick Setup

Check out our [Quick Start Guide](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/quick-start) on the wiki to get your database configured and Limbo set up in 5 minutes!

### Minimum Requirements
- **Minecraft:** 1.21.X (Spigot, Paper, or Purpur)
- **Java:** 21+
- **Database:** SQLite (built-in, for single servers) OR MySQL 5.7+ / MariaDB 10.2+ (for 2-server setups)

### Optional multi-server requirements:
- **Proxy:** Velocity
- **Servers:** Two backend servers (Main + Limbo)

> **Important:** Do NOT enable `hardcore=true` in `server.properties`. Keep it `false` - the plugin natively handles these interactions.

---

## 💬 Commands & Permissions

SSoggySouls packs standard commands for your users (`/pstatus` to check their lives, `/limbo` to visit exiled teammates) and a suite of admin tools (`/psadmin lives`, `/psadmin revive`, `/psadmin grace`) to help you manage the playing field.

For a full list, check out our [Commands Reference](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/commands) on the wiki.

---

**Enjoying SSoggySouls?** Heart it on Modrinth and star our [GitHub repo](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore)!
