# 0.2.0

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
