# PvP Overhaul — Fabric 26.3

A first playable implementation of the supplied weapon overhaul concept, with a GitHub Actions builder and a Java 25 Codespaces workspace. **Minecraft Java Edition 26.3 only; not Bedrock or older 1.21.x.** Install this mod and Fabric API on the server and every client.

Pinned toolchain: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, Loom 1.17.21, Gradle 9.6.0, Java 25. Minecraft 26.3 is unobfuscated: this project uses Mojang class names directly and intentionally has no Yarn mappings dependency.

## Put this in your new GitHub repository

The fresh kit needs **one project upload**. Extract it, open `UPLOAD-THIS`, and drag everything **inside** that folder (including `src`, `scripts` and `gradle`) into the repository root. Create `.github/workflows/build.yml` in GitHub's file editor using the visible `workflow-build.yml.txt` supplied outside that folder. Follow `START-HERE.txt`. `Complete-project` contains the full source and generated textures/resources for local or git-based use; browser users only need `UPLOAD-THIS` plus the workflow.

The build now runs `generateResources` before validation and packaging. The included Python scripts generate all current PNG textures, models, recipes, tags, enchantments and translations, repairing missing images and old definitions. The browser kit includes Java, scripts, three metadata files and a packed sprite input; it does not require hundreds of individual generated resource uploads.

1. Create an empty GitHub repository, for example `pvp-overhaul`. Leave the “add README” and “add license” options unchecked.
2. Extract the ZIP. Open a terminal **inside the extracted `Complete-project` folder**; `build.gradle` and `.github` must be at the repository root.
3. Run these commands, replacing `YOURNAME` and the repository name:

```bash
git init -b main
git add .
git commit -m "Add PvP Overhaul for Fabric 26.3"
git remote add origin https://github.com/YOURNAME/pvp-overhaul.git
git push -u origin main
```

For browser uploads, follow the single-upload instructions above. Create hidden configuration files directly in GitHub's file editor from the visible templates.

4. Open **Actions → Build Fabric 26.3**. A push starts the build; you can also choose **Run workflow**.
5. Open the completed green run and download `pvp-overhaul-26.3-ubuntu-latest` from **Artifacts**. Extract it. Install **`pvp-overhaul-0.8.4.jar`**, not the `-sources.jar`, in your game's `mods` folder. Update the server and clients to the same mod version.
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

| Tier | Bow skills | Bow damage bonus | Full bow draw | Crossbow damage bonus | Magazine |
| --- | --- | --- | --- | --- | --- |
| Stone | Standard vanilla behavior | 0% | 20 ticks | 0% | 1 volley |
| Copper | Zoom, Target Lock | 3% | 20 ticks | 5% | 1 volley |
| Iron | Zoom, Target Lock, Dead Eye | 6% | 20 ticks | 10% | 3 volleys |
| Gold | Zoom, Target Lock, faster draw | 0% | 18 ticks | 15% | 4 volleys |
| Diamond | Zoom, Target Lock, Dead Eye | 9% | 20 ticks | 20% | 5 volleys |
| Netherite | All three skills | 12% | 20 ticks | 25% | 6 volleys |

Use vanilla bows and crossbows as the wooden starting weapons; there are no duplicate custom wooden versions. Existing saved custom wooden weapons convert to vanilla equivalents while preserving count and component patches. Stone versions retain vanilla firing behavior, have 448 durability and repair with stone-tool materials. Craft one vanilla bow/crossbow with three cobblestone to get the matching stone weapon. Copper and higher upgrades can start with a vanilla or stone bow/crossbow.

Gold trades impact damage and durability for an 18-tick draw. Its unenchanted damage-per-full-draw rate stays below netherite's. Crossbow tier damage and magazines are retained from v0.1. Shield stun-duration scales remain 1.00/0.88/0.76/0.64/0.52/0.40 across the six tiers.

Copper through netherite crossbows have Zoom; netherite additionally has Target Lock and Dead Eye. A magazine fires **one volley per click**, with a six-tick firing cooldown. Vanilla Multishot still determines arrows per volley; each extra volley loads and pays for another ammo unit. Insufficient ammo produces a partially filled magazine. Creative/infinite projectiles remain non-pickup copies.

- **47 added items:** six materials of bows and crossbows (stone, copper, iron, gold, diamond and netherite), seven materials of knives and battleaxes, six shield tiers, twelve custom arrows, Dragon Scales, Dragon Scale Wolf Armor and a Quiver. Spears use the seven existing Minecraft spear items, including stone; the mod registers no additional spear items.
- Knives: seven materials (wood, stone, copper, iron, gold, diamond, netherite), 3–8 base damage, 3.2 attack speed, one block less interaction range. Sneaking **or** invisibility doubles final melee damage, including vanilla charge/critical/enchantment calculations. Right-click throws a knife without an enchantment.
- Spears: the original Minecraft items, models, crafting recipes and kinetic/piercing stats are retained, including native Lunge. Add Throwable for sneak-right-click throwing or Pole Jump for a ground vault. Throwable is incompatible with Lunge and Pole Jump; Pole Jump can coexist with Lunge. Old v0.1 mod spear IDs decode as the matching native spears, retaining saved count and component patches; subsequent saves use the native IDs.
- Battleaxes: increased melee damage, 0.8 attack speed, vanilla-style five-second shield disable before shield-tier scaling; Sharpness plus new Cleaving and Impact options. Cleaving shares the vanilla damage-enchantment exclusion set.
- Shields: tiered durability and progressively shorter stun recovery using vanilla's `blocks_attacks` component. High tiers still get stunned; this implementation interprets “harder to stun” as a shorter duration, not a random immunity roll.
- Tridents: craftable with prismarine shards and sticks; +1 main-hand entity interaction range supplied as a normal item attribute. Depth Strider boots allow an enchanted Riptide trident to activate in dry conditions. You still need Riptide on the trident.
- Axes/battleaxes: Throwable enables sneak-right-click throwing. Loyalty also supports thrown knives, axes and spears.
- Boots: Speed I–III grants +3%/+6%/+9% movement speed. Speed and Double Jump are mutually exclusive mobility choices; existing boots carrying both retain Speed and cannot activate Double Jump. Depth Strider and Soul Speed stay compatible with either. Double Jump I–II still preserves accumulated fall distance.
- Elytra: Boost I–III now has 20% stronger 0.36/0.48/0.60 impulses, 8/7/6-second cooldowns, base three durability cost and 0.6 exhaustion. Its added impulse is capped at 2.0 blocks/tick without truncating existing momentum. Aerodynamics now improves passive gliding instead of Boost: I/II reduces gliding gravity by 10%/20% and increases horizontal velocity retention from 0.99 to 0.9925/0.995. It runs on both client and server and still loses height during level flight. It does not change normal falling, erase fall distance, or grant unlimited flight.
- Overload I: press O while gliding for a 1.5 directional impulse, capped at a resulting 3.0 blocks/tick. It costs **half the Elytra's maximum durability, rounded up**, on every use (216 for a normal 432-durability Elytra). Unbreaking does not reduce that exact cost; activation is refused if the Elytra would stop being usable. Overload has a 20-second cooldown shared with Boost, so they cannot be chained to skip cooldowns. Existing above-cap rocket/Riptide momentum is preserved. Boost and Overload can be enchanted on the same Elytra and have separate keys. All nine new enchantments are included in enchanting/trades/loot and `/enchant`.

The stone knife uses cobblestone above a stick in the crafting grid. It has 4 base damage, 3.2 attack speed, one block less interaction range and 131 durability, with the same stealth bonus, throwing and Loyalty support as the other knives.

### Controls

| Default key | Action |
| --- | --- |
| Hold Z | Zoom with a mod bow/crossbow in the main hand |
| R | One charged shot within three seconds; visible target within 24 blocks and roughly ten degrees of your crosshair; 16-second cooldown |
| V | One charged shot within three seconds; 40% less random spread and +5% projectile base damage; 20-second cooldown |
| B | Elytra Boost; 20% stronger impulse and 8/7/6-second cooldown by level |
| O | Elytra Overload; half maximum durability, 20-second shared flight cooldown |
| C | Open the quiver's arrow selection wheel; click or use 1–9 to select |
| Shift+C | Open arrow storage directly for a carried quiver |
| G | Pole Jump from the ground; 2-second cooldown |
| Press Jump again in the air | Double Jump with enchanted boots |
| Right-click a knife | Throw |
| Sneak-right-click a Throwable axe/spear | Throw |

Rebind these in Minecraft's controls and resolve conflicts with other mods. Tooltips show the currently bound skill keys. Hold a supported weapon in either hand, press R or V, then fully draw and fire the bow within three seconds (or fire a loaded netherite crossbow). Target Lock needs a visible target near the crosshair; it aims at the target's body and rechecks range, visibility, teammates and aim at release. Dead Eye needs no target. Skills bind to the armed weapon type, and a charged shot consumes them. R and V can now be armed in the same tick. The action bar explains unsupported items, cooldowns, low Elytra durability, gliding requirements, and speed caps. The client reports a missing server installation. Server movement skills explicitly send the owner's motion packet. These fixes address hand selection, dropped simultaneous activations, aim overshoot and missing feedback; multiplayer gameplay still needs testing. Partial bow shots receive neither Dead Eye bonuses nor Target Lock correction. Arrows keep normal gravity and collision.

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

In flight it uses Minecraft's actual **fishing-bobber renderer and line**, rather than the regular arrow visual. A block hit anchors the hook at the surface; a player hit anchors it to that player's body and follows their movement. The shooter is pulled toward the anchor; the target is not pulled. Friendly players can be hooked too because only the shooter's motion is changed.

Attachment reach is **48 blocks**, with a **64-block tether limit**, maximum pull speed **1.35 blocks/tick** and up to **100 ticks (five seconds at 20 TPS)** of pulling. **Sneak releases** the line. It also releases when close to the anchor, on death/disconnect, when the target or block is lost, on dimension changes, or on expiry. Normal collisions and fall damage apply; it does not teleport or grant flight abilities. Only one tether is active per shooter; a later successful grapple replaces it. In-flight hooks expire after ten seconds. Attached hooks are discarded on save/reload instead of retaining a permanent floating anchor. Dispenser-fired arrows have the bobber appearance but cannot pull a player owner.

The bobber line starts near the shooter's native rope/hand position. Rendering and actual multiplayer pulling still require the in-game checklist in TESTING.md.

## Dragon Scale wolf armor and quiver

All **47 added items have survival crafting or smithing recipes**. The resource validator requires a recipe for each item; all 48 mod recipes were also parsed by Minecraft's native recipe codec. Enchantments remain available through enchanting, trading and loot; enchanted books are not crafting recipes. Vanilla spears retain their original recipes.

| New item | Ingredients | 3×3 pattern, top to bottom |
| --- | --- | --- |
| Dragon Scale Wolf Armor | 6 Dragon Scales + 1 vanilla wolf armor | `S S` / `SAS` / `S S` (S = scale, A = armor) |
| Quiver | 7 leather + 1 string | `LSL` / `L L` / `LLL` (L = leather, S = string) |

**Wolf armor:** right-click your tamed adult wolf to equip it, after removing any existing armor with shears. Make it stand and remove its leash before taking off. A wolf within 32 blocks follows while you glide with Elytra, steering toward a position behind you. It keeps ordinary terrain collision and cannot fly through walls. It descends toward you when you land, then restores normal gravity. Flight stops if you separate by more than 128 blocks, change dimension, disconnect, remove/break the armor or make the wolf sit. It receives temporary Slow Falling when flight ends in midair. Saving/reloading resets temporary flight gravity. The armor absorbs eligible damage like vanilla wolf armor, has 128 durability and can be repaired with Dragon Scales while the wolf sits. Its appearance uses the supplied eleven-cube Blockbench armor model and the original 64×32 PNG, converted to native animated wolf parts. Walking, head movement, sitting, shaking and tail animation are retained. Native equipment/glint/crack rendering uses the custom mesh only for Dragon Scale armor. No OptiFine installation or wing animation is needed.

**Quiver:** hold it and right-click to open nine arrow-storage slots, or press **Shift+C** while carrying it anywhere in your inventory/offhand. Newly loaded quivers automatically use the first arrow compatible with your weapon until you choose a slot or inventory mode. Store vanilla, tipped, spectral or mod arrows; rockets and other items cannot enter it. Carry it anywhere in your normal inventory, or in the offhand. Press **C** to open an emote-style radial wheel; click an arrow slot or press **1–9**. Click the center or press **0** for ordinary inventory ammunition; Escape cancels. Wheel click boxes now match the visible boxes, and selected arrows update immediately. The selected compatible arrow takes priority on future bow shots or crossbow loads. Empty selections fall back to normal inventory ammo. An explicitly selected arrow incompatible with your weapon blocks drawing and shows a warning, instead of silently firing a different arrow. Incompatible slots are marked red when a ranged weapon is held; center/0 restores inventory mode. Already-loaded crossbows retain their ammunition. The quiver you select stays active after switching to your weapon or moving the quiver; selecting another replaces that preference. Holding a different quiver lets you open its own wheel. Server confirmation reports each selection. Arrows and their components, counts and selected slot save inside the quiver when dropped or reloaded. Normal survival ammo consumption, Multishot and native Infinity/creative rules are retained. The source quiver is locked while its storage menu is open.

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
