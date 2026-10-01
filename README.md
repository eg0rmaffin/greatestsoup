<img src="thegreatestsoup.png" alt="The Greatest Soup logo" width="160" align="right">

# The Greatest Soup

A Forge mod for Minecraft 1.12.2 that collects bug fixes and mod conflict fixes for **The Greatest Soup**, a private
modpack. Every fix is enabled only when the mod it targets is installed, and each one can be switched off in
`config/greatestsoup.cfg` or in game from the mod list, unless its section says otherwise.

## Fixes

### Traveler's Backpack: worn backpack goes into the grave

A worn [Traveler's Backpack](https://www.curseforge.com/minecraft/mc-mods/travelers-backpack) is kept in a separate
capability slot, not in the inventory. On death, Traveler's Backpack handles it on `LivingDeathEvent`: it places the
backpack as a block nearby or drops it as an item. Both happen before the inventory is collected into
`PlayerDropsEvent`, which is where [Corail Tombstone](https://www.curseforge.com/minecraft/mc-mods/corail-tombstone)
gathers items for the grave. So the backpack never reached the grave and could end up in lava or the void.

The fix takes the backpack off just before Traveler's Backpack reacts to the death and adds it to the death drops:

- With Tombstone (or any grave mod that listens to `PlayerDropsEvent`), the backpack goes into the grave.
- Without a grave mod, it drops on the ground like any other item.
- With `keepInventory` on, nothing changes: the backpack stays on your back.
- If another mod cancels the death (a totem-like item, for example), the backpack is put back on.
- The Creeper backpack still explodes on death, as it does in Traveler's Backpack.

Config: `travelersBackpack.backpackInGrave`. Tested with Traveler's Backpack 1.0.35 and Corail Tombstone 4.8.0.

### Corail Tombstone: no crash when a mob targets a ghost

After death, Corail Tombstone gives the player the Ghostly Shape effect, and mobs are supposed to ignore such a
player. To make a mob drop its target, Tombstone 4.8.0 writes to a protected Minecraft field directly. That only
works with the access transformer that Tombstone ships in `META-INF/tombstone_at.cfg`, but its jar doesn't declare
that file in the manifest, so Forge never applies it. As a result, the game crashes with `IllegalAccessError` as soon
as any mob targets a player who just died, every time they go back for the grave.

This mod declares the same access transformer lines in its own manifest, so Forge applies them and Tombstone works as
intended with the original jar.

This fix can't be switched off: access transformers are applied before any config is loaded. It only makes four
Minecraft fields public and changes nothing on its own, so it is harmless without Tombstone too.

## Installing and updating

Put `greatestsoup-<version>.jar` into `mods/`. To update, **delete the old jar** and put the new one in its place:
two versions side by side crash the game on start. The mod adds no blocks, items or world data, so updating it or
removing it cannot damage saves.

Requires Minecraft 1.12.2 and Forge 14.23.5.2847 or newer.

## Building

Built with the [CleanroomMC ForgeDevEnv](https://github.com/CleanroomMC/ForgeDevEnv) template
(Gradle 9 + RetroFuturaGradle). Gradle itself must run on **Java 25**. Java 8 for compiling the mod is downloaded
automatically.

1. Run `./gradlew build`. The patched mods are downloaded from CurseForge as compile-only dependencies.
2. Use `build/libs/greatestsoup-<version>.jar`, not the `-dev` one.

## Releasing

Pushing a `vX.Y.Z` tag runs the [release workflow](.github/workflows/release.yml): it builds the mod, uploads it to
[CurseForge](https://www.curseforge.com/projects/1716660) and creates a GitHub release, both with the matching
section of `CHANGELOG.md`. The tag must match `mod_version` in `gradle.properties`. CurseForge needs the
`CURSEFORGE_TOKEN` repository secret.

## History

The Greatest Soup is my own private modpack. It isn't published on CurseForge or Modrinth, but I may share it
privately one day. This mod started as fixes for problems I ran into while playing it. They aren't tied to the pack,
though: each fix only targets the mods it names, so it can be useful in any 1.12.2 pack with those mods.

## License

[MIT](LICENSE)
