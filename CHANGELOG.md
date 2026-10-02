# Changelog

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
