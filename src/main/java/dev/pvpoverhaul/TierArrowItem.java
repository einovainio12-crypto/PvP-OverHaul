package dev.pvpoverhaul;
import net.minecraft.world.item.ArrowItem;

/** Vanilla Arrow serializes and retrieves the custom stack; hit effects inspect that saved stack. */
public final class TierArrowItem extends ArrowItem {
    public final Ammo ammo;
    public TierArrowItem(Properties properties, Ammo ammo) { super(properties); this.ammo=ammo; }
}
