# Rose gold for PvP+

Minecraft 26.3 / Fabric / Java 25. Main mod version stays 0.10.15.

Rose gold is included directly; Rose Gold Equipment is not required.

Combine one copper ingot and one gold ingot to make raw rose gold, then smelt
or blast it into an ingot. Nuggets and both storage blocks convert back and forth.

Added standard sword, axe, pickaxe, shovel, hoe, helmet, chestplate, leggings and
boots, plus PvP+ bows, crossbows, knives, battleaxes, shields, spears, flails,
hammers, scythes, halberds and arrows. All have survival recipes. Knives remain
diagonal; hammers and battleaxes retain the updated layouts.

Standard equipment textures are from Rose Gold Equipment 1.8.9 by IGoByJake,
under its MIT license. The original notice is included in THIRD-PARTY-NOTICES.txt
and inside the built JAR. Custom weapons keep the existing PvP+ artwork and
model transforms; Minecraft's native palette permutations supply the rose metal
colors. Original sprites are unchanged. Held, charged, blocking and thrown
variants are included.

Rose gold has iron-class combat strength and mining level, gold-like mining
speed (12) and enchantability (25). Standard tools have 384 durability, bows and
crossbows 384, and custom melee weapons/shields 900. Armor has iron-class defense
and durability multiplier 18. The bow draws in 16 ticks, supports Target Lock
and Dead Eye, and uses the normal PvP+ ammo rules. Rose gold ingots repair it.

Validation performed: Java 25 compilation against Minecraft 26.3/Fabric,
resource generation and validation (106 items), native Fabric server bootstrap,
real Minecraft tag/enchantment loading and durability checks for new equipment,
and the existing balance checks. Interactive client rendering and multiplayer
playtesting have not been performed. The GitHub workflow performs the final
Gradle build and publishes its JAR artifact.
