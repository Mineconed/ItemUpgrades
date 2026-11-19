<p align="center"><img src="ItemUpgrades_ikon_128.png" width="128"></p>
<h1 align="center">ItemUpgrades</h1>
<p align="center">Upgrade swords, tools and armor in a menu with XP or money.<br>Made by <b>Mineconed</b> • Minecraft 1.8 to 1.21</p>

## Features
- ⚒️ **Upgrade menu** for the item in your hand
- ⭐ **Levels** with stars in the lore (max level you choose)
- ⚔️ Each level gives +1 to the enchants you choose (Sharpness, Efficiency, Protection...)
- 💰 Pay with **XP levels** or **money** (Vault)
- 🎲 **Success chance** per level for extra risk
- 📉 Optional: lose a level when it fails
- 📢 Broadcast when someone reaches max level
- ⚙️ Make your own categories (swords, axes, armor, bows...)
- 🔓 Can go above the normal enchant max
- 🌐 English and Danish built in

## Setup
1. Put **ItemUpgrades.jar** in your plugins folder
2. Start the server. Done!
3. Hold a sword and type **/upgrade**

## Commands
| Command | What it does |
|---|---|
| `/upgrade` | open the upgrade menu |
| `/upgrade setlevel <level>` | set the level of the item in your hand |
| `/upgrade reload` | reload |

## Permissions
| Permission | What it does |
|---|---|
| `itemupgrades.use` | use /upgrade (everyone) |
| `itemupgrades.free` | upgrade for free |
| `itemupgrades.admin` | setlevel and reload (op) |

## Config
| Setting | What it does |
|---|---|
| `max_level` | max upgrade level |
| `cost_type` | xp or money |
| `cost.base / cost.per_level` | the price |
| `success_chance` | chance in percent per level |
| `downgrade_on_fail` | lose a level when it fails |
| `broadcast_max` | tell everyone at max level |
| `categories` | what can be upgraded and which enchants |

## Versions
1.8 to 1.21.
