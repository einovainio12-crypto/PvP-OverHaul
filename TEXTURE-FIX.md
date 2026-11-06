# Material texture shading fix

Compared the bundled original iron and diamond weapon artwork against rendered
compatibility variants, and checked native Enderite, Rose Gold Equipment and
Advanced Netherite swords for their material appearance.

The previous palette conversion spread however many metal shades a sprite used
across the full ramp. A bow or flail with only two metal shades therefore used
the darkest outline and brightest highlight, even though its source shades were
both light. Each source shade now keeps its brightness position within the
material ramp. This restores the distinction between outlines, midtones and
highlights across weapon sprites and their animation frames.

The original pixel artwork, dimensions, transparency, handles and strings are
unchanged. Colors still come from each compatibility material's recorded palette.
Enderite remains dark green, matching its source mod. No combat code changed.

Validated 326 palette mappings, unchanged non-metal colors, resource references,
and Punchy references. Contact sheets were inspected before and after. In-game
rendering has not been tested for this update.

Upload PATCH-ONLY over the repository, preserving folders, then rebuild. The
build regenerates the corrected palettes automatically. Full source is included.
