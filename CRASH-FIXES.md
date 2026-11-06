# Flail and Create Fly rendering fixes

The supplied crash report fails at FlailHookRenderer.java:33 with:
`Invalid atlas id: minecraft:textures/atlas/items.png`.

The renderer now requests `AtlasIds.ITEMS` (`minecraft:items`), the definition ID
accepted by AtlasManager. It previously passed the texture location instead.
This fixes the identified crash path for rose-gold and compatibility flails.

The low-shield renderer now uses a MixinExtras method wrapper with try/finally.
Non-shield items do not touch the pose stack. Shield rendering always unwinds its
frame, including when another mod cancels rendering or throws. Create Fly's hand
hook cancels vanilla rendering for its tools; the previous separate HEAD/RETURN
injections could leave an unmatched matrix frame in that situation.

Checks passed: Java 25 compilation; headless native client-mixin transformation
with Create Fly 26.3-6.0.9-1's actual hand-rendering mixin; repeated early-return,
non-shield and exception pose-stack regression checks; resource/palette validation.
The Create Fly mixin check uses an isolated test mod with its initialization
turned off. It is not a full game session with Create Fly machinery running.

The world-loading log confirms a separate Create Fly recipe-loader failure:
RecipeLoaderTask.apply dereferences a null PendingRegistration. Fabric returns
null when a conditional resource is skipped. The optional CreateRecipeLoaderMixin
returns early for that null entry, preserving skipped recipes and existing errors.
It does not remove recipe conditions or require Create Fly to be installed.

Regression checks use Create Fly 26.3-6.0.9-1's actual recipe-loader class:
1,000 skipped entries return null without failing, the underlying loader runs
exactly once per entry, and an existing recipe error is returned unchanged.
These checks run in an isolated Fabric bootstrap, not a full Create Fly world.

The complete Gradle build and in-game playtesting remain outstanding. Use the
included GitHub workflow to build. Fabric Loader already supplies MixinExtras;
the new Gradle compileOnly entry makes its annotations available to the compiler.
