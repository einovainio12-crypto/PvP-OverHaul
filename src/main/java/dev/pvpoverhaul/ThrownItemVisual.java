package dev.pvpoverhaul;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/** The projectile's original stack is explicitly synchronized for client item rendering. */
public interface ThrownItemVisual {
    ItemStack pvp$getVisualItem();
    static float modelPitchOffset(ItemStack stack) {
        // Native spear sprites point upper-left; knives/axes point upper-right.
        return ModItems.isSpear(stack)?-135f:-45f;
    }
    static boolean usesItemModel(ItemStack stack) {
        return !stack.isEmpty() && (ModItems.isKnife(stack) || ModItems.isBattleaxe(stack)
            || stack.is(ItemTags.AXES) || ModItems.isSpear(stack) || Enchantments.level(stack,"throwable")>0);
    }
    static boolean spins(ItemStack stack) { return ModItems.isBattleaxe(stack) || stack.is(ItemTags.AXES); }
    static float modelScale(ItemStack stack) { return ModItems.isSpear(stack)?1.35f:1f; }
}
