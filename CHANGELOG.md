# 0.6.3

- Integrated the supplied wolf_armor.bbmodel (eleven inflated box-UV cubes) and original 64×32 wolf_armor.png. A deterministic build exporter converts the CEM coordinate system to Minecraft's native wolf skeleton; no OptiFine is needed.
- Added a client-only armor mesh selector. Dragon Scale armor keeps walking, sitting, head, shaking and tail animations plus normal equipment/glint/crack rendering; ordinary wolf armor keeps its native mesh.
- Replaced the quiver inventory icon with a transparent 16×16 sprite based on the supplied brown leather quiver/arrow/strap reference. Storage and C-key selection behavior are unchanged.
- Native client armor Mixin application, eleven-cube baking, animation poses and equipment texture routing passed headless checks. Full visual gameplay and Gradle verification remain outstanding.
- Preserved all 48 added items, 49 recipes, wolf flight, quiver storage and existing overhaul features.
- Output: `pvp-overhaul-0.6.3.jar`.

# 0.6.2

- Integrated all seven latest supplied knife PNGs without pixel changes.
- Added a craftable stone knife: cobblestone above a stick, 4 base damage, 3.2 attack speed, one block shorter interaction range, 131 durability. It uses the same stealth/throwing behavior and weapon/durability/Loyalty tags as the other knives.
- All 48 added items have survival crafting/smithing routes; there are 49 mod recipes including the vanilla trident recipe.
- Preserved wolf flight armor, quiver wheel, shields, axe/arrow textures, movement powers and other overhaul features.
- Output: `pvp-overhaul-0.6.2.jar`.

# 0.6.1

- Replaced all seven battleaxe and eleven arrow art inputs with the latest supplied PNGs, copied without pixel changes. `pixil-frame-5.png` supplies the stone battleaxe.
- Preserved all 47 items, survival recipes, dragon-scale wolf flight armor, quiver wheel and other overhaul features.
- Output: `pvp-overhaul-0.6.1.jar`.

# 0.6.0

- Added craftable Dragon Scale Wolf Armor: six scales + native wolf armor, 128 durability, scale repair, native equipment and damage absorption. Owned adult standing/unleashed wolves follow nearby Elytra flight and descend afterward; flight restores gravity and resets on reload.
- Added craftable nine-stack quiver with native saved item-container components, arrow-only storage and a C-key radial selector. Ordinary arrow components and native consumption rules are retained, with stale-draw and source-slot protections.
- Added a required survival recipe-coverage audit for all 47 mod items. All 48 recipes passed the actual Minecraft 26.3 recipe codec.
- Integrated the latest six battleaxe sprites from the supplied sheet, a copper variant and eleven revised arrow textures. Added the stone battleaxe and its recipe/enchantment tags. Art inputs are included in the browser upload so builds reproduce them.
- Preserved firework damage, shields, native spears, movement enchantments, End City scales, fletching and powers.
- Output: `pvp-overhaul-0.6.0.jar`; update clients and server together.

# 0.4.0

- Restored original Boost impulses (0.30/0.40/0.50) and 8/7/6-second cooldowns. Aerodynamics no longer boosts its strength or reduces its cooldown.
- Added Overload I on O: 1.5 impulse, 3.0 resulting-speed cap, exact half-maximum Elytra durability cost and a 20-second cooldown shared with Boost. It refuses to make the Elytra unusable; Unbreaking cannot discount the cost.
- Aerodynamics I/II now extends gliding via 10%/20% lower glide gravity and less horizontal drag, identically on client and server.
- Reanchored the custom shields around the grip and removed large display offsets that separated them from the hand. Geometry and embedded textures remain original.
- Added arrow crafting at fletching tables using a synchronized arrow-only 3×3 grid. Upgrade/waxing interactions remain available.
- Added 35% chance of 1–3 Dragon Scales to native End City treasure chests as a separate loot pool.
- Skill activation supports either hand, binds to the chosen weapon type, allows R/V together, gives eligibility/cooldown feedback and explicitly syncs owner movement. Target Lock uses body targeting and corrected gravity compensation instead of the old excessive eye-height correction. Tooltips show bound controls.
- Output: `pvp-overhaul-0.4.0.jar`.

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
