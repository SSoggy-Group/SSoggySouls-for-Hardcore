---
layout: default
title: SSoggySouls Documentation
description: Hardcore lives system plugin for Minecraft 1.21.X
---

## Welcome to the SSoggySouls Wiki

SSoggySouls is a hardcore lives plugin and mod built for Minecraft 1.21.X. Whether you want a standard standalone server with lives and custom spectator rules, or a multi-server setup behind a Velocity proxy that exiles dead players to a dedicated Limbo world, we've got you covered.

> **Note:** Fabric and Forge support is still in early testing. Expect frequent updates, and please let us know on GitHub if you run into any bugs!

[Modrinth](https://modrinth.com/project/Pb03qu6T){: .btn }
[GitHub Repository](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore){: .btn }
[Releases](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/releases){: .btn }

## What's Inside?

SSoggySouls adds a high-stakes cycle to your Minecraft community:
- **Custom Lives System:** Configure how many lives players start with and their maximum cap.
- **Limbo Exile & Spectating:** Dead players can either spectate on the main server or get teleported to a separate Limbo server.
- **Multiple Revival Flows:** Bring teammates back using in-game ritual structures (beacon setups), craftable items, or commands.
- **Cross-Server Syncing:** Automatically routes players between Main and Limbo servers and keeps data in sync using MySQL.
- **Standalone Support:** Easily run it on a single server with zero-config SQLite.

## Core Requirements

- **Minecraft Version:** 1.21.X (Spigot, Paper, Purpur, Fabric, or Forge)
- **Java:** 21 or newer
- **Database:** SQLite (built-in, great for single servers) or MySQL 5.7+ / MariaDB 10.2+ (for multi-server networks)
- **Proxy (Optional):** Velocity (needed only if you want a Main + Limbo server network)

> **Important:** Keep `hardcore=false` in your `server.properties` file! SSoggySouls manages the hardcore state internally. Enabling vanilla hardcore mode will break the plugin.

## Help Guides

- [Quick Start](quick-start) - Get up and running in just a few minutes.
- [Installation Guide](installation) - Detailed installation steps for different server environments.
- [Configuration Reference](configuration) - A breakdown of all config options in `config.yml`.
- [Commands & Permissions](commands) - Lists all commands and permission nodes.
- [Revival System](revival-system) - Learn how to build the revival ritual structure and craft custom items.
- [Troubleshooting](troubleshooting) - Solutions to common problems.
- [FAQ](faq) - Answers to frequently asked questions.

## Support

If you run into any issues or have ideas to improve the plugin, feel free to open a ticket on our [GitHub Issue Tracker](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues).
