# Material support update

Fabric 26.3. Install each source mod separately; none is required by PvP+.

- Advanced Netherite: iron, gold, emerald and diamond netherite weapon tiers remain supported, with sequential smithing upgrades using the source ingots and Netherite Upgrade template.
- Enderite Mod (enderite-mod / enderitemod), version 1.10.0: custom knives, battleaxes, hammers, scythes, halberds, flails, bows, crossbows, shields and arrows. Durable custom weapons upgrade from their netherite equivalent using Enderite's upgrade template and ingot. The source mod's standard tools, player armor, spear, horse armor and nautilus armor are reused. PvP+ adds wolf armor. Source-mod teleportation and void-floating abilities are not copied onto PvP+ items.
- Rose Gold Equipment (rosegoldequipment), version 1.10.0: custom weapons, arrows and animal equipment now require that mod's ingot. Standard tools and player armor come from Rose Gold Equipment. PvP+ no longer registers separate rose-gold ingots, nuggets, raw material, blocks, standard tools or player armor, and no longer adds a rose-gold trim material.

Material colors are extracted from the installed-version reference ingot sprites and applied to the existing PvP+ artwork. Handles and strings are preserved. Custom item IDs are stable; optional custom items are hidden from the creative tab and their recipes are gated when their provider is absent.

Existing standalone PvP+ rose-gold materials, blocks and standard equipment have been removed. They will not automatically convert to Rose Gold Equipment items in existing worlds. Back up a world before loading this removal update. Existing custom rose-gold weapon IDs remain.

Upload PATCH-ONLY over your existing source, preserving folders, then build with GitHub Actions. The resource generator removes obsolete rose-gold resources automatically, including when updating an old expanded source tree. No manual repository deletion is needed.

Checks: Java 25 compilation; Fabric registry bootstrap without optional mods (removed IDs absent, custom IDs registered but hidden); resource/recipe/tag link validation; 326 palette-map checks; Punchy item/clip reference checks. A full Gradle build and gameplay with the complete modpack have not been run.

Reference downloads inspected: Advanced Netherite fabric-2.4.4-mc26.3, Enderite Mod 1.10.0, Rose Gold Equipment 1.10.0. No third-party mod JARs are bundled.
