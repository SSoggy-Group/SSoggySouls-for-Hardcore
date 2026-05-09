---
layout: default
title: Frequently Asked Questions
---

# Frequently Asked Questions (FAQ)

Here are quick answers to some of the most common questions about installing, configuring, and playing with SSoggySouls.

---

## Installation & Setup

### Can I install SSoggySouls directly on my Velocity proxy?
**No.** SSoggySouls is a backend server plugin, not a proxy plugin. Install it in the `plugins/` (or `mods/`) folder of your backend servers (Main and Limbo). Putting the JAR file in your Velocity plugins folder will not work.

### What servers do I need to run this?
It depends on your preferred setup:
1. **Single Server Setup:** You only need one Minecraft server. When players run out of lives, they enter spectator mode. There's no proxy or Limbo server needed, and you can use the local SQLite database option with zero setup.
2. **Linked Multi-Server Setup:** You need a Main gameplay server, a dedicated Limbo purgatory server, a Velocity proxy connecting them, and a shared MySQL/MariaDB database to sync player states.

### Can I use BungeeCord or Waterfall instead of Velocity?
Possibly. BungeeCord/Waterfall support is currently untested, but it may work if you set up IP forwarding and enable `bungeecord: true` in your backend servers' `spigot.yml` files. Velocity is highly recommended for the best experience.

### Do I need to enable Minecraft's built-in hardcore mode?
**No. Keep `hardcore=false`** in your `server.properties` file on all servers. SSoggySouls manages player lives and spectator/exile states internally. Enabling vanilla hardcore mode will cause conflicts and break the plugin.

---

## Gameplay & Death Mechanics

### What happens when my character dies?
It depends on the `death-mode` you have configured in `config.yml`:
- **Spectator Mode (default for standalone SQLite):** When a player loses all lives, they enter spectator mode on the Main server and remain there until revived. They are never sent to a Limbo server.
- **Limbo Mode (Strict):** As soon as a player runs out of lives, they are instantly exiled to your Limbo server.
- **Hybrid Mode:** When a player runs out of lives, they become a spectator on the Main server for a set duration (defaults to 5 minutes). This gives teammates a quick window to build a ritual structure. If they aren't revived before the timer expires, or if they log out, they are exiled to the Limbo server.

### Can dead players visit the Main server?
No. Once a player is dead and exiled to Limbo, they cannot leave Limbo until someone revives them.

### Can living players visit Limbo?
Yes! Living players can type `/limbo` in chat to visit Limbo and hang out with dead teammates. When they want to return, they can type `/leavelimbo` or `/hub` to teleport back to the Main server.

### What is the revive cooldown?
After being revived, players receive a safety window of invincibility (defaults to 30 seconds). This protects them from losing another life immediately if they accidentally fall, suffocate, or get attacked right after spawning.

---

## Revival System

### How do you revive someone?
Teammates can bring players back using **four different methods**:
1. **Ritual Structure:** Build a physical 3x3x3 beacon-like structure using Soul Sand, stairs, an ore block, Wither Roses, and a fence, then place the dead player's head on top.
2. **Revive Skull:** Right-click a crafted Revive Skull to open a GUI menu of dead players and retrieve their heads for the ritual.
3. **Moderator Command:** Staff with the proper permission can run `/revive <player>`.
4. **Admin Command:** Staff can run `/psadmin revive <player>` (useful from consoles or Command Blocks).

### What ore block do I need to use for the ritual structure base?
Any ore block works! You can use Gold, Diamond, Emerald, Iron, Coal, Redstone, or Netherite blocks. This is a great way to customize the difficulty or look of the ritual on your server. For example, you can require Diamond blocks for high difficulty, or Iron blocks for a more casual experience.

### What effects do you get when wearing a dead player's head?
If you put on a dead teammate's head block, you will receive **Speed II** and **Night Vision** effects. This is a neat utility feature to help players safely run the head back to their base or closest revival altar.

---

## Configurations & Customs

### Can I customize player-facing messages?
Yes! You can edit every single message, title, and prefix in the `messages` section of `config.yml`. It supports standard Minecraft color codes (using the `&` symbol).

### Can I disable the grace period?
Yes. To turn off the new player grace period completely, set `lives.grace-period` to `"0"` in your config file.

### Can I change how many lives players get back on revival?
Yes. You can configure this with the `lives.on-revive` setting in your config file:
- Set it to `1` to have players revive with one life.
- Set it to a higher number if you want a more forgiving gameplay loop.

### Can I reload the configuration without restarting my servers?
Yes! You can reload most settings by typing `/psadmin reload` in-game or in your server console. However, database connections or server identification changes will still require a full server restart to apply.

---

[← Troubleshooting Guide](troubleshooting) | [Back to Home](index)
