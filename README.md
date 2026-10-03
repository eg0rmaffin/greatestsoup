<p align="center"><img src="thegreatestsoup.png" alt="The Greatest Soup logo" width="200"></p>

# The Greatest Soup

A Forge mod for Minecraft 1.12.2 that collects bug fixes, mod conflict fixes and a few small additions for **The
Greatest Soup**, a private modpack. Every fix is enabled only when the mod it targets is installed, and each one can be switched off in
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

### Minecraft: white inventory screen with arrows stuck in the player

For every arrow stuck in an entity, Minecraft picks a random part of its model and a random box of that part to
attach the arrow to. Some mods add parts without boxes to the player model, and when such a part is picked, the arrow
layer throws `IllegalArgumentException: bound must be positive`. HBM's Nuclear Tech Extended does this: it attaches the
Egon backpack to the player's body as a model part that draws an OBJ model and has no boxes. The exception escapes in the middle of rendering,
so every frame leaves an extra matrix on the OpenGL stack until it overflows (`GL ERROR 1283: Stack overflow` in the
log), and the player preview in the inventory turns the whole screen white. It looks tied to places, like a floor of a
dungeon, because that's where skeletons shoot you; it goes away once the arrows wear off.

The fix renders stuck arrows the same way, but only on parts that have boxes. The first time a model with empty parts
shows arrows, the log names it, which points to the mod that added those parts.

Config: `minecraft.safeArrowLayer`. Client side only.

### HBM's Nuclear Tech: the shredder ignores ores of other mods

HBM builds most shredder recipes from the ore dictionary: `oreX` becomes two `dustX`, `ingotX` one, and so on, so
any mod's ore works as long as some mod adds the matching dust. Two bugs break that:

- for each ore dictionary name, only the first item gets a recipe, so when several mods add tin or copper ore, only
  one of them shreds into dust and the rest give scrap;
- items registered with the wildcard meta are stored under meta 32767, and the shredder looks recipes up by exact
  meta, so they never match.

The fix repeats HBM's rules after HBM's own setup, for every item of every name, with wildcard items expanded into
their variants. It only adds recipes where there were none, so HBM's own recipes and overrides stay as they are, and
it doesn't invent new dusts. Ores that still shred into scrap because the pack has no dust for their material are
listed in the log, which tells what a mod like JAOPCA would need to add.

Config: `hbm.shredderAllOreDictItems`, applied on restart. Tested with HBM's Nuclear Tech Extended 3.0.3.

## Additions

### HBM's Nuclear Tech: the "M" Detector

HBM's Mask Man doesn't spawn at random moments. Once every `maskmanDelay` ticks of total world time (216000 by
default, 3 hours of play), on that exact tick, a random player in the Overworld gets a 1 in `maskmanChance` roll,
but only if their radiation dose is at least `maskmanMinRad` (50) and they are underground (more than 3 blocks of
terrain above them). None of this is visible in game, so it is easy to catch him twice by chance or never at all.

The detector reveals the schedule without spelling out the rules:

- from 30 minutes before a roll it clicks now and then, from 10 minutes often, and it beeps every second through the
  last minute; the roll itself gives a sonar ping;
- its lens turns red while the player would be picked by a roll, and then every click comes twice;
- outside surface worlds, where no roll ever happens, it only gives off static.

The rules are read from HBM's own config (`MobConfig`), and a server sends its values to joining players, so the
detector always counts what the server actually rolls. It works from the hotbar, the offhand or, with Baubles, a
trinket slot. Crafted from an HBM gas mask, a clock, redstone and four iron plates.

Config: `hbm.maskDetectorSounds`, `hbm.maskDetectorVolume`. Tested with HBM's Nuclear Tech Extended 3.0.3.

## Installing and updating

Put `greatestsoup-<version>.jar` into `mods/` on both the server and the client: the mod adds an item, so both sides
need it. To update, **delete the old jar** and put the new one in its place: two versions side by side crash the game
on start. The fixes add nothing to the world; removing the mod only removes the detectors from it.

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

The mod is licensed under the [Mozilla Public License 2.0](LICENSE). You're free to use it in any modpack, and to
build on it: if you distribute modified versions of its files, they must stay under MPL-2.0. Versions up to 1.0.0
were released under the MIT license.

The build scripts (`build.gradle`, `settings.gradle`, `gradle.properties`, `gradle/scripts/`) come from the
[CleanroomMC ForgeDevEnv](https://github.com/CleanroomMC/ForgeDevEnv) template and stay under its
[MIT license](LICENSE-ForgeDevEnv).
