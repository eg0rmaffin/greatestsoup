# The Greatest Soup

**Small, focused fixes for bugs and conflicts between 1.12.2 mods.**

Big modpacks are a soup of hundreds of mods, and some of them don't get along: items get lost, events fire in the wrong order, things break in ways no single mod author ever sees. This mod collects fixes for such problems, one at a time, as I run into them in my own pack.

## How it works

- **Only what you need.** Every fix turns on only when the mod it targets is installed. If you don't have that mod, the fix does nothing.
- **Everything can be switched off.** Each fix has its own option in `config/greatestsoup.cfg`, also editable in game from the Mods list (unless its section says otherwise).
- **Safe to add or remove.** The mod adds no blocks, items or world data, so it can't damage your world.

## Fixes

### Traveler's Backpack + Corail Tombstone: the backpack goes into the grave

**The problem:** a worn Traveler's Backpack isn't in your inventory, it's in its own slot. When you die, Traveler's Backpack places or drops it *before* grave mods collect your items, so the backpack never makes it into the grave. Die in lava or the void, and it's gone.

**The fix:** on death, the backpack is taken off and added to your death drops like any other item.

- With **Corail Tombstone** (or any grave mod that collects `PlayerDropsEvent`), the backpack goes into the grave with the rest of your stuff.
- Without a grave mod, it simply drops on the ground.
- With `keepInventory` on, nothing changes: the backpack stays on your back.
- If another mod prevents the death (a totem-like item), the backpack is put back on.

*Config option: `travelersBackpack.backpackInGrave`. Tested with Traveler's Backpack 1.0.35 and Corail Tombstone 4.8.0.*

### Corail Tombstone: no crash when a mob targets a ghost

**The problem:** Tombstone 4.8.0 crashes the game (`IllegalAccessError`) whenever a mob targets a player under the Ghostly Shape effect, which happens every time you go back for your grave. Tombstone ships an access transformer for this, but its jar doesn't declare it, so Forge never applies it.

**The fix:** this mod declares the same access transformer, so Tombstone works as intended with its original jar.

*This fix can't be switched off (access transformers load before any config), but it's harmless: it only makes four Minecraft fields public.*

### Minecraft: white inventory screen with arrows stuck in the player

**The problem:** some mods add empty parts to the player model. When arrows are stuck in you, Minecraft can pick such a part to attach an arrow to and throws an error every frame, which overflows the OpenGL stack and turns the inventory screen white until the arrows wear off.

**The fix:** stuck arrows are rendered only on model parts that have something to attach to.

*Config option: `minecraft.safeArrowLayer`. Client side only.*

## More fixes coming

The list will grow as I find more problems. Hit a bug or conflict between mods that isn't handled anywhere else? Open an issue on [GitHub](https://github.com/eg0rmaffin/greatestsoup/issues).

## Modpacks

Feel free to include this mod in any modpack.

## Links

- [Source code](https://github.com/eg0rmaffin/greatestsoup) (MPL-2.0)
- [Changelog](https://github.com/eg0rmaffin/greatestsoup/blob/main/CHANGELOG.md)
- [Report an issue](https://github.com/eg0rmaffin/greatestsoup/issues)
