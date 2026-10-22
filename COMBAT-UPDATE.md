# PvP+ 0.10.6

Press H to swap your held weapon with the weapon stored in a sheath. Rebind it in
Controls > PvP+ > Swap Sheathed Weapon. Carry the sheath in your inventory or
offhand; offhand takes priority, otherwise the first inventory sheath is used.
An empty sheath stores the held weapon; an empty hand retrieves the stored weapon.
The swap preserves damage, names and enchantments and runs on the server.
The six supplied item textures are installed, including the wolf armor inventory icon.
Gathca Core maps to Combat Cache and Health Core maps to Healing Totem.

All seven longsword tiers have been removed, including recipes, projectile registrations,
enchantment tags and combat-cache rewards. Existing longswords are no longer supported.
Older longsword resource files are cleaned automatically when building.

Halberds now swing for their primary attack and can disable shields like axes.
Hold use/right-click for the spear's kinetic secondary attack and animation.
Their held sprites are larger and upright. Inventory icons and thrown scale are unchanged.

Use a Storm Core in your main hand with a netherite hammer in your offhand.
Each core is consumed and has a 5% chance to awaken the hammer. Success grants
Stormbound, Throwable I, Channeling I, Loyalty III and Riptide I together.
Hold use for at least half a second, then release for a Riptide spin—even on dry land.
The spin costs four durability and has an eight-second cooldown. Sneak-use throws
the hammer instead; Loyalty returns it. Channeling still requires vanilla thunderstorm
and exposed-sky conditions. Other tridents retain their normal water requirements.

Quarterstaffs and shurikens have been removed from registration, recipes, tags and
random rewards. Their old generated files are cleaned automatically during builds.
Existing copies in a saved world are no longer supported.
The supplied blunt arrow and grappling arrow PNGs are included unchanged.

Java 25 compilation and Fabric registry, recipe, enchantment and client rendering-hook
checks passed. All 81 registered items and 82 recipes validate. Interactive gameplay,
held-model alignment and multiplayer movement have not been tested here.
This ZIP contains source; GitHub Actions runs the full Gradle build and produces the JAR.

# Java 0.10.6 — combat and texture update

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
