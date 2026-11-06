# PvP+ material update

Minecraft 26.3, Fabric, Java 25. This is an update to the 0.10.15 source project.

Your weapon sprites remain the base artwork. Compatibility equipment uses colors
sampled from the matching mod's ingot texture. Native palette swaps preserve the
custom weapon silhouettes, handles, strings, UVs and animation frames. The
material names, source versions, sampled colors and texture paths are recorded in
`scripts/compat_materials.json`.

| Material mod | Added PvP+ weapon materials |
| --- | --- |
| Create Fly | Brass, zinc |
| Alloyed | Bronze, steel |
| The Silver Age | Silver |
| The Lead Age | Lead |
| Mielon's The Sift | Siftite |
| Advanced Netherite | Netherite-iron, netherite-gold, netherite-emerald, netherite-diamond |

Each material gets PvP+ bows, crossbows, knives, battleaxes, flails, hammers,
scythes, halberds, shields and arrows. Existing provider tools, player armor and
spears are reused. Brass, zinc and bronze get complete tool/armor sets and spears.
Advanced Netherite custom weapons upgrade through the four tiers at a smithing
table, retaining their data. Recipes and creative entries activate when the
corresponding material mod is installed. Registry entries remain stable if a
material mod is removed later.

Horse, wolf and nautilus armor are included for rose gold and the supported
materials. Existing silver, lead and steel horse/nautilus armor are reused.
Crafting recipes cover the missing routes, including vanilla metal/diamond
horse and nautilus armor. Vanilla leather crafting, netherite smithing and
Alloyed's existing steel armor recipes are preserved.

The rose-gold shield uses the supplied iron-shield artwork as its base, with
rose-gold metal shades. Every item with the standard shield-blocking component
is lowered in first person, including other mods' shields. Blocking strength,
third-person models and hitboxes are unchanged.

## Other integrations

- **Iron Wolf Armor:** material wolf armor is registered in its recognition and
  damage-absorption tags. Repair ingredients and equipment slots use standard
  item components. These armors also work without Iron Wolf Armor installed.
- **Tiered Backpacks:** new chestplates use the chest-armor tag that its attachment
  recipe checks. This allows existing backpacks to attach; no new backpack tiers
  are added. The mod's fireproof restriction for netherite backpacks stays intact.
- **Armored Foes:** normal humanoid equipment layers work with its renderers.
  One in eight eligible natural monster spawns can exchange existing iron armor
  for an active iron-class material set. Empty slots and saved equipment are not
  filled or replaced. Enchantments are retained.
- **Detail Armor Bar Reconstructed:** the new PvP+ player armor sets have material-
  colored armor-bar definitions. Existing armor from material mods keeps its own
  behavior.
- **Ice Caves Backport:** ice arrows use its `ice_ball` projectile plus an arrow
  when installed. The original ice-arrow recipe remains available without it.
- **Elytra Trims and More Armor Trims:** rose gold is a native trim material. Its
  palette works with armor and elytra patterns. Elytra Trims already supplies the
  wing textures for More Armor Trims. No copies of those patterns are bundled.

Break a mature crop with a scythe to harvest mature crops in the surrounding 3×3
area. Sneak for a single-block harvest. Immature crops remain intact. Harvesting
uses normal player block breaks, drops and protection callbacks, and costs one
additional durability per harvested crop. It does not automatically replant.

## Validation and limits

Java 25 compilation, 276-item resource coverage, palette/handle checks and a
native Fabric server bootstrap were checked locally. Conditional recipe JSONs
were decoded with Minecraft's recipe codecs and Fabric's condition codecs;
foreign item registry fixtures were used for that isolated recipe test. Native
client class loading checked the new shield/equipment mixin targets.

The compact GitHub upload regenerates byte-identical resources to the full source
tree. A full local Gradle build was attempted but could not resolve Fabric Loom
1.17.21 in this environment; the GitHub workflow still needs to complete.

These checks are not a full interactive test of all third-party mods together.
In-game equipment rendering, multiplayer behavior and the complete modpack still
need playtesting. Compatibility is based on the 26.3 Fabric versions recorded in
`COMPATIBILITY-VERSIONS.json`; future versions may change IDs or APIs.

New weapon artwork and third-party mod JARs are not bundled. The earlier licensed
Rose Gold Equipment textures and their notices remain included.

## Siftite smithing correction

Custom siftite bows, crossbows, knives, battleaxes, shields, flails, hammers,
scythes and halberds now upgrade their netherite equivalents at a smithing table.
Use The Sift's Siftite Upgrade Smithing Template and one siftite ingot.
This matches The Sift 1.1.2's own equipment progression. Existing recipe IDs
are replaced, so no crafting-table bypass remains for these nine items.
Arrows and animal armor retain their existing recipes.

Recipe validation and native recipe decoding passed. A full Gradle build and
in-game testing remain outstanding. All previous fixes are included.

## Siftite Storm Core support

Hold the siftite hammer in your offhand and use a Storm Core in your main hand.
It has the same 5% awakening chance as the netherite hammer; each attempt consumes
a core. Success grants Throwable I, Channeling I, Loyalty III and Riptide III.
The awakened siftite hammer uses the same water-free Riptide and sneak-to-throw
behavior as the netherite version, with no added cooldown. Upgrading an awakened
netherite hammer to siftite retains its enchantments through normal smithing.
