---
layout: default
title: Revival System Guide
---

# Revival System Guide

This guide covers all the ways you can bring dead players back to life on your server. We support everything from immersive in-game ritual structures to craftable items and administrative commands.

> **Credits:** Core revival mechanics (like player head drops, ritual structures, and the Revive Skull) are based on JakeCCz's excellent [Hardcore Revive Mod](https://modrinth.com/plugin/hardcore-revive-mod). Advanced features like ghost tracking, customizable block tags, and database cleanups were ported from the **RevivalPlus DLC by Cera and JakeCCz**.

---

## Revival Methods Overview

There are **four ways** to revive a player who has run out of lives:

- **Ritual Structure (Any player):** Build a physical 3x3x3 beacon-like structure and place the dead player's head on top to bring them back.
- **Revive Skull (Any player):** Craft a special item to open an in-game menu of dead players and retrieve their heads.
- **Moderator Command (Staff):** Run `/revive <player>` (requires the `ssoggysouls.revive` permission).
- **Admin Command (Staff):** Run `/psadmin revive <player>` (requires the `ssoggysouls.admin` permission).

All of these options work concurrently, so you can mix and match them to suit your community's playstyle.

---

## Building the Revival Ritual Structure

The most popular and immersive way to revive teammates is by constructing a physical ritual altar. It is a 3x3x3 structure made of three distinct layers.

### Layer 1: The Base (3x3 grid)
Dig a 3x3 shallow pit or find a flat patch of ground and lay down these blocks:
- **Corners:** 4 Soul Sand blocks
- **Edges:** 4 Stair blocks of your choice (stone, wood, quartz, etc. all work)
- **Center:** 1 Ore block (Gold, Diamond, Emerald, Iron, or Netherite blocks work great!)

```text
  [Soul Sand]       [Stair]       [Soul Sand]
    [Stair]       [Ore Block]       [Stair]
  [Soul Sand]       [Stair]       [Soul Sand]
```

### Layer 2: The Middle
Construct the middle section on top of your base:
- **Corners:** Place 4 Wither Roses directly on top of the Soul Sand corner blocks.
- **Center:** Place 1 Fence block directly on top of the central ore block.

```text
 [Wither Rose]       [Empty]       [Wither Rose]
    [Empty]          [Fence]          [Empty]
 [Wither Rose]       [Empty]       [Wither Rose]
```

### Layer 3: The Top
The final piece of the ritual:
- **Center:** Place the dead player's head directly on top of the central fence post.

```text
    [Empty]          [Empty]          [Empty]
    [Empty]       [Player Head]       [Empty]
    [Empty]          [Empty]          [Empty]
```

### What happens next?
Once you place the dead player's head on the fence, the plugin will automatically detect that the structure is complete. It will:
1. Update the player's status to alive in the database.
2. Safe-transfer them back from Limbo to the Main gameplay server.
3. Restore their starting lives (configurable in `config.yml`, defaults to 1).
4. Remove the head block. (If you have `leave-structure-base: false` configured, the entire base structure will be destroyed; if `true`, the base stays intact for future revivals).

---

## Craftable Items

### 1. The Revive Skull
If a player's original head was lost in lava, deep underground, or if you simply don't want to run thousands of blocks to retrieve it, you can craft a **Revive Skull**.

#### The Recipe (Shapeless)
Combine these items anywhere in your crafting grid:
- 4× Obsidian
- 2× Ghast Tears
- 2× Totem of Undying
- 1× Any Player or Mob Skull/Head

```text
[ Obsidian ]     [ Ghast Tear ]     [ Obsidian ]
[ Totem ]        [ Any Head ]       [ Totem ]
[ Obsidian ]     [ Ghast Tear ]     [ Obsidian ]
```

#### How to use it
1. Hold the Revive Skull and right-click.
2. A custom GUI chest-menu will open, showing a list of all currently dead players.
3. Click on a player's name.
4. Their custom player skull will drop directly into your inventory.
5. Use that skull on top of a ritual structure to bring them back!

### 2. Extra Life Items
You can also let players craft custom items that grant them +1 life when consumed (up to their server maximum cap). By default, this item displays as a Nether Star.

#### Default Recipe
Configure your crafting table like this:
- **Top Row:** Diamond Block, Emerald Block, Diamond Block
- **Middle Row:** Netherite Ingot, Nether Star, Netherite Ingot
- **Bottom Row:** Gold Block, Emerald Block, Gold Block

```text
[ Diamond Block ]   [ Emerald Block ]   [ Diamond Block ]
[ Netherite Ingot ] [ Nether Star ]     [ Netherite Ingot ]
[ Gold Block ]      [ Emerald Block ]   [ Gold Block ]
```

#### Customizing the Recipe
If you want to make Extra Lives easier or harder to craft, you can edit the layout in `config.yml`. For example, here's how to make a recipe using only Emerald Blocks and Diamonds:
```yaml
extra-life:
  enabled: true
  item-material: "NETHER_STAR"
  recipe:
    row1: "DDD"
    row2: "DED"
    row3: "DDD"
    ingredients:
      D: "DIAMOND_BLOCK"
      E: "EMERALD_BLOCK"
```

---

## Head Mechanics

Dead player heads are the central currency of our revival mechanics, and we've built a few systems to make working with them engaging and stable.

### How heads behave on death
In `config.yml`, you can choose how heads behave when a player runs out of lives using the `hrm.head-place-as-block` toggle:

- **Block Mode (true, default):** The head is placed as a permanent skull block at the coordinates of their death. The plugin scans *upward* from their death location to find the first open air pocket on solid ground. This means if they die inside a lava lake, their head block safely emerges *above* the surface. It cannot burn, despawn, or wash away. Break the block to pick up the skull.
- **Item Entity Mode (false):** The head drops as a standard item on the ground. We recommend turning on `hrm.head-no-despawn: true` (so the item never despawns) and `hrm.head-fireproof: true` (so the item survives lava or explosions) if you choose this mode.

### Wearing dead player heads
If you place a dead teammate's head on your character's head slot, you will receive **Speed II** and **Night Vision** effects. This is a neat lore-friendly mechanic to help you safely carry their head through the night back to your base or nearest revival altar.

### Automatic Head Cleanup
To prevent players from duplicating skulls or cluttering chests with old heads, the plugin runs a thorough cleanup check whenever a player is revived (through any method).
- **Pass 1 (Instant):** The plugin looks up the exact coordinate map where the head block was placed, force-loads the chunk, safely removes the block, and releases the chunk.
- **Pass 2 (Fallback):** The plugin performs a tick-spaced sweep across all loaded worlds, inventories, ender chests, shulker boxes, and item frames to remove any remaining copies of that specific player's head. We split this work over multiple server ticks to prevent any TPS drop or lag spikes.

---

## Troubleshooting Revivals

### The ritual structure is complete, but nothing happens
- **Double-check the layout:** Make sure you are using Soul Sand at the corners, and that the fence post is sitting *directly* on top of your central ore block.
- **Check the head block:** Ensure the head sitting on top belongs to the actual dead player, and that they are marked as "Dead" (you can verify this by typing `/pstatus <player>`).
- **Check config switches:** Verify that `hrm.structure-revive` and `main.detect-hrm-revive` are both set to `true` on your Main server.

### The Revive Skull won't open the menu
- Make sure `hrm.revive-skull-recipe` is set to `true` in your configuration.
- The GUI only lists players who are currently dead. If there are no dead players registered in your database, the menu will open empty or show a message.

---

[← Configuration Reference](configuration) | [Back to Home](index) | [FAQ →](faq)
