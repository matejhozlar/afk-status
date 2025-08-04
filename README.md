# AFKStatus

![Java](https://img.shields.io/badge/Language-Java-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.7-green)
![ModLoader](https://img.shields.io/badge/Mod%20Loader-NeoForge-brightgreen)
![ServerMod](https://img.shields.io/badge/Type-Server--Side-orange)

<p align="center">
  <img src="docs/images/afkstatus-chat-preview.png" width="500">
</p>

**AFKStatus** is a lightweight, server-side mod for **Minecraft**, built on the **NeoForge** mod loader. It automatically tracks player activity and marks users as AFK (Away From Keyboard) when they stop moving or chatting for a configurable period. Players can also manually toggle AFK status using a simple `/afk` command.

### [AFKStatus on CurseForge](https://www.curseforge.com/minecraft/mc-mods/afkstatus)

---

## Features

- **Automatic AFK detection**  
  Detects inactivity by tracking movement and chat activity. Players are marked as AFK after a configurable timeout and broadcasted as such.

- **Manual AFK toggle**  
  `/afk` command lets players manually set or clear AFK status.

- **Customizable timeouts**  
  Set the AFK trigger delay and optional kick timeout via config.

- **Optional auto-kicking**  
  Automatically kicks players who stay AFK beyond a set time.

- **Custom messages and colors**  
  System messages are customizable, including color options.

- **Scoreboard integration**  
  Uses Minecraft's team system to visually tag AFK players with `[AFK]`.

- **Server-only**  
  No client-side installation required. Just drop the mod into your server's `mods/` folder.

<p align="center">
  <img src="docs/images/afkstatus-tab-preview.png" width="500">
</p>

---

## Configuration

After the first server launch, a configuration file (`afkstatus-server.toml`) is created in your `config/` directory.

| Config Key           | Description                                                                 | Default | Range / Options            |
|----------------------|-----------------------------------------------------------------------------|---------|-----------------------------|
| `afkTriggerTimer`    | Minutes of inactivity before a player is marked AFK                         | `5`     | 1–60 minutes                |
| `afkKickTimer`       | Minutes after being AFK before being kicked (set `0` to disable kicking)    | `0`     | 0–120 minutes               |
| `systemMessages`     | Whether to broadcast AFK status changes                                     | `true`  | `true` / `false`            |
| `checkIntervalTicks` | How often to check AFK status (20 ticks = 1 second)                         | `20`    | 1–1200 ticks                |
| `messageColor`       | Message color (e.g., `gray`, `yellow`, `red`, etc.)                         | `yellow`| See full color list below   |

**Valid colors:** `black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`

> **Tip:** Changes take effect on server restart.

### How to Edit

1. Stop your server.
2. Open the generated config file in a text editor.
3. Modify values as desired.
4. Save and restart the server.

---

## Commands

| Command | Description                                                                 |
|---------|-----------------------------------------------------------------------------|
| `/afk`  | Toggles your AFK status. Automatically broadcasts status changes to others. |

This lets players manually indicate they’re AFK or return without moving or chatting.

---

## Scoreboard Integration

AFKStatus uses Minecraft's scoreboard to add players to a team called `afkstatus`:

- Prefix: `[AFK] `
- Color: Configurable (default: `gray`)
- Visible in the tab list and nametags

Players are added/removed from this team as their status changes.

---

## Installation

1. Install **NeoForge** on your Minecraft server.
2. Place the **AFKStatus JAR** file into your server’s `mods/` directory.
3. Start or restart the server.

---

## Usage

- **Automatic AFK:** Stop moving and avoid chat. You’ll be marked AFK after the configured timeout.
- **Manual AFK:** Use `/afk` to toggle status.
- **Return from AFK:** Move, chat, or use `/afk` again to clear your AFK status.
- **Auto-Kick:** If enabled, players AFK too long will be disconnected with a reason message.

---

>## Acknowledgements
> - Inspired by the need for simple and effective AFK tracking in multiplayer servers.
> - Thanks to the NeoForge community for their tools and helpful examples.
