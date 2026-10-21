# PvP+ 0.10.3

Renamed the mod to PvP+ and installed the 60 supplied copper/diamond/gold/iron/netherite/stone bow and
crossbow textures, including draw stages and loaded arrow/firework states.
All 0.10.2 features and the other supplied weapon artwork are retained.
The internal mod ID and item namespace remain pvpoverhaul for saved-world compatibility.
The build produces pvp-plus-0.10.3.jar. Remove the old mod JAR when installing it;
this replaces the existing mod and should not be installed alongside it.

# Java 0.10.3 — combat and texture update

For Fabric Minecraft 26.3, Java 25. Install the same mod version on server and clients.

- Target Lock (R): the selected living target gets a glowing outline visible to you,
  and your crosshair tracks it. Press R again or sneak to release. Switching weapons,
  death, loss of sight, changing dimension or leaving the 24-block range releases it.
  The activation cooldown remains 16 seconds.
- Dead Eye (V): selects up to three closest visible enemies within 24 blocks and
  fires one aimed shot at each, two ticks apart (about 0.2 seconds for the whole burst).
  Targets are hostile mobs and attackable non-creative players; allies and tame pets
  are excluded. It stops if the weapon changes or ammo runs out. Cooldown: 20 seconds.
  No tick-rate slowdown. Arrow velocity remains normal to preserve damage balance.
  Native ammo consumption, Infinity, quiver selection and arrow effects are retained;
  this ability fires one projectile per target, without extra Multishot copies.
  Loaded crossbow magazine rounds are used first.
- One slingshot, without material tiers: 128 durability, cobblestone ammo, 4 damage,
  twelve ticks to fully draw and an eight-tick cooldown. Craft with three sticks in
  a Y shape and one string in the center. Repair with string. It has its own pebble entity.
- All 37 supplied PNGs are included unchanged: seven halberd icons, seven held
  halberds, seven hammers, seven scythes, seven flails, an ice arrow and the slingshot.
  Flail_stobe.png maps to stone_flail. Halberds use the small icon in inventory and
  the large held sprite in hand and when thrown. Thrown size remains 1.35, and the
  rotation follows the new upper-left-pointing artwork.

Eligible skills: copper through netherite bows have Target Lock; iron, diamond and
netherite bows have Dead Eye. The netherite crossbow has both. Keybinds are rebindable.

Validation: Java 25 compilation against Minecraft 26.3/Fabric, generated resource
checks, native recipe/enchantment/registry decoding, and client mixin loading checks.
Interactive gameplay, appearance and multiplayer latency still need an in-game test.
The ZIP contains source, not an installable mod JAR. GitHub runs the full Gradle build.
