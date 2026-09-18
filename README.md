# Sonarus Helper

Client-side Fabric helper for the Sonarus Minecraft server.

## Version

Sonarus Helper **1.0**.

## Current feature

While connected through `play.sonarus.win`:

- works only on Minecraft Dialog API screens;
- `Enter` and numpad `Enter` press the active and visible button whose visible text is exactly `Войти`;
- does nothing on other servers;
- contains no Sonarus Helper diagnostic runtime logging.

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

The source projects are under `versions/<minecraft-version>/`.

Compatibility branches:

- 1.21.6-1.21.8: legacy `Screen.keyPressed(int, int, int)`;
- 1.21.9-1.21.11: `KeyInput`;
- 26.1-26.2: unobfuscated Mojang names, Java 25;
- 26.3: SDL-era input through vanilla `InputConstants` instead of GLFW.

The dialog-button search logic remains the same across versions: direct screen buttons, the dialog layout, and the scrollable dialog body are inspected for the exact `Войти` action.

## Build one version

From the project root:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-one.ps1 1.21.10
```

Example for the newest target in this project:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-one.ps1 26.3
```

The script automatically uses:

- `remapJar` for 1.21.x;
- `jar` for 26.x, where the game is no longer distributed with the old obfuscated/remap workflow.

The resulting production JAR is copied to `dist\` with a version-specific filename.

## Build all supported versions

```powershell
powershell -ExecutionPolicy Bypass -File .\build-all.ps1
```

Expected artifacts:

```text
dist\sonarus-helper-1.0-mc1.21.6.jar
dist\sonarus-helper-1.0-mc1.21.7.jar
dist\sonarus-helper-1.0-mc1.21.8.jar
dist\sonarus-helper-1.0-mc1.21.9.jar
dist\sonarus-helper-1.0-mc1.21.10.jar
dist\sonarus-helper-1.0-mc1.21.11.jar
dist\sonarus-helper-1.0-mc26.1.jar
dist\sonarus-helper-1.0-mc26.1.1.jar
dist\sonarus-helper-1.0-mc26.1.2.jar
dist\sonarus-helper-1.0-mc26.2.jar
dist\sonarus-helper-1.0-mc26.3.jar
```

## Verification

Run:

```powershell
python .\verify-dist.py
```

The verifier checks every expected JAR for:

- mod version `1.0`;
- the exact Minecraft dependency;
- client-only environment;
- the Sonarus Helper mixin config.

Current verification: **11/11 expected JARs passed**.
