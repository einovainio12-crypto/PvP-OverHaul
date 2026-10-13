This mod contains a bit of ai code since im still learning so i used some ai code as help but mostof the code is still mine
# PvP Overhaul

A Fabric mod for Minecraft Java 26.3 that adds weapons, special arrows and movement enchantments. The aim is to give each material a reason to use it, with tradeoffs in damage, speed, durability and abilities.

The mod is required on the server and every player's client.

## What's in the mod

### Weapons and shields

- Stone, copper, iron, gold, diamond and netherite bows and crossbows.
- Knives and battleaxes in seven materials, including stone.
- Six shield tiers. Iron shields take two qualifying stun hits, diamond takes three and netherite takes four.
- Throwable weapons, spinning axes and battleaxes, and stronger Sharpness damage when thrown.
- Craftable tridents. Vanilla spears are used instead of adding duplicate spear items.

Gold bows draw quickly but have very little durability. Diamond and netherite weapons take longer to charge and offer more durability, abilities and ammunition options. Crossbow magazines fire one volley per click.

### Arrows and the quiver

Special arrows include blunt, blaze, breeze, fire charge, firework and grappling arrows, alongside material-based arrows.

Blaze arrows burn for eight seconds; fire-charge arrows burn for four. Firework arrows create a damaging explosion without destroying blocks. Grappling arrows pull you toward a block or entity you hit. Sneak to release the line.

The quiver stores nine arrow stacks. Open its selection wheel to choose ammunition, or use inventory mode to return to your normal arrows. Some arrows require a higher-tier weapon. Changing the selection affects the next crossbow load, so it won't replace arrows already loaded.

Right-click a fletching table to craft arrows.

### Movement and equipment

- **Boost:** a burst of speed while gliding with an Elytra.
- **Overload:** a stronger burst that costs half the Elytra's maximum durability.
- **Aerodynamics:** reduces drag and gravity during gliding.
- **Double Jump:** adds an extra jump in the air. Level II also increases your first jump.
- **Pole Jump:** vault with a spear. With Lunge equipped too, looking down while grounded turns a Lunge into a vault.
- **Dragon Scale Wolf Armor:** lets a standing, owned wolf follow your Elytra flight and descend when you land.

Dragon Scales can be found in End City treasure chests or crafted. All added items have survival recipes.

## Installation

You need:

- Minecraft Java **26.3**
- Fabric Loader **0.19.5**
- Fabric API **0.162.0+26.3**
- Java **25**

Put Fabric API and the built `pvp-overhaul-0.9.0.jar` in your `mods` folder. For multiplayer, install them on the server and all clients. Replace the previous PvP Overhaul JAR when updating.

## Controls

| Key | Action |
| --- | --- |
| Hold Z | Zoom with a supported bow or crossbow |
| R | Target Lock |
| V | Dead Eye |
| B | Elytra Boost |
| O | Elytra Overload |
| C | Open the quiver selection wheel |
| Shift+C | Open quiver storage |
| G | Pole Jump |
| Press Jump again in the air | Double Jump |

Click a quiver slot or press **1–9**, including numpad keys. Press **0** for inventory ammunition. An empty or unsupported selected slot stops shooting instead of choosing a different arrow.

Right-click to throw a knife. Sneak-right-click to throw an item enchanted with Throwable. Keybindings can be changed in Minecraft's controls. Item tooltips show which abilities are available.

## Throwable commands

Hold the item you want to enchant, then run:

```mcfunction
/enchant @s pvpoverhaul:throwable 1
```

This works with items such as maces and blocks as well as weapons. Normal command permissions and enchantment conflicts still apply.

## Building

### GitHub Actions

1. Open **Actions → Build Fabric 26.3**.
2. Run the workflow, or push a commit to start a build.
3. Open the successful run and download its artifact.
4. Extract the ZIP and use `pvp-overhaul-0.9.0.jar`. The `-sources.jar` is for source code, not installation.

### Local build

Install Java 25 and Python 3, then run:

```bash
chmod +x gradlew
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

The built JAR is in `build/libs`.

## Bugs and balance

The mod is still being tested. Compilation and automated checks have passed, but multiplayer behavior and visuals need more in-game testing.

If you find a bug, open an issue with what happened, how to reproduce it, your mod version and any relevant logs. Balance feedback is welcome too.

## License

The code is licensed under MIT. Supplied artwork and models retain their original rights; the code license does not relicense those assets.
