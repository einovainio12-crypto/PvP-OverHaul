# Validation and gameplay checklist

## Validation performed in the creation environment

- All main and client Java sources compiled with a Java 25 JDK against the **actual Minecraft 26.3 client JAR**, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3 and the game's dependency libraries. Compilation used `javac --release 25 -proc:none`; this establishes API/type correctness, not gameplay correctness or Mixin application.
- `python3 scripts/validate_resources.py` passed: 232 JSON resources, 47 added items, internal model/texture links, recipe symbol/result consistency, entrypoint/mixin source coverage and enchantment exclusions.
- Minecraft 26.3's actual client loaders parsed all twelve exported normal/blocking shield models and six conditional item definitions without opening a graphics window. The exported PNGs are byte-identical to the six embedded Blockbench textures; geometry/face UVs are preserved with a constant anchor translation and UV-grid scaling. This does not verify in-hand placement or visual rendering in-game.
- The v0.3 firework Mixins applied when the real Fabric server loaded Arrow and FireworkRocketEntity classes. The firework definition has exactly one colored explosion star. Native rocket implementation supplies damage and visuals; impact explosions still need the gameplay checks below. ItemStack creation is deferred until impact because Minecraft 26.3 binds item components after mod initialization.
- A manually packaged mod loaded under **Fabric Loader 0.19.5 + Fabric API 0.162.0+26.3** on the actual 26.3 dedicated-server classes. `KnotServer --initSettings nogui` exited successfully and logged `PvP Overhaul loaded for Minecraft 26.3`. A temporary test initializer forced all combat, gliding, wolf-flight, projectile-draw and migration targets to load with no Mixin injection failure. It verified six old spear IDs are absent from the registry, decode to native items, retain count/damage/name patches and encode back to native IDs. This checks initialization and Mixin application; it does not create a world. The EULA was not accepted automatically.
- A separate JVM used Minecraft's actual tag and dynamic registry loaders with vanilla resources plus the mod resources. All nine custom enchantments loaded, and Boost, Overload and Aerodynamics support Elytra. The additive Dragon Scale loot pool encoded successfully with the native LootPool codec. This audit stops after checking registries and does not create a world or verify actual chest generation.
- The dependency-free Java checks passed: bow tradeoffs, charge timing, stacked damage bounds, stronger Boost values, cooldown floors, Overload's half-maximum durability cost, enchantment bounds, preservation of above-cap momentum, invalid-motion rejection and 10,000 seeded Boost/Overload vector cases. A simulation using native level-pitch glide equations verifies longer but finite gliding at each Aerodynamics level. These checks are part of Gradle `check`.
- The actual registered stone knife was recognized by shared knife behavior, retained native stone durability and belonged to the runtime knife, durability, sharp-weapon and Loyalty tags. The native Loyalty enchantment accepted it.
- A dedicated headless Fabric audit forced WolfArmorLayer to load from the actual client JAR with the client armor Mixin temporarily enabled for that audit. Both required model-field redirects and the typed submit injection applied. The exported mesh baked all eleven cubes into an AdultWolfModel and produced finite walking/sitting/walking poses. EquipmentClientInfo decoded and resolved its custom texture to an existing file. This checks geometry, animation compatibility and injection application without an OpenGL render; visual placement and resource-pack reload still need in-game verification. Audit classes/configuration are excluded from the deliverable.
- The native thrown-weapon renderer constructor, render-state, extraction and submission Mixins applied in the headless client-class audit. The synchronized-item interface applied to ThrownTrident; its ITEM_STACK network codec retained item identity and custom components for stone knives/battleaxes and native axes/spears. Quiver tests verified automatic compatible-arrow selection, explicit inventory mode and explicit incompatible-selection detection. Removed stone/flint arrow stacks decoded to native arrows with counts/names intact. These checks do not render pixels or simulate full projectile flight.
- Grappling ammo was registered, accepted by native and all tier projectile predicates, and selected/consumed through the quiver. 10,000 seeded native Vec3 force vectors stayed finite and within the 1.35 speed cap; invalid-motion rejection passed. Common grappling flags and client bobber state/renderer/culling hooks applied in the headless client-class audit. These tests do not create a world, attach a real player or render pixels.
- Native ItemStack codec round-trips preserved the quiver's sparse nine slots, selection, counts and named tipped-arrow components. Consumption checks accepted exactly-one and zero-use changes and rejected stale replay/count increases. Actual armor components were bound and verified (128 durability, BODY slot). All 48 recipes parsed with the actual Recipe codec. These checks do not simulate a player shooting or a wolf flying.
- A fixture reproduced the reported eleven missing wood textures, stale spear enchantment tags, and old Speed/Double Jump definitions. It failed validation with the same three error types, then generation restored current resources and validation passed. A second fixture containing Java, scripts and the three mod metadata files regenerated and validated the full set.
- Resource generation is reproducible from the included Python source and uses no third-party Python dependencies.
- Minecraft 26.3's official manifest reports Java 25. The Gradle wrapper distribution is pinned to 9.6.0 with its SHA-256 checksum.
- The supplied GitHub Actions log confirmed main/client compilation, JAR assembly and standalone balance checks passed, then exposed Gradle 9's no-discovered-tests failure. The build script now permits zero framework tests while keeping `balanceChecks` and resource validation mandatory. Its syntax was parsed with the Gradle-bundled Groovy compiler, and the property was verified against Gradle 9.6.0's `AbstractTestTask` API. The amended full GitHub build still needs a rerun.
- Full Gradle execution is **not verified in this environment**: Gradle exits during filesystem initialization with `java.io.IOException: Mount point not found`, before evaluating the project. GitHub Actions is provided to run the normal Loom build on Linux and Windows. An included workflow is not evidence that either GitHub run has already passed.

## Required before a public PvP release

Use a disposable test world with two clients and a dedicated Fabric 26.3 server. Build with `./gradlew build`, install the resulting JAR plus Fabric API, and watch client/server logs for Mixin failures and datapack/model loading errors. This is v0.4; multiplayer balance and complete technique compatibility have not been verified.

### Combat regression

- Confirm vanilla sword, axe, bow, crossbow and mace cooldown/critical behavior against an unmodded 26.3 baseline.
- Reproduce your exact attribute-swap combos, shield stun/slam chains, spear Lunge tricks, Riptide interactions, and mace smash setups. Verify damage, knockback, charge, reach and timing.
- Compare +1 trident interaction-range swaps and short-range knife swaps. Ordinary attribute components are used, but packet timing behavior must be tested with your real technique.
- Knife sneak/invisibility multiplier should apply once, not twice when both conditions are present, and must retain cooldown damage scaling.

### Grappling arrows (new; in-game testing required)

- Craft using arrow + string + iron ingot at both tables. Fire from vanilla and all mod bows/crossbows and from the quiver; verify actual ammo consumption, Multishot/Infinity/creative rules and magazine loads.
- Confirm the native bobber sprite and line in first/third person, other clients, both hands, while gliding and when the hook leaves the camera frustum. Ordinary/tipped/spectral arrows and actual fishing must retain native behavior.
- Hit walls/floors/ceilings within 48 blocks and reject farther hits. Hit stationary/moving/friendly players: shooter moves toward the target, not vice versa, without direct arrow damage. Living and non-living entity hits can anchor a pull; dispenser shots without a player owner must not create a pull.
- Verify gradual bounded motion, normal collisions, no teleports or flight ability changes, fall damage, arrival, Sneak release, five-second timeout and 64-block separation cutoff. Test water, Elytra/rockets/Boost, obstacles, shields, moving targets, mounted players and high latency.
- Replace an existing grapple; only one pull is active. Verify block breaking, owner/target death/disconnect, dimension changes and server restart. Attached saved hooks must discard on reload, unattached hooks must expire after ten seconds, and no duplicate arrow pickups should appear.

### Wolf flight and quiver (new; gameplay testing required)

- Craft every added item in survival. Confirm all ingredients are consumed; test smithing and fletching arrow recipes too.
- Equip owned adult wolves; reject babies/other owners and full BODY slots. Test armor absorption, breakage, shearing and sitting-scale repair.
- Fly with a standing wolf within 32 blocks. Test turns, rockets/Boost/Overload, obstacles, landing, sitting, leashing, armor removal/breakage, owner death/disconnect, dimension travel and the 128-block separation cutoff. Wolves must retain collision, descend safely and never keep temporary NoGravity after save/reload. Inspect the supplied custom armor mesh and original texture while standing, walking, sitting, shaking and flying; check damage cracks and glint. Ordinary wolf armor must keep its native appearance.
- Right-click main/offhand quivers and insert all arrow types, including potion/name/enchantment components. Reject rockets, other items and nested quivers. Test shift-click, hotbar/offhand swaps, drag, quick-craft, source-slot protection, disconnect, death/drop and reload. No arrow duplication/loss.
- Test newly loaded quiver automatic ammo selection and Shift+C storage access. Test C wheel with mouse and 1–9, 0 inventory fallback, Escape, different UI scales, key rebinding and multiple quivers. The game does not pause. Verify selecting future ammunition preserves already-charged crossbows.
- Shoot vanilla/mod bows and crossbows with selected arrows, empty slots, incompatible tiers, last arrow, Infinity, Multishot, creative and mod magazines. Verify actual consumption, explicit empty-slot blocking and unsupported explicit-choice blocking. Try changing selection during charge and closing/opening the storage screen.

### Ranged weapons and resource economy

- Fire every arrow tier from each bow/crossbow; check permitted ammo and gold's iron/blunt exclusion.
- Verify copper/iron/gold/diamond/netherite damage bonuses and gold's 10-tick full bow draw and 48 durability, diamond/netherite bow draws of 26/30 ticks and crossbow loads of 32/40 ticks.
- Iron/gold/diamond/netherite magazines: 3/4/5/6 paid volleys. One click fires one volley.
- Add Multishot: each volley retains the normal spread, consumes one extra ammo unit and makes additional arrows unpickable.
- Check partial magazines, ammo stack boundaries, offhand fireworks, different projectile types in one magazine, creative ammo, weapon breakage and dropped/reloaded/relogged charged items.
- Check custom arrow spread in vanilla bows/crossbows and dispensers. Ordinary vanilla arrows retain their original spread unless a mod skill is active.
- Blunt stun must respect shield facing, resolve the blocking hit before stunning, and scale recovery duration with shield tier.
- Breeze/fire effects apply only on successful damage. Check teammate protection, disabled PvP and entity invulnerability.
- Firework arrows: hit entities and blocks; verify one visible/audible explosion, consumption, nearby damage with falloff, no hits through solid walls, armor/shield behavior, team/PvP protection and close-range self-damage. Try bow/crossbow/dispenser shots, Piercing, Multishot and rapid repeated impacts. It replaces direct arrow damage and must not duplicate explosions or destroy blocks. Ordinary arrows and actual rocket ammunition must remain unchanged.

### Throwing and enchantments

- Throw all seven knife materials, including stone, and all axe/spear tiers. Retrieve the original item, durability, name and enchantments.
- Test Loyalty I–III after entity hits, block hits, chunk unload/reload, player death and server restart. No duplicated survival pickups from creative projectiles.
- Add Throwable first and then try Lunge/Pole Jump; reverse the order. Both anvil/enchanting paths must reject combinations. Pole Jump + Lunge must work.
- Verify Cleaving's exclusion with Sharpness/Smite/Bane, Impact knockback, Unbreaking/Mending and normal weapon enchants.
- Test crafting, smithing, fletching upgrades, copper waxing twice, survival ingredient costs and creative costs. Upgrades preserve charged ammo and item patches while changing maximum durability.

### Movement, skills and visuals

- Test a second Jump press with/without Double Jump boots, then remove boots midair. Only one extra jump is allowed, and fall distance stays intact.
- Test landing resets, climbing, water, spectator mode, flying, gliding, passengers, disconnect/reconnect and death.
- Boost: check 0.54/0.72/0.90 impulses, 8/7/6-second cooldowns, three durability cost before Unbreaking, exhaustion, low-durability rejection and the 2.4 blocks/tick cap. Aerodynamics must not alter its strength or cooldown.
- Overload: check O while gliding, 1.5 impulse, 3.0 speed cap, 216 damage on a normal Elytra and the 20-second shared cooldown. Unbreaking must not discount the price. Refuse to make the Elytra unusable, and test repaired/custom-durability Elytra. Existing above-cap momentum must remain unchanged for both flight skills.
- Aerodynamics: compare gliding from the same altitude/pitch with no enchantment, I and II. Verify longer glide time/distance, identical client/server movement, normal falling unaffected and no infinite level flight.
- Fletching table: craft every arrow, shift-click outputs, use the recipe book, close with inputs remaining, move too far away, break the table and disconnect. Ensure ingredient conservation and no non-arrow outputs; verify old upgrades/waxing still work.
- Loot: open previously unopened End City and ship treasure chests; check extra 1–3 scales occur alongside original loot. Confirm other chests and already filled chests are unaffected.
- Powers: test both hands, R/V on the same tick, supported and unsupported weapon tiers, full/partial shots, expiration, cooldowns and reconnects. Watch owner velocity changes for Boost, Overload, Double Jump and Pole Jump. Check the tooltip's rebound key labels and messages on a server missing the mod.
- Speed: verify +3%/+6%/+9%, mutually exclusive enchanting/anvil paths with Double Jump, and old boots carrying both (Speed takes priority). Depth Strider and Soul Speed must remain compatible.
- Pole Jump needs enchanted spear + ground contact and cannot activate with Throwable.
- Target Lock is limited to one charged shot/volley within three seconds and a 16-second cooldown, obeys line of sight, 24-block range, allies, PvP protection and target validity on release. Swapping to a bow that lacks the skill must not retain its benefit.
- Dead Eye grants +5% damage and 40% less random spread to one charged shot/volley, with a three-second window and 20-second cooldown. Partial bow shots must not gain the effect. Check cooldown packet spam and reconnect behavior.
- Confirm key rebinding, zoom, all 96 textures and their bow draw/crossbow load states, all six custom shield models in both hands, the new 1.5-unit inward position, blocking/normal poses, GUI/ground/item-frame/shelf contexts, enchantment glint, native spear visuals, translations and creative entries. Supplied shield geometry does not render banner layers. Thrown weapons must show their actual item textures/models and glint in flight, at impact, on Loyalty return and after unload/reload. Ordinary tridents must retain their native model.

Record the server/client versions, exact combo steps and observed damage/timing before changing combat hooks.

## Removed wooden ranged weapons (0.8.2)

- Java 25 compilation and native Fabric/data-registry audits passed. Both old IDs are unregistered; five custom bow/crossbow tiers remain. Native ItemStack codec checks preserved names, damage, loaded crossbow arrows and counts, and subsequent saves used vanilla IDs. All 46 remaining recipes parsed successfully.
- A simulated existing browser upload with all 24 obsolete wooden ranged-weapon resource files passed regeneration and validation: old files were deleted and no removed IDs remained in JSON.
- Confirm only vanilla wooden bows/crossbows appear in creative search. Copper through netherite custom weapons remain craftable and upgrade from vanilla weapons.
- Open a saved inventory containing old custom wooden weapons; they should become vanilla equivalents with names, damage, enchantments and loaded projectiles preserved. Native codec audits check component preservation; the saved-world check remains manual.

## Supplied ranged sprites and stone weapons (0.8.3)

- All sixty supplied PNGs are copied byte-for-byte. Idle, pulling_0/1/2 and crossbow arrow/firework models reference the matching sprites. Both browser and complete-project build paths are checked for identical bytes.
- Stone ranged weapons use native BowItem/CrossbowItem classes with 448 durability and normal enchantment tags. Java 25 compilation, native registry/component checks and all 48 recipe codec audits passed after regeneration.
- In-game check: observe all draw/load states for all six materials, craft the stone variants, repair/enchant them and upgrade them at a fletching table.

## Spear rotation, quiver selection and fire durations (0.8.4)

- Native spear tip vectors for all seven materials align with the model-forward axis after rotation; knife offsets remain unchanged. Native renderer/common arrow Mixins applied in the headless client-class audit. This does not render pixels.
- Native component audits cover switching active quivers, offhand priority, moving the selected quiver, saved selection, automatic/explicit ammo filtering and consuming the last named arrow once. Both fire durations are checked as 8/4 seconds.
- In-game: throw every spear horizontally/up/down; confirm tip alignment. With multiple quivers, choose an arrow, switch to a bow, move the quiver and shoot. Verify red compatibility markers, server confirmation, unsupported-choice warnings, inventory mode and next-load behavior for crossbows.
- Compare successful blaze/fire-charge hits on fresh non-fire-resistant targets: eight/four seconds. Also test shields, fire resistance, water and ordinary Flame arrows. Existing longer fire may remain; native protection and extinguishing rules still apply.

## Gameplay and input update (0.9.0)

- All nine native wheel button callbacks, native child hit regions, number keys and numpad keys selected the corresponding named ammo stack at 640×360, 320×240, 320×180 and 200×180 GUI sizes. This headless test does not open a graphics window or shoot as a real player.
- The actual Fabric command event accepted command-scoped Throwable on maces/blocks and reverted to its default outside that scope. The actual injected command wrapper retained native max-level rejection and cleared its scope on the exception. Forced item models, Sharpness level/bonus, spin categories, spear scale and gold ranged durability passed native component checks.
- Shield resistance state reached each tier's exact hit threshold; timeout and changing shield stacks reset progress. Ground-jump multiplier, draw/load times, Pole Jump impulse and the bounded stronger Boost cases passed. New crossbow, jump, command, shield and native impulse Mixins all applied under Fabric.
- In-game checks remain: real quiver clicks/network acknowledgments/shooting with mixed ammo and multiple quivers; axes spinning and enlarged spears; block/mace throwing, survival/creative ammo consumption and Sharpness against armor; Lunge + Pole Jump with misses/hits/downward aim/hunger/cooldowns; first and second jumps; all weapon charging/Quick Charge; repeated frontal shield-disable hits; grappling living/non-living moving targets. Never count these headless checks as in-game testing.
