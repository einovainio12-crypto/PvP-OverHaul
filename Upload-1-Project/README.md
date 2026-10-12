# PvP Overhaul — Fabric 26.3

A first playable implementation of the supplied weapon overhaul concept, with a GitHub Actions builder and a Java 25 Codespaces workspace. **Minecraft Java Edition 26.3 only; not Bedrock or older 1.21.x.** Install this mod and Fabric API on the server and every client.

Pinned toolchain: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, Loom 1.17.21, Gradle 9.6.0, Java 25. Minecraft 26.3 is unobfuscated: this project uses Mojang class names directly and intentionally has no Yarn mappings dependency.

## Put this in your new GitHub repository

The current browser kit needs **one project upload**. Extract it, open `Upload-1-Project`, and drag everything **inside** that folder (including `src`, `scripts` and `gradle`) into the repository root. Keep the existing workflow, or create `.github/workflows/build.yml` from the kit's `workflow-build.yml.txt` if it is missing.

The build now runs `generateResources` before validation and packaging. The included Python scripts generate all current PNG textures, models, recipes, tags, enchantments and translations, repairing missing images and old definitions. The browser kit therefore only needs Java source plus the three mod metadata files; it does not require hundreds of individual generated resource uploads.

1. Create an empty GitHub repository, for example `pvp-overhaul`. Leave the “add README” and “add license” options unchecked.
2. Extract the ZIP. Open a terminal **inside the extracted `pvp-overhaul` folder**; `build.gradle` and `.github` must be at the repository root.
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
5. Open the completed green run and download `pvp-overhaul-26.3-ubuntu-latest` from **Artifacts**. Extract it. Install **`pvp-overhaul-0.2.0.jar`**, not the `-sources.jar`, in your game's `mods` folder.
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
| Wood | Zoom | 0% | 20 ticks | 0% | 1 volley |
| Copper | Zoom, Target Lock | 3% | 20 ticks | 5% | 1 volley |
| Iron | Zoom, Target Lock, Dead Eye | 6% | 20 ticks | 10% | 3 volleys |
| Gold | Zoom, Target Lock, faster draw | 0% | 18 ticks | 15% | 4 volleys |
| Diamond | Zoom, Target Lock, Dead Eye | 9% | 20 ticks | 20% | 5 volleys |
| Netherite | All three skills | 12% | 20 ticks | 25% | 6 volleys |

Gold trades impact damage and durability for an 18-tick draw. Its unenchanted damage-per-full-draw rate stays below netherite's. Crossbow tier damage and magazines are retained from v0.1. Shield stun-duration scales remain 1.00/0.88/0.76/0.64/0.52/0.40 across the six tiers.

Crossbows have Zoom at every tier; netherite additionally has Target Lock and Dead Eye. A magazine fires **one volley per click**, with a six-tick firing cooldown. Vanilla Multishot still determines arrows per volley; each extra volley loads and pays for another ammo unit. Insufficient ammo produces a partially filled magazine. Creative/infinite projectiles remain non-pickup copies.

- **44 added items:** six tiers each of bows, crossbows, knives, battleaxes and shields, thirteen custom arrows, and a Dragon Scale ingredient. Spears use the seven existing Minecraft spear items, including stone; the mod registers no additional spear items.
- Knives: 3–8 base damage, 3.2 attack speed, one block less interaction range. Sneaking **or** invisibility doubles final melee damage, including vanilla charge/critical/enchantment calculations. Right-click throws a knife without an enchantment.
- Spears: the original Minecraft items, models, crafting recipes and kinetic/piercing stats are retained, including native Lunge. Add Throwable for sneak-right-click throwing or Pole Jump for a ground vault. Throwable is incompatible with Lunge and Pole Jump; Pole Jump can coexist with Lunge. Old v0.1 mod spear IDs decode as the matching native spears, retaining saved count and component patches; subsequent saves use the native IDs.
- Battleaxes: increased melee damage, 0.8 attack speed, vanilla-style five-second shield disable before shield-tier scaling; Sharpness plus new Cleaving and Impact options. Cleaving shares the vanilla damage-enchantment exclusion set.
- Shields: tiered durability and progressively shorter stun recovery using vanilla's `blocks_attacks` component. High tiers still get stunned; this implementation interprets “harder to stun” as a shorter duration, not a random immunity roll.
- Tridents: craftable with prismarine shards and sticks; +1 main-hand entity interaction range supplied as a normal item attribute. Depth Strider boots allow an enchanted Riptide trident to activate in dry conditions. You still need Riptide on the trident.
- Axes/battleaxes: Throwable enables sneak-right-click throwing. Loyalty also supports thrown knives, axes and spears.
- Boots: Speed I–III grants +3%/+6%/+9% movement speed. Speed and Double Jump are mutually exclusive mobility choices; existing boots carrying both retain Speed and cannot activate Double Jump. Depth Strider and Soul Speed stay compatible with either. Double Jump I–II still preserves accumulated fall distance.
- Elytra: Boost I–III applies a 0.30/0.40/0.50 directional impulse with an 8/7/6-second cooldown, a base three durability cost (Unbreaking applies normally), and 0.6 hunger exhaustion. Aerodynamics adds 0.025 impulse and removes 0.25 seconds of cooldown per level, capped at level II. Boost adds no speed above 1.8 blocks/tick; it never truncates existing rocket/Riptide momentum. It requires gliding, usable elytra durability, and no vehicle or creative flight. All eight new enchantments remain available through enchanting/trades/loot and `/enchant`. Boost and Speed are less common enchanting-table choices than before.

### Controls

| Default key | Action |
| --- | --- |
| Hold Z | Zoom with a mod bow/crossbow in the main hand |
| R | One charged shot within three seconds; visible target within 24 blocks and roughly ten degrees of your crosshair; 16-second cooldown |
| V | One charged shot within three seconds; 40% less random spread and +5% projectile base damage; 20-second cooldown |
| B | Elytra Boost; 8/7/6-second cooldown by level, slightly reduced by Aerodynamics |
| G | Pole Jump from the ground; 2-second cooldown |
| Press Jump again in the air | Double Jump with enchanted boots |
| Right-click a knife | Throw |
| Sneak-right-click a Throwable axe/spear | Throw |

Rebind these in Minecraft's controls. The server validates enchantments, movement state, range, line of sight and cooldowns; packet contents cannot set damage or arbitrary velocity. Skill cooldowns are per player during the session. Target Lock only assists a fully drawn bow shot (or netherite crossbow volley), rechecks line of sight and the aiming cone at release, and is consumed by that charged shot. Dead Eye is also consumed by one charged shot/volley. Partial bow shots receive neither the Dead Eye damage/spread bonus nor aim correction. Arrows keep normal gravity and collision.

### Ammunition

Wood/stone/flint arrows work in every mod bow/crossbow. Copper unlocks copper arrows; iron unlocks iron and blunt arrows; gold unlocks gold and spectral arrows. Gold deliberately does not accept iron/blunt arrows. Diamond additionally unlocks Dragon Scale, blaze, breeze, firework and fire charge arrows. Netherite accepts all ammunition. Vanilla normal and tipped arrows continue to work.

Arrow spread values (lower is more accurate): wood 1.8, stone 1.5, flint 1.0, copper 0.8, iron 0.6, gold 0.5, Dragon Scale 0.2, netherite 0.05. Special arrows use 0.7–0.9. Custom arrow material accuracy also applies when a vanilla bow/crossbow fires that arrow.

Blunt arrows disable a frontal blocking shield for a base two seconds after resolving its block; shield tier scales that time. Blaze/fire charge arrows ignite on a successful hit, breeze arrows knock back, and firework arrows **mark with Glowing for five seconds**. Firework arrows are a safe starter effect rather than explosions. Actual vanilla firework rockets remain supported in the offhand for diamond/netherite crossbows.

### Crafting and fletching

After a build or regeneration, recipes are in `src/main/resources/data/pvpoverhaul/recipe`. Convert a vanilla bow/crossbow to its wooden tier using a one-item crafting recipe. Add three copper/iron/gold ingots or diamonds to a vanilla bow/crossbow for higher tiers. Netherite crafting uses the usual upgrade template, diamond-tier item and netherite ingot at a smithing table.

A fletching table also supports a simple **two-hand upgrade interaction**, with no new GUI:

1. Put the bow/crossbow to upgrade in your **offhand**.
2. Hold at least three ingots/diamonds (or one netherite ingot) in the **main hand**.
3. Right-click the fletching table. It upgrades to that material if its rank is higher; this route preserves enchantments, names, current damage and loaded ammunition. The netherite fletching upgrade does not require a template.
4. Hold honeycomb instead to wax a copper bow/crossbow, increasing maximum durability by 50%. Waxing only works once.

Craft Dragon Scale from Dragon Breath + Echo Shard + Popped Chorus Fruit. This is a placeholder survival acquisition recipe, not a new dragon drop. Each arrow recipe makes four arrows using its tip material, a stick and a feather.

## Compatibility and known limits

The code avoids replacing `Player.attack`, global cooldowns, critical-hit rules, mace attacks, enchantment processing and swap handling. Knife damage changes only the final damage argument when a knife is in the main hand. Trident reach is an ordinary main-hand attribute. **Attribute swapping, stun slams and other techniques need in-game regression testing before compatibility can be promised.** This is a v0.2 prototype; multiplayer balance still needs gameplay testing.

Important current limits:

- There are **92 original, editable 16×16 placeholder textures**: material-specific bows and crossbows with pulling/loaded states, knives, battleaxes, arrows, shield inventory icons, and Dragon Scale. They use weapon silhouettes and vanilla-style small palettes. Held shields still use the vanilla 3D renderer; native spears keep native visuals. Custom thrown weapons still use the vanilla trident flying model and return their original item stack.
- Vanilla Loyalty's JSON definition is replaced only to widen its supported-item set. Datapacks/mods that also replace that definition may conflict. Other vanilla enchantment definitions are not overwritten.
- Spectral arrows and vanilla rockets are reused; custom firework arrows do not create fireworks or damage blocks. Breeze arrows use a direct knockback effect rather than a wind-charge entity.
- Movement/skill state is session-local and resets on disconnect/server restart. There is no saved progression tree or skill XP system because the original concept did not define one.
- Vanilla spear melee/kinetic stats and recipes are retained. Mod throwing damage is 5/6/6/7/8/9/10 for wood/stone/copper/iron/gold/diamond/netherite. Costs and cooldowns are initial PvP tuning values, not a claim of finished balance.
- The resource checker validates internal structure and references, not Minecraft's full runtime datapack codecs.

See [TESTING.md](TESTING.md) for the actual validation performed and the gameplay checklist.

## Editing the mod

- `Balance.java` and `Tier.java`: movement safety limits, bow/skill tuning, magazine capacities and shield stun scaling.
- `Ammo.java`: tier restrictions and accuracy.
- `Combat.java` and `mixin/`: narrow combat hooks, shield effects and Riptide extension.
- `Skills.java`: server checks, cooldowns and movement abilities.
- `Interactions.java`: throwing, waxing and fletching upgrades.
- `scripts/generate_resources.py` and `scripts/pixel_art.py`: recipes, enchantment data, item tags, models, translations and original pixel-art geometry. The build regenerates these files before validation and packaging. Make persistent texture/data changes in these scripts, because edits to their generated PNG/JSON outputs are overwritten on the next build.
- `LegacySpears.java` / `mixin/ItemCodecMixin.java`: v0.1 saved-spear ID conversion without duplicate registration.
- `scripts/obsolete_resources.json`: exact old spear files excluded from the JAR if a browser upload leaves them in your checkout.
- `src/test/java/dev/pvpoverhaul/BalanceChecks.java`: bow tradeoffs and movement bounds, included in Gradle `check`.

Java changes require rebuilding the JAR. Version pins are in `gradle.properties`. Updating just the Minecraft version number is not a safe port: recheck APIs and run the gameplay checklist. Source and original generated icons are MIT licensed; vanilla models/textures are referenced from the game and are not bundled.
