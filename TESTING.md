# Validation and gameplay checklist

## Validation performed in the creation environment

- All main and client Java sources compiled with a Java 25 JDK against the **actual Minecraft 26.3 client JAR**, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3 and the game's dependency libraries. Compilation used `javac --release 25 -proc:none`; this establishes API/type correctness, not gameplay correctness or Mixin application.
- `python3 scripts/validate_resources.py` passed: 214 JSON resources, 44 added items, internal model/texture links, recipe symbol/result consistency, entrypoint/mixin source coverage and enchantment exclusions.
- A manually packaged mod loaded under **Fabric Loader 0.19.5 + Fabric API 0.162.0+26.3** on the actual 26.3 dedicated-server classes. `KnotServer --initSettings nogui` exited successfully and logged `PvP Overhaul loaded for Minecraft 26.3`. A temporary test initializer forced all six server combat/migration target classes to load with no Mixin injection failure. It verified that all six old spear IDs are absent from the item registry, decode to the correct native items, retain count/damage/name patches, and encode back to native IDs. This checks mod initialization and server Mixin application only; it does not load a world or validate datapacks/gameplay. The EULA was not accepted automatically.
- The dependency-free Java balance checks passed: sustained gold/netherite bow tradeoff, charge timing, stacked damage bounds, cooldown floors, overleveled-enchantment limits, preservation of above-cap momentum, invalid-motion rejection, and 10,000 seeded boost-vector cases. These checks are also part of Gradle `check`.
- Resource generation is reproducible from the included Python source and uses no third-party Python dependencies.
- Minecraft 26.3's official manifest reports Java 25. The Gradle wrapper distribution is pinned to 9.6.0 with its SHA-256 checksum.
- Full Gradle execution is **not verified in this environment**: Gradle exits during filesystem initialization with `java.io.IOException: Mount point not found`, before evaluating the project. GitHub Actions is provided to run the normal Loom build on Linux and Windows. An included workflow is not evidence that either GitHub run has already passed.

## Required before a public PvP release

Use a disposable test world with two clients and a dedicated Fabric 26.3 server. Build with `./gradlew build`, install the resulting JAR plus Fabric API, and watch client/server logs for Mixin failures and datapack/model loading errors. This is a v0.2 implementation; multiplayer balance and complete technique compatibility have not been verified.

### Combat regression

- Confirm vanilla sword, axe, bow, crossbow and mace cooldown/critical behavior against an unmodded 26.3 baseline.
- Reproduce your exact attribute-swap combos, shield stun/slam chains, spear Lunge tricks, Riptide interactions, and mace smash setups. Verify damage, knockback, charge, reach and timing.
- Compare +1 trident interaction-range swaps and short-range knife swaps. Ordinary attribute components are used, but packet timing behavior must be tested with your real technique.
- Knife sneak/invisibility multiplier should apply once, not twice when both conditions are present, and must retain cooldown damage scaling.

### Ranged weapons and resource economy

- Fire every arrow tier from each bow/crossbow; check permitted ammo and gold's iron/blunt exclusion.
- Verify copper/iron/gold/diamond/netherite damage bonuses and gold's 18-tick full bow draw.
- Iron/gold/diamond/netherite magazines: 3/4/5/6 paid volleys. One click fires one volley.
- Add Multishot: each volley retains the normal spread, consumes one extra ammo unit and makes additional arrows unpickable.
- Check partial magazines, ammo stack boundaries, offhand fireworks, different projectile types in one magazine, creative ammo, weapon breakage and dropped/reloaded/relogged charged items.
- Check custom arrow spread in vanilla bows/crossbows and dispensers. Ordinary vanilla arrows retain their original spread unless a mod skill is active.
- Blunt stun must respect shield facing, resolve the blocking hit before stunning, and scale recovery duration with shield tier.
- Breeze/fire effects apply only on successful damage. Check teammate protection, disabled PvP and entity invulnerability.

### Throwing and enchantments

- Throw knives and all axe/spear tiers. Retrieve the original item, durability, name and enchantments.
- Test Loyalty I–III after entity hits, block hits, chunk unload/reload, player death and server restart. No duplicated survival pickups from creative projectiles.
- Add Throwable first and then try Lunge/Pole Jump; reverse the order. Both anvil/enchanting paths must reject combinations. Pole Jump + Lunge must work.
- Verify Cleaving's exclusion with Sharpness/Smite/Bane, Impact knockback, Unbreaking/Mending and normal weapon enchants.
- Test crafting, smithing, fletching upgrades, copper waxing twice, survival ingredient costs and creative costs. Upgrades preserve charged ammo and item patches while changing maximum durability.

### Movement, skills and visuals

- Test a second Jump press with/without Double Jump boots, then remove boots midair. Only one extra jump is allowed, and fall distance stays intact.
- Test landing resets, climbing, water, spectator mode, flying, gliding, passengers, disconnect/reconnect and death.
- Boost: check 8/7/6-second cooldowns, Aerodynamics bounds, three durability cost before Unbreaking, exhaustion, low-durability rejection, and the 1.8 blocks/tick incremental cap. Existing rocket/Riptide momentum above that cap must stay unchanged.
- Speed: verify +3%/+6%/+9%, mutually exclusive enchanting/anvil paths with Double Jump, and old boots carrying both (Speed takes priority). Depth Strider and Soul Speed must remain compatible.
- Pole Jump needs enchanted spear + ground contact and cannot activate with Throwable.
- Target Lock is limited to one charged shot/volley within three seconds and a 16-second cooldown, obeys line of sight, 24-block range, allies, PvP protection and target validity on release. Swapping to a bow that lacks the skill must not retain its benefit.
- Dead Eye grants +5% damage and 40% less random spread to one charged shot/volley, with a three-second window and 20-second cooldown. Partial bow shots must not gain the effect. Check cooldown packet spam and reconnect behavior.
- Confirm key rebinding, zoom, all 92 textures and their bow draw/crossbow load states, shield icons versus held 3D models, native spear visuals, translations and creative entries. Custom thrown projectiles intentionally use the vanilla trident visual in v0.2.

Record the server/client versions, exact combo steps and observed damage/timing before changing combat hooks.
