# Sonarus Helper

Client-side Fabric helper for the Sonarus Minecraft server.

## Version

Sonarus Helper **1.2**.

## Features

While connected through Sonarus:

- `Enter` and numpad `Enter` activate the visible `Войти` button on Minecraft Dialog API screens;
- press `H` to open Sonarus Helper settings;
- optional Windows notifications for nickname mentions and Helper updates;
- notifications can be limited to times when Minecraft is not focused;
- built-in update checker for GitHub Releases;
- optional automatic download of the correct JAR for the current Minecraft version, installed after Minecraft closes;
- no Sonarus Helper diagnostic runtime logging.

Recognized Sonarus addresses include `play.sonarus.win`, `10.29.240.51:26565`, and `178.168.208.14:26565`.

Settings are stored locally in `config/sonarus-helper.json`.

## Supported Minecraft versions

Each Minecraft version has its own production JAR:

- 1.21.6
- 1.21.7
- 1.21.8
- 1.21.9
- 1.21.10
- 1.21.11
- 26.1
- 26.1.1
- 26.1.2
- 26.2
- 26.3

Download the JAR matching your exact Minecraft version from **GitHub Releases** and place it in the client `mods` folder.

The source projects are under `versions/<minecraft-version>/`.

## Compatibility

- 1.21.6-1.21.8: legacy screen/input APIs;
- 1.21.9-1.21.11: `KeyInput`;
- 26.1-26.2: unobfuscated Mojang names, Java 25;
- 26.3: SDL-era input through vanilla `InputConstants` instead of GLFW.

The original dialog-button behavior remains Sonarus-only and searches for the exact visible `Войти` action.
