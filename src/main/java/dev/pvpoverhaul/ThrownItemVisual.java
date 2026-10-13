package dev.pvpoverhaul;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/** The projectile's original stack is explicitly synchronized for client item rendering. */
public interface ThrownItemVisual {
    ItemStack pvp$getVisualItem();
    static boolean usesItemModel(ItemStack stack) {
        return !stack.isEmpty() && (ModItems.isKnife(stack) || ModItems.isBattleaxe(stack)
            || stack.is(ItemTags.AXES) || ModItems.isSpear(stack));
    }
}
