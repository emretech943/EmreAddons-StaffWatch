# EmreAddons – StaffWatch (source prototype)

Target: Minecraft Java 1.21.11, Fabric, Java 21.

## What works in source
- Independent Fabric client initializer
- F8 manual demo trigger
- Simple animated skin head (2D; **not** Anubis' original 3D rotating cube)
- Red WATCHING YOU title, cooldown and detection of tab unlisting / spectator changes for a small starter list
- No auto-leave, auto-disconnect or server-side commands

## Important limitations
This is an **uncompiled, untested source prototype**. The Maven coordinates and Minecraft 1.21.11 API calls must be verified in an internet-enabled build environment. Staff heuristics are not proof someone is watching you. Staff names are intentionally a small example set; expand only with confirmed names. The full 3D effect, sound, and robust detection still need implementation.

## Build on a PC
1. Install Java 21 and Gradle 9-compatible tooling.
2. Open a terminal in this directory and run `gradle build` (online dependency downloads required).
3. If compilation fails, send the complete Gradle output for fixing.
4. The JAR would appear under `build/libs/` after a successful build.
5. Install Fabric API and put the JAR in the Minecraft `mods` directory.

**Do not install or execute unknown Anubis binaries.** This project does not include Anubis code or JARs.
