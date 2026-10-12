# 0.3.0

- Firework arrows now detonate as one-star vanilla fireworks on the first entity or block impact. Native area damage, falloff, sight checks, particles and sound replace the old Glowing-only effect. The arrow is consumed and cannot make repeated Piercing explosions; no block destruction.
- Increased Boost's base impulses by 20% to 0.36/0.48/0.60. Cooldowns, costs, Aerodynamics and the incremental 1.8 blocks/tick cap remain unchanged.
- Integrated all six supplied Blockbench shield meshes and embedded textures, including normal and blocking poses. The supplied `shield.bbmodel` is used for the wood tier. Original models are kept in `scripts/shields` and exported at build time.
- Complete project and browser upload updated to 52 files. Output: `pvp-overhaul-0.3.0.jar`.

# 0.2.1

Fresh complete source distribution. Includes the Gradle test-discovery fix and automatic resource repair, a complete generated project, and a single browser-upload folder. Gameplay features and balance values match 0.2.0. Output: `pvp-overhaul-0.2.1.jar`.

# 0.2.0

Resource repair: automatically run the existing generator before resource validation, JAR packaging and source-JAR packaging. Missing texture uploads and old enchantment/tag JSON files are regenerated. The browser kit now has one 43-file project upload.

Build fix: allow the framework `test` task to discover no tests, because `BalanceChecks` runs through its dedicated `balanceChecks` task. Balance/resource checks and actual test failures remain enforced.

- Boost I/II/III: impulse 0.30/0.40/0.50; cooldown 8/7/6 seconds; base three durability cost and 0.6 exhaustion. Aerodynamics adds 0.025 impulse and reduces cooldown by 0.25 seconds per level. Only the added impulse is capped; existing rocket/Riptide momentum is preserved.
- Speed I/II/III: +3%/+6%/+9%, reduced enchanting weight. Mutually exclusive with Double Jump; Speed takes priority on older boots that already carry both.
- Bow tier damage: wood/gold 0%, copper 3%, iron 6%, diamond 9%, netherite 12%. Gold draw is 18 ticks; all other mod bows use 20. Crossbow tier damage and magazines stay as before.
- Target Lock: one charged shot, three-second window, 16-second cooldown, 24-block range and narrow cone rechecked at release.
- Dead Eye: one charged shot/volley, three-second window, 20-second cooldown, +5% base damage and 40% less random spread. Partial bow shots do not receive these skill bonuses.
- Removed all six mod spear registrations and custom spear recipes; reuse native spears, including stone. Throwable/Pole Jump/Loyalty apply to the native items.
- Added six exact legacy-ID mappings so v0.1 saved spear stacks decode as the matching native items and save using native IDs. No hidden duplicate item registrations.
- Replaced placeholder artwork with 92 original, editable 16px textures and proper bow/crossbow states. Shields have tier-specific inventory icons and keep the vanilla held renderer.
- Automatically exclude obsolete spear resource paths left behind by an overlay browser upload.
- Added dependency-free balance invariant checks to Gradle check.

These are initial tuning changes. Dedicated-server initialization, migration and mathematical checks passed; multiplayer combat/visual testing and a full Loom build still need verification.
