---
layout: default
title: SSoggySouls Documentation
description: Hardcore lives system plugin for Minecraft 1.21.X
---

## SSoggySouls Documentation

Hardcore lives system mod/plugin for Minecraft 1.21.X with Limbo exile, revival mechanics, and cross-server persistence.

> **Note:** Fabric and Forge versions are currently in an early testing phase. Expect frequent updates and please report any bugs you find!

[Modrinth](https://modrinth.com/project/Pb03qu6T){: .btn }
[GitHub](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore){: .btn }
[Releases](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/releases){: .btn }

## Overview

SSoggySouls is designed for Velocity proxy networks and provides a high-stakes hardcore loop:

- Configurable lives with max cap and extra-life mechanics
- SQLite spectator mode, plus `hybrid` and `limbo` modes for MySQL/proxy setups
- Ritual and command-based revival flows
- Automatic transfer between Main and Limbo servers
- MySQL/MariaDB persistence across backend servers

## Requirements

- Minecraft: 1.21.X (Spigot, Paper, Purpur, Fabric, or Forge)
- Proxy: Velocity
- Database: MySQL 5.7+ or MariaDB 10.2+
- Java: 21+
- Architecture: Main server + Limbo server

> Do not enable `hardcore=true` in `server.properties`. Leave it `false` and let SSoggySouls manage hardcore behavior.

## Documentation

- [Quick Start](quick-start)
- [Installation](installation)
- [Configuration](configuration)
- [Commands](commands)
- [Revival System](revival-system)
- [Troubleshooting](troubleshooting)
- [FAQ](faq)

## Quick Start Summary

1. Install the plugin on both backend servers.
1. Configure identical DB credentials on both servers.
1. Set `is-limbo-server` correctly on each server.
1. Ensure names match your `velocity.toml` server names.
1. Set Limbo spawn with `/setlimbospawn`.
1. Restart and verify with `/pstatus` and a revive test.

## Support

- [Issue Tracker](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore/issues)
- [Project Repository](https://github.com/SSoggy-Group/SSoggySouls-for-Hardcore)
