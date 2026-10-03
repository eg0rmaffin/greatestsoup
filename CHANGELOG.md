# Changelog

## [1.4.0] - 2026-10-03

### Added
- HBM's Nuclear Tech: the "M" Detector, a trinket that warns about Mask Man spawn rolls without spelling out the rules: it clicks more and more often as a roll gets close, pings on the roll, and its lens turns red while you could be picked. Crafted from a gas mask, a clock, redstone and iron plates. Works from the hotbar, the offhand or a Baubles trinket slot

### Changed
- The mod now adds an item, so it is needed on the client as well as on the server

## [1.3.0] - 2026-10-02

### Fixed
- HBM's Nuclear Tech: the shredder no longer turns ores and materials of other mods into scrap when the pack has a dust for them. HBM only gave a recipe to the first item of each ore dictionary name and never matched items registered with the wildcard meta; now every item gets one under HBM's own rules (71 more recipes in The Greatest Soup, among them copper, uranium, iron, titanium, tungsten and Draconium ores). Ores that still give scrap because no mod adds a dust for them are listed in the log

## [1.2.0] - 2026-10-02

### Fixed
- Minecraft: the inventory screen no longer turns white while arrows are stuck in the player. Arrows stuck in entities skip model parts that have no boxes, such as the Egon backpack that HBM's Nuclear Tech Extended adds to the player model, instead of throwing "bound must be positive" every frame

## [1.1.0] - 2026-10-01

### Fixed
- Corail Tombstone 4.8.0: no more crash (`IllegalAccessError`) when a mob targets a player under the Ghostly Shape effect. Tombstone's access transformer is now applied through this mod

### Added
- Mod logo, shown in the in-game mod list

### Changed
- License changed from MIT to MPL-2.0

## [1.0.0] - 2026-09-28

### Added
- Traveler's Backpack: the worn backpack is now added to the player's death drops, so Corail Tombstone puts it into the grave
- Config file `config/greatestsoup.cfg` with a switch for every fix, also editable in game
