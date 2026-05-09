# SSoggySouls

<img src="https://cdn.modrinth.com/data/Pb03qu6T/images/70ce5f45786d4716bb6d47d242ee3238a2b4ec4a.jpeg" alt="SSoggySouls Banner">
**Version 4.4.8** | Minecraft 1.21.X | Spigot/Paper/Purpur/Fabric/Forge

SSoggySouls is a hardcore lives plugin and mod. If you die enough times, you get exiled to a dedicated Limbo server (for multi-server networks) or placed into spectator mode (for single servers) until your teammates manage to revive you.

> **Note:** Fabric and Forge versions are still in early testing. Expect frequent updates, and please report any bugs you find on GitHub!

> **[Read the Full Documentation Wiki →](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/)** - Complete installation guides, config references, commands, and more.

---

## Features

- **Lives System:** Start with a customizable number of lives (defaults to 2, capped at 5).
- **Death Handling:** Standalone SQLite keeps dead players in spectator mode; MySQL/proxy networks can use instant Limbo transfer or a hybrid spectator timeout.
- **Multiple Revival Methods:** Bring players back using in-game ritual structures, craftable Revive Skulls, or commands.
- **New Player Grace Period:** Protect new players from losing lives for their first few hours of online play.
- **Extra Life Items:** Craft items that grant extra lives, with fully customizable recipes.
- **Flexible Database Support:** Works out of the box with SQLite for single servers, or MySQL/MariaDB to sync across multiple backend servers.
- **Limbo Visiting:** Living players can use `/limbo` to visit and hang out with dead teammates.

---

## Built-in Revival System

> **Credits:** Core revival mechanics—including player head drops, ritual structures, and the Revive Skull—are adapted from JakeCCz's [Hardcore Revive Mod](https://modrinth.com/plugin/hardcore-revive-mod).

- **Player Head Drops:** When you lose all your lives, your head drops where you died and your coordinates are sent to you in chat so your teammates can find it.
- **Revival Ritual:** Build a 3x3x3 beacon-like structure:
  - **Base:** Place an ore block (like gold or diamond blocks) in the center, Soul Sand at the four corners, and stairs of your choice on the remaining sides.
  - **Middle:** Place a fence block on the central ore, and Wither Roses on the four Soul Sand corners.
  - **Top:** Put the dead player's head on the fence to trigger the revival!
- **Craftable Items:**
  - **Revive Skull:** Right-click to open a GUI menu of dead players and get their heads for rituals.
  - **Extra Life:** Right-click to consume and gain +1 life (up to your maximum cap). The recipe is fully customizable.
- **Head Effects:** Putting on a dead player's head block grants you Speed II and Night Vision, making it easier to run the head back to a ritual structure.

---

## Requirements

- **Minecraft Version:** 1.21.X (Spigot, Paper, Purpur, Fabric, or Forge)
- **Java Version:** Java 21 or higher
- **Database:** SQLite (built-in, zero setup) OR MySQL/MariaDB (needed for multi-server networks)
- **Proxy (Optional):** Velocity with two backend servers

> **Important:** Keep `hardcore=false` in your `server.properties` file on all servers! SSoggySouls handles the lives and spectator mechanics internally, and vanilla hardcore will break this.

---

## Quick Start

1. Download the plugin and place it in your server's `plugins/` (or `mods/`) folder.
2. Start the server once to generate `config.yml`, then stop it.
3. Choose your database type in `config.yml`:
   - **For single servers:** Keep `type: "sqlite"`. You're done!
   - **For multi-server networks:** Set `type: "mysql"`, fill in your database details, and copy those exact connection settings into both server configs.
4. If running two servers, set `is-limbo-server: false` on your Main server and `true` on your Limbo server.
5. Restart and test the flow!

For detailed guides on proxy configurations, commands, and permissions, head over to the **[Quick Start Guide on our Wiki](https://SSoggy-Group.github.io/SSoggySouls-for-Hardcore/quick-start)**.

---

## Credits

- Revival mechanics are based on [JakeCCz's Hardcore Revive Mod](https://modrinth.com/plugin/hardcore-revive-mod).
- Developed by: SSoggyTacoMan
- Source Code: [GitHub Repository](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore)
