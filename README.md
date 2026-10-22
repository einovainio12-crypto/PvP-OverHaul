# PvP+ 0.10.9

Scythes now sweep all eligible enemies in a broad forward area, up to 4.5 blocks
from the player and 3 blocks around the struck target. Sweep damage is 3 HP per
extra target, with no two-target cap. Walls and allies are excluded.

Target Lock is limited to iron, diamond and netherite bows. Crossbows cannot use it.
It costs 1 exhaustion to activate and 0.4 per second while tracking; saturation is
used before visible hunger. Tracking stops at six food points (three icons).
Creative players bypass hunger costs. Pole Jump adds only 0.1 exhaustion per jump
and uses the same minimum food requirement. Downward Lunge retains its native cost.

Flails gain 1.5 blocks of melee reach (4.5 blocks total at the default player range).
Right-click casts a fishing bobber; right-click again reels it in and can pull a hooked
entity. Native fishing and durability costs apply. Sneak-use still throws a flail
with Throwable. Flail held models are turned back 180 degrees from 0.10.8.

Halberds rotate 180 degrees in third person only; first-person transforms are unchanged.
Halberds can take Pole Jump. Hold the halberd and press G to jump; right-click still
uses its spear-style secondary attack. Pole Jump and Throwable remain exclusive.

Thrown maces have at least six base damage. A successful entity hit with Wind Burst
creates the native wind explosion at the impact, once per hit, at the enchantment's
strength. The thrown hit does not require a melee smash or falling player.
Stormbound is removed. Successful Storm Core rolls grant Riptide III, Loyalty III,
Channeling I and Throwable I; the netherite hammer can spin on dry land.


Removed the pale background from the weapon sheath icon; it now has transparency.

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
Their held sprites are larger; the third-person view is rotated 180 degrees. Inventory icons and thrown scale are unchanged.

Use a Storm Core in your main hand with a netherite hammer in your offhand.
Each core is consumed and has a 5% chance to awaken the hammer. Success grants
Throwable I, Channeling I, Loyalty III and Riptide III together.
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

# Java 0.10.9 — combat and texture update

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

Eligible skills: iron, diamond and netherite bows have Target Lock and Dead Eye.
The netherite crossbow has Dead Eye only. Keybinds are rebindable.

Validation: Java 25 compilation against Minecraft 26.3/Fabric, generated resource
checks, native recipe/enchantment/registry decoding, and client mixin loading checks.
Interactive gameplay, appearance and multiplayer latency still need an in-game test.
The ZIP contains source, not an installable mod JAR. GitHub runs the full Gradle build.

---

# PvP+ — Fabric 26.3

A first playable implementation of the supplied weapon overhaul concept, with a GitHub Actions builder and a Java 25 Codespaces workspace. **Minecraft Java Edition 26.3 only; not Bedrock or older 1.21.x.** Install this mod and Fabric API on the server and every client.

Pinned toolchain: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, Loom 1.17.21, Gradle 9.6.0, Java 25. Minecraft 26.3 is unobfuscated: this project uses Mojang class names directly and intentionally has no Yarn mappings dependency.

## Put this in your new GitHub repository

The fresh kit needs **one project upload**. Extract it, open `UPLOAD-THIS`, and drag everything **inside** that folder (including `src`, `scripts` and `gradle`) into the repository root. Create `.github/workflows/build.yml` in GitHub's file editor using the visible `workflow-build.yml.txt` supplied outside that folder. Follow `START-HERE.txt`. `Complete-project` contains the full source and generated textures/resources for local or git-based use; browser users only need `UPLOAD-THIS` plus the workflow.

The build now runs `generateResources` before validation and packaging. The included Python scripts generate all current PNG textures, models, recipes, tags, enchantments and translations, repairing missing images and old definitions. The browser kit includes Java, scripts, three metadata files and a packed sprite input; it does not require hundreds of individual generated resource uploads.

1. Create an empty GitHub repository, for example `pvp-plus`. Leave the “add README” and “add license” options unchecked.
2. Extract the ZIP. Open a terminal **inside the extracted `Complete-project` folder**; `build.gradle` and `.github` must be at the repository root.
3. Run these commands, replacing `YOURNAME` and the repository name:

```bash
git init -b main
git add .
git commit -m "Add PvP+ for Fabric 26.3"
git remote add origin https://github.com/YOURNAME/pvp-plus.git
git push -u origin main
```

For browser uploads, follow the single-upload instructions above. Create hidden configuration files directly in GitHub's file editor from the visible templates.

4. Open **Actions → Build Fabric 26.3**. A push starts the build; you can also choose **Run workflow**.
5. Open the completed green run and download `pvp-plus-26.3-ubuntu-latest` from **Artifacts**. Extract it. Install **`pvp-plus-0.10.9.jar`**, not the `-sources.jar`, in your game's `mods` folder. Update the server and clients to the same mod version.
6. Install Fabric Loader for **26.3** and Fabric API **0.162.0+26.3** or a compatible newer 26.3 release. Use the same mod version on all participants.

The workflow builds on Linux and Windows. It uses no publishing credentials or repository write permissions. It does not create a GitHub release automatically.

## GitHub workspace / Codespaces

From your repository choose **Code → Codespaces → Create codespace on main**. The included container installs Java 25 and Python, then runs `./gradlew build` automatically. Subsequent builds:

```bash
./gradlew build
```

Outputs are in `build/libs`. Codespaces is suitable for editing, building and dedicated-server development; launch the graphical Minecraft client on a desktop computer. GitHub may charge for Codespaces usage according to your account plan.

## Local development

Install a **JDK 25** and Python 3, then run:

```bash
# Linux/macOS
chmod +x gradlew
./gradlew build
./gradlew runClient
```

```powershell
# Windows PowerShell
.\gradlew.bat build
.\gradlew.bat runClient
```

You do not need to install Gradle separately: the wrapper downloads the pinned version. For a development server use `./gradlew runServer`; review the Minecraft EULA yourself and edit the generated `run/eula.txt` if you agree. Server/client development uses ordinary Fabric Loom tasks.

## Implemented features

| Tier | Bow skills | Bow bonus | Bow draw | Crossbow bonus | Crossbow load | Magazine | Ranged durability |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Stone | Vanilla behavior | 0% | 20 ticks | 0% | 25 ticks | 1 volley | 448 |
| Copper | Zoom, Target Lock | 3% | 20 ticks | 5% | 25 ticks | 1 volley | 512 |
| Iron | Zoom, Target Lock, Dead Eye | 6% | 20 ticks | 10% | 25 ticks | 3 volleys | 768 |
| Gold | Zoom, Target Lock | 0% | 10 ticks | 15% | 16 ticks | 4 volleys | 48 |
| Diamond | Zoom, Target Lock, Dead Eye | 9% | 26 ticks | 20% | 32 ticks | 5 volleys | 1561 |
| Netherite | All three skills | 12% | 30 ticks | 25% | 40 ticks | 6 volleys | 2031 |

Use vanilla bows and crossbows as the wooden starting weapons; there are no duplicate custom wooden versions. Existing saved custom wooden weapons convert to vanilla equivalents while preserving count and component patches. Stone versions retain vanilla firing behavior, have 448 durability and repair with stone-tool materials. Craft one vanilla bow/crossbow with three cobblestone to get the matching stone weapon. Copper and higher upgrades can start with a vanilla or stone bow/crossbow.

Gold ranged weapons trade durability for speed: the bow charges in half a second, and both bow/crossbow have only 48 durability. Diamond and netherite trade longer draw/load times for their skills, ammunition support, magazine capacity and durability. Native Quick Charge still reduces crossbow load time, with a three-tick minimum on custom crossbows. Their charge animations use the same durations. Crossbow damage and magazine sizes remain unchanged.

Copper through netherite crossbows have Zoom; netherite additionally has Target Lock and Dead Eye. A magazine fires **one volley per click**, with a six-tick firing cooldown. Vanilla Multishot still determines arrows per volley; each extra volley loads and pays for another ammo unit. Insufficient ammo produces a partially filled magazine. Creative/infinite projectiles remain non-pickup copies.

- Double Jump II also raises the first ground-jump impulse by 25%; level I retains a normal first jump. Native Jump Boost effects remain part of the starting impulse. Speed remains mutually exclusive.
- Thrown Sharpness adds an extra vanilla-sized Sharpness bonus, in addition to native enchantment damage: Sharpness V contributes 6 damage total (3 native + 3 extra) before armor.
- Hold any item, including a mace or block, and run `/enchant @s pvpoverhaul:throwable 1` with ordinary command permission. Sneak-use throws one item and uses its actual item model; blocks have 4 base thrown damage, weapons use their attack attributes. Normal enchanting tables/anvils retain weapon restrictions. Axes/battleaxes tumble once per 10 ticks in flight; thrown spears are 35% larger.
- **47 added items:** six materials of bows and crossbows (stone, copper, iron, gold, diamond and netherite), seven materials of knives and battleaxes, six shield tiers, twelve custom arrows, Dragon Scales, Dragon Scale Wolf Armor and a Quiver. Spears use the seven existing Minecraft spear items, including stone; the mod registers no additional spear items.
- Knives: seven materials (wood, stone, copper, iron, gold, diamond, netherite), 3–8 base damage, 3.2 attack speed, one block less interaction range. Sneaking **or** invisibility doubles final melee damage, including vanilla charge/critical/enchantment calculations. Right-click throws a knife without an enchantment.
- Spears: the original Minecraft items, models, crafting recipes and kinetic/piercing stats are retained, including native Lunge. Add Throwable for sneak-right-click throwing or Pole Jump for a ground vault. Throwable is incompatible with Lunge and Pole Jump; Pole Jump can coexist with Lunge. While grounded and looking down at least 55 degrees, using native Lunge with both enchantments replaces its forward impulse with a Pole Jump. Native Lunge costs/requirements remain in effect; it shares the two-second Pole Jump cooldown with G. Old v0.1 mod spear IDs decode as the matching native spears, retaining saved count and component patches; subsequent saves use the native IDs.
- Battleaxes: increased melee damage, 0.8 attack speed, vanilla-style five-second shield disable before shield-tier scaling; Sharpness plus new Cleaving and Impact options. Cleaving shares the vanilla damage-enchantment exclusion set.
- Shields: iron and gold resist the first qualifying stun hit and disable on the second; diamond disables on the third, netherite on the fourth. Wood/copper and vanilla shields disable normally on one qualifying hit. Only shield-disable attempts (such as axe/battleaxe or blunt-arrow hits) count, not ordinary sword hits. Progress resets after eight seconds without a qualifying hit or on switching shields; normal shield damage remains. Existing tier recovery scales apply after the final hit.
- Tridents: craftable with prismarine shards and sticks; +1 main-hand entity interaction range supplied as a normal item attribute. Depth Strider boots allow an enchanted Riptide trident to activate in dry conditions. You still need Riptide on the trident.
- Axes/battleaxes: Throwable enables sneak-right-click throwing. Loyalty also supports thrown knives, axes and spears.
- Boots: Speed I–III grants +3%/+6%/+9% movement speed. Speed and Double Jump are mutually exclusive mobility choices; existing boots carrying both retain Speed and cannot activate Double Jump. Depth Strider and Soul Speed stay compatible with either. Double Jump I–II still preserves accumulated fall distance.
- Elytra: Boost I–III now has 0.54/0.72/0.90 impulses (50% stronger than v0.8.4), 8/7/6-second cooldowns, base three durability cost and 0.6 exhaustion. Its added impulse is capped at 2.4 blocks/tick without truncating existing momentum. Aerodynamics now improves passive gliding instead of Boost: I/II reduces gliding gravity by 10%/20% and increases horizontal velocity retention from 0.99 to 0.9925/0.995. It runs on both client and server and still loses height during level flight. It does not change normal falling, erase fall distance, or grant unlimited flight.
- Overload I: press O while gliding for a 1.5 directional impulse, capped at a resulting 3.0 blocks/tick. It costs **half the Elytra's maximum durability, rounded up**, on every use (216 for a normal 432-durability Elytra). Unbreaking does not reduce that exact cost; activation is refused if the Elytra would stop being usable. Overload has a 20-second cooldown shared with Boost, so they cannot be chained to skip cooldowns. Existing above-cap rocket/Riptide momentum is preserved. Boost and Overload can be enchanted on the same Elytra and have separate keys. All nine new enchantments are included in enchanting/trades/loot and `/enchant`.

The stone knife uses cobblestone above a stick in the crafting grid. It has 4 base damage, 3.2 attack speed, one block less interaction range and 131 durability, with the same stealth bonus, throwing and Loyalty support as the other knives.

### Controls

| Default key | Action |
| --- | --- |
| Hold Z | Zoom with a mod bow/crossbow in the main hand |
| R | One charged shot within three seconds; visible target within 24 blocks and roughly ten degrees of your crosshair; 16-second cooldown |
| V | One charged shot within three seconds; 40% less random spread and +5% projectile base damage; 20-second cooldown |
| B | Elytra Boost; 0.54/0.72/0.90 impulse and 8/7/6-second cooldown by level |
| O | Elytra Overload; half maximum durability, 20-second shared flight cooldown |
| C | Open the quiver's arrow selection wheel; click or use 1–9 to select |
| Shift+C | Open arrow storage directly for a carried quiver |
| G | Pole Jump from the ground; 2-second cooldown |
| Press Jump again in the air | Double Jump with enchanted boots |
| Right-click a knife | Throw |
| Sneak-right-click a Throwable axe/spear | Throw |

Rebind these in Minecraft Controls. R toggles a visible target lock with a glowing outline and camera tracking; sneak releases. V fires at the three closest visible enemies in a rapid burst. See COMBAT-UPDATE.md for ammo rules, cooldowns, textures and the new untiered slingshot.

### Ammunition

Wood arrows and ordinary vanilla arrows work in every mod bow/crossbow. Stone and flint custom arrows are removed; old saved stacks convert to vanilla arrows, preserving count and component patches. Copper unlocks copper arrows; iron unlocks iron and blunt arrows; gold unlocks gold and spectral arrows. Gold deliberately does not accept iron/blunt arrows. Diamond additionally unlocks Dragon Scale, blaze, breeze, firework and fire charge arrows. Netherite accepts all ammunition. Vanilla normal and tipped arrows continue to work.

Arrow spread values (lower is more accurate): wood 1.8, copper 0.8, iron 0.6, gold 0.5, Dragon Scale 0.2, netherite 0.05. Damage-effect special arrows use 0.7–0.9; grappling arrows use 1.0. Custom arrow material accuracy also applies when a vanilla bow/crossbow fires that arrow.

Blunt arrows disable a frontal blocking shield for a base two seconds after resolving its block. Blaze arrows burn for eight seconds on a successful hit; fire-charge arrows burn for four seconds, and breeze arrows knock back. Firework arrows are consumed on the first entity or block impact and create a **vanilla one-star firework explosion** on the next server tick. It replaces the ordinary arrow hit, dealing up to 7 damage before armor with vanilla distance falloff over 5 blocks and sight checks. It has normal rocket particles/sound and shooter attribution, can harm the shooter at close range, and does not destroy blocks. Piercing cannot make one arrow explode repeatedly. Bow damage bonuses and Power do not multiply this native explosion damage. Actual vanilla firework rockets remain supported in the offhand for diamond/netherite crossbows.

### Crafting and fletching

After a build or regeneration, recipes are in `src/main/resources/data/pvpoverhaul/recipe`. Convert a vanilla bow/crossbow to its wooden tier using a one-item crafting recipe. Add three copper/iron/gold ingots or diamonds to a vanilla bow/crossbow for higher tiers. Netherite crafting uses the usual upgrade template, diamond-tier item and netherite ingot at a smithing table.

A fletching table supports both arrow crafting and the existing **two-hand upgrade interaction**. Right-click it without a valid upgrade/waxing setup to open a synchronized 3×3 crafting grid. Use the same tip-over-stick-over-feather recipes as at a crafting table, making four custom arrows per craft. Vanilla normal, spectral and tipped-arrow recipes also work. Only arrow results are allowed; unused ingredients return normally when closing the menu. The recipe book uses the normal crafting interface and may show other recipes whose results are blocked at this workstation.

1. Put the bow/crossbow to upgrade in your **offhand**.
2. Hold at least three ingots/diamonds (or one netherite ingot) in the **main hand**.
3. Right-click the fletching table. It upgrades to that material if its rank is higher; this route preserves enchantments, names, current damage and loaded ammunition. The netherite fletching upgrade does not require a template.
4. Hold honeycomb instead to wax a copper bow/crossbow, increasing maximum durability by 50%. Waxing only works once.

Dragon Scales can now be found in **End City treasure chests**, including the ship's treasure chests: a separate 35% chance for 1–3 scales, keeping the original loot. Previously opened chests are unchanged. The existing Dragon Breath + Echo Shard + Popped Chorus Fruit crafting recipe remains available. Each arrow recipe makes four arrows using its tip material, a stick and a feather.

## Compatibility and known limits

The code avoids replacing `Player.attack`, global cooldowns, critical-hit rules, mace attacks, enchantment processing and swap handling. Knife damage changes only the final damage argument when a knife is in the main hand. Trident reach is an ordinary main-hand attribute. **Attribute swapping, stun slams and other techniques need in-game regression testing before compatibility can be promised.** This is a v0.2 prototype; multiplayer balance still needs gameplay testing.

Important current limits:

- The supplied sprites provide all seven battleaxes, all seven knives and all twelve arrow textures, including your supplied grappling-arrow icon. All sixty supplied stone/copper/iron/gold/diamond/netherite bow and crossbow sprites are used: idle, three draw stages, and the crossbow arrow/firework loaded states. Blunt uses the former flint-arrow texture; blaze and fire-charge textures are swapped. For local editing, persistent files in `scripts/art/textures` are copied byte-for-byte into the mod. The browser kit packs those identical PNG bytes into `scripts/art/sprites_packed.json`; that file takes precedence over older loose sprites if present. All six supplied shields retain their original geometry and 64×64 textures, with centered grip/display transforms and a further 1.5-model-unit inward hand-display adjustment in both hands and blocking poses. Banner patterns are not drawn; in-hand placement still needs gameplay verification. Native spears retain native visuals. Thrown knives, battleaxes, axes and spears now use their actual item models; spears have an additional 90-degree correction so their native upper-left tips follow flight orientation and preserving item-model overrides and glint. The original pickup stack and Loyalty behavior are retained. Ordinary tridents keep their native renderer.
- Vanilla Loyalty's JSON definition is replaced only to widen its supported-item set. Datapacks/mods that also replace that definition may conflict. Other vanilla enchantment definitions are not overwritten.
- Spectral arrows and vanilla rockets are reused; custom firework arrows create native damaging explosions without destroying blocks. Breeze arrows use a direct knockback effect rather than a wind-charge entity.
- Movement/skill state is session-local and resets on disconnect/server restart. There is no saved progression tree or skill XP system because the original concept did not define one.
- Vanilla spear melee/kinetic stats and recipes are retained. Mod throwing damage is 5/6/6/7/8/9/10 for wood/stone/copper/iron/gold/diamond/netherite. Costs and cooldowns are initial PvP tuning values, not a claim of finished balance.
- The build checker validates internal links, recipe coverage and exclusions. Separate native codec checks parsed all 48 mod recipes and all nine enchantments; actual gameplay is still unverified.

See [TESTING.md](TESTING.md) for the actual validation performed and the gameplay checklist.

## Grappling arrows

Craft **one ordinary arrow + one string + one iron ingot** anywhere in a crafting grid or at the fletching table to get one Grappling Arrow. Vanilla and all mod bows/crossbows can fire it, and the quiver can store/select it. It is a single-use utility arrow with **no direct hit damage**.

In flight it uses Minecraft's actual **fishing-bobber renderer and line**, rather than the regular arrow visual. A block hit anchors the hook at the surface; an entity hit anchors it to that entity and follows its movement. Players, mobs and non-living entities such as boats, minecarts, armor stands and dropped items are eligible. Removed/dead/spectator entities, the shooter and the shooter's vehicle/passengers are excluded. The shooter is pulled toward the anchor; the target is not pulled. Friendly players can be hooked too because only the shooter's motion is changed.

Attachment reach is **48 blocks**, with a **64-block tether limit**, maximum pull speed **1.35 blocks/tick** and up to **100 ticks (five seconds at 20 TPS)** of pulling. **Sneak releases** the line. It also releases when close to the anchor, on death/disconnect, when the target or block is lost, on dimension changes, or on expiry. Normal collisions and fall damage apply; it does not teleport or grant flight abilities. Only one tether is active per shooter; a later successful grapple replaces it. In-flight hooks expire after ten seconds. Attached hooks are discarded on save/reload instead of retaining a permanent floating anchor. Dispenser-fired arrows have the bobber appearance but cannot pull a player owner.

The bobber line starts near the shooter's native rope/hand position. Rendering and actual multiplayer pulling still require the in-game checklist in TESTING.md.

## Dragon Scale wolf armor and quiver

All **47 added items have survival crafting or smithing recipes**. The resource validator requires a recipe for each item; all 48 mod recipes were also parsed by Minecraft's native recipe codec. Enchantments remain available through enchanting, trading and loot; enchanted books are not crafting recipes. Vanilla spears retain their original recipes.

| New item | Ingredients | 3×3 pattern, top to bottom |
| --- | --- | --- |
| Dragon Scale Wolf Armor | 6 Dragon Scales + 1 vanilla wolf armor | `S S` / `SAS` / `S S` (S = scale, A = armor) |
| Quiver | 7 leather + 1 string | `LSL` / `L L` / `LLL` (L = leather, S = string) |

**Wolf armor:** right-click your tamed adult wolf to equip it, after removing any existing armor with shears. Make it stand and remove its leash before taking off. A wolf within 32 blocks follows while you glide with Elytra, steering toward a position behind you. It keeps ordinary terrain collision and cannot fly through walls. It descends toward you when you land, then restores normal gravity. Flight stops if you separate by more than 128 blocks, change dimension, disconnect, remove/break the armor or make the wolf sit. It receives temporary Slow Falling when flight ends in midair. Saving/reloading resets temporary flight gravity. The armor absorbs eligible damage like vanilla wolf armor, has 128 durability and can be repaired with Dragon Scales while the wolf sits. Its appearance uses the supplied eleven-cube Blockbench armor model and the original 64×32 PNG, converted to native animated wolf parts. Walking, head movement, sitting, shaking and tail animation are retained. Native equipment/glint/crack rendering uses the custom mesh only for Dragon Scale armor. No OptiFine installation or wing animation is needed.

**Quiver:** hold it and right-click to open nine arrow-storage slots, or press **Shift+C** while carrying it anywhere in your inventory/offhand. Newly loaded quivers automatically use the first arrow compatible with your weapon until you choose a slot or inventory mode. Store vanilla, tipped, spectral or mod arrows; rockets and other items cannot enter it. Carry it anywhere in your normal inventory, or in the offhand. Press **C** to open an emote-style selection wheel with native clickable buttons; click an arrow slot or press **1–9**, including numpad keys. A compact 3×3 layout is used at smaller GUI sizes. Click the center or press **0** for ordinary inventory ammunition; Escape cancels. Wheel click boxes now match the visible boxes, and selected arrows update immediately. The selected compatible arrow takes priority on future bow shots or crossbow loads. An explicitly selected empty slot stops drawing instead of silently switching to the first arrow; select another slot or inventory mode. An explicitly selected arrow incompatible with your weapon also blocks drawing and shows a warning, instead of silently firing a different arrow. Incompatible slots are marked red when a ranged weapon is held; center/0 restores inventory mode. Already-loaded crossbows retain their ammunition. The quiver you select stays active after switching to your weapon or moving the quiver; selecting another replaces that preference. Holding a different quiver lets you open its own wheel. Server confirmation reports each selection. Arrows and their components, counts and selected slot save inside the quiver when dropped or reloaded. Normal survival ammo consumption, Multishot and native Infinity/creative rules are retained. The source quiver is locked while its storage menu is open.

## Editing the mod

- `Balance.java` and `Tier.java`: movement safety limits, bow/skill tuning, magazine capacities and shield stun scaling.
- `Ammo.java`: tier restrictions and accuracy.
- `Combat.java` and `mixin/`: narrow combat hooks, shield effects and Riptide extension.
- `Skills.java`: server checks, cooldowns and movement abilities.
- `Interactions.java`: throwing, waxing and fletching upgrades.
- `scripts/generate_resources.py` and `scripts/pixel_art.py`: recipes, enchantment data, item tags, models, translations and original pixel-art geometry. `scripts/shield_assets.py` and `scripts/shields/*.bbmodel` supply the custom shields. The build regenerates these files before validation and packaging. Make persistent texture/data changes in these sources, because generated PNG/JSON outputs are overwritten on the next build.
- `FireworkArrows.java` and `mixin/FireworkRocketAccessor.java`: native firework payload and impact detonation timing; ordinary arrows/rockets keep their normal behavior.
- `FletchingMenu.java`: arrow-only workstation using the standard crafting screen; `LootAdditions.java`: additive End City scale loot; `mixin/GlidingMixin.java`: passive Aerodynamics glide physics.
- `scripts/wolf_assets.py` and `scripts/wolf/`: source wolf model/texture and deterministic native-mesh export. `DragonWolfArmorMesh.java` / `client/mixin/WolfArmorLayerMixin.java`: native animated armor rendering. The custom quiver inventory sprite in `scripts/art/textures/quiver.png` uses the supplied leather-quiver reference.
- `Quivers.java`, `QuiverMenu.java`, `QuiverWheelScreen.java`: saved arrow storage, ammo consumption and selection UI. `DragonWolfFlightGoal.java` / `mixin/DragonWolfMixin.java`: owned wolf flight, armor and reload handling.
- `LegacySpears.java` / `mixin/ItemCodecMixin.java`: removed spear IDs migrate to native spears; removed stone/flint-arrow IDs migrate to vanilla arrows. No duplicate registration.
- `GrapplingArrows.java` / `mixin/AbstractArrowMixin.java`: temporary attachment, pull forces, synchronized bobber marker and save/disconnect cleanup. `GrapplingArrowStateMixin.java` / `GrapplingArrowRendererMixin.java`: native fishing-bobber/line rendering.
- `ThrownItemVisual.java` / `mixin/ThrownTridentMixin.java`: synchronized original weapon stacks. `client/mixin/ThrownWeaponRendererMixin.java` selects actual item models for thrown weapons.
- `scripts/obsolete_resources.json`: exact obsolete spear/arrow files excluded from the JAR if a browser upload leaves them in your checkout.
- `src/test/java/dev/pvpoverhaul/BalanceChecks.java`: bow tradeoffs and movement bounds, included in Gradle `check`.

Java changes require rebuilding the JAR. Version pins are in `gradle.properties`. Updating just the Minecraft version number is not a safe port: recheck APIs and run the gameplay checklist. Source and original generated icons are MIT licensed. The supplied shield/wolf models and textures and axe/arrow/knife art are included as user-provided assets; their original licensing is not changed by this project. Vanilla models/textures are referenced from the game and are not bundled.
