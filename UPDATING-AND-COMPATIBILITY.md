# Compatibility and maintaining PvP+

This update provides optional integrations, not a guarantee that every mod works.
No third-party mods are installed or redistributed. Keep matching client/server
PvP+ versions. All existing material, Punchy, Siftite and Storm Core work remains.

## New integrations

- Farmer's Delight Refabricated: custom knives are in its tools/knives and enchantable/knife tags and the shared c:tools/knife tag. This makes them eligible for tag-based cutting and enchantment recognition. It does not copy Farmer's Delight item-class-specific code. The diagonal PvP+ recipe remains unchanged.
- Enchantment Descriptions: English descriptions for all 13 PvP+ enchantments, using its documented .desc translation keys.
- Common Fabric tool tags: bows, crossbows, shields, knives, maces/hammers, fishing rods/flails, melee and ranged weapons. Scythe and spear categories are also exposed for consumers. These support mods that read the corresponding tags; class checks may need dedicated adapters.
- Recipe viewers: the existing crafting/smithing recipes remain standard Minecraft recipes. This update does not add a custom EMI, JEI or REI plugin or claim their entire UI is tested.
- Combat mods: the update report tracks Better Combat availability, but no Better Combat moveset is added. Punchy support remains included. Availability is not verified compatibility.

## Adding a material

Edit scripts/compat_materials.json, using a similar existing entry as your template.
Use a unique lower-case id; the provider's exact Fabric mod ID, Modrinth slug,
reference version and ingot ID; a verified material palette; rank (0–5), durability,
speed and enchantability; and the standard/external_spear/external_horse/fireproof
flags. standard=true reuses the provider's player armor/tools. external_horse=true
reuses both horse and nautilus armor, so verify both IDs exist first. External IDs
normally use material_kind; external_prefix can adjust armor resource tags.

Optional upgrade_from and upgrade_template fields make custom non-arrow weapons
smithing upgrades of a previous PvP+ material. Example: upgrade_from=netherite,
upgrade_template=enderitemod:enderite_upgrade_smithing_template. The source base
items must exist. Do not invent registry IDs or copy another material's palette.

Run python scripts/sync_materials.py, python scripts/generate_resources.py and
python scripts/validate_resources.py. Gradle runs these automatically before
compiling/packaging. Java Tier, Ammo, material registry and arsenal lists are
synchronized from the manifest. Existing base materials and special mechanics
still have code; this does not generate unique abilities for arbitrary mods.
Test recipes, repair, throwing, models and absent-mod startup before release.

## Checking updates on GitHub

Keep the usual Build workflow. Also create .github/workflows/compatibility.yml
with the supplied compatibility-workflow.yml.txt if your upload hides dotfolders.
The Compatibility and update report workflow runs each Monday or manually.
Choose a Minecraft version to query and download compatibility-report from its
artifacts. It checks Fabric releases and reports reference-version differences,
missing versions and lookup failures. It does not update files, create commits,
merge changes, download/install mods or claim new releases are safe.
Scheduled workflows must be on the default branch and Actions enabled.

## Moving to another Minecraft version

Use a separate branch. Change minecraft_version, loader_version,
fabric_api_version and loom_version in gradle.properties; the mod metadata now
uses those properties too. Check whether Java and the Gradle wrapper need changes
(Java is currently 25 in build.gradle and the Build workflow). Run the full build,
then test singleplayer, dedicated-server startup and your actual modpack. Fix
changed Minecraft APIs and mixin targets before releasing. Never widen the
Minecraft dependency to claim untested versions are compatible.

## Verification for this release

Java compilation, generated-resource validation, palette checks and native Fabric
registry startup without optional mods passed. The update-report script was run
against Modrinth. Full gameplay and all third-party mod combinations were not run.
The scheduled workflow has not been run on your GitHub repository yet.
