# TB Grave Fix

A small server-side Forge mod for Minecraft 1.12.2 that makes the backpack you are **wearing** from
[Traveler's Backpack](https://www.curseforge.com/minecraft/mc-mods/travelers-backpack) end up in your grave from
[Corail Tombstone](https://www.curseforge.com/minecraft/mc-mods/corail-tombstone) when you die.

## The problem

A worn Traveler's Backpack is not kept in your inventory but in a separate capability slot. When you die,
Traveler's Backpack handles it on `LivingDeathEvent`: it either places the backpack as a block nearby or drops it as
an item. Both happen before the game collects your inventory into `PlayerDropsEvent`, which is where Tombstone
gathers items for the grave. So the backpack never ends up in the grave. It lies somewhere around your death spot,
possibly in lava or the void.

## What this mod does

- Takes the backpack off just before Traveler's Backpack reacts to the death, so its own death handling is skipped.
- Adds the backpack to the player's death drops, where Tombstone (or any other grave mod listening to
  `PlayerDropsEvent`) collects it together with the rest of the inventory.
- Without a grave mod, the backpack simply drops on the ground like any other item.
- With `keepInventory` on, nothing changes: the backpack stays on your back, as before.
- If another mod cancels the death (a totem-like item, for example), the backpack is put back on.
- The Creeper backpack still explodes on death, as it does in Traveler's Backpack.

## Requirements

- Minecraft 1.12.2, Forge 14.23.5.2847 or newer
- Traveler's Backpack (tested with 1.0.35). Without it, the mod does nothing.
- Corail Tombstone is optional (tested with 4.8.0)

The mod is needed only on the server (in single player, that is the game itself). Clients can join a server that
has it without installing it.

## Building

Built with the [CleanroomMC ForgeDevEnv](https://github.com/CleanroomMC/ForgeDevEnv) template
(Gradle 9 + RetroFuturaGradle). Gradle itself must run on **Java 25**. Java 8 for compiling the mod is downloaded
automatically.

1. Put `TravelersBackpack-1.12.2-1.0.35.jar` and `tombstone-1.12.2-4.8.0.jar` into `libs/`.
   They are compile-only dependencies and are not included in this repository.
2. Run `./gradlew build`.
3. Use `build/libs/tbgravefix-<version>.jar`, not the `-dev` one.

## License

[MIT](LICENSE)
