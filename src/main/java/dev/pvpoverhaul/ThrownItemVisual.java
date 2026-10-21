package dev.pvpoverhaul;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/** The projectile's original stack is explicitly synchronized for client item rendering. */
public interface ThrownItemVisual {
    ItemStack pvp$getVisualItem();
    static float modelPitchOffset(ItemStack stack) {
        // Native spear sprites point upper-left; knives/axes point upper-right.
        return ModItems.isSpear(stack)||isHalberd(stack)?-135f:-45f;
    }
    static boolean usesItemModel(ItemStack stack) {
        return !stack.isEmpty() && (ModItems.KINDS.containsKey(stack.getItem()) || ModItems.isKnife(stack) || ModItems.isBattleaxe(stack)
            || stack.is(ItemTags.AXES) || ModItems.isSpear(stack) || Enchantments.level(stack,"throwable")>0);
    }
    static boolean spins(ItemStack stack) { return ModItems.isBattleaxe(stack) || stack.is(ItemTags.AXES) || java.util.List.of("shuriken","hammer","flail").contains(ModItems.KINDS.getOrDefault(stack.getItem(),"")); }
    static boolean isHalberd(ItemStack stack) { return "halberd".equals(ModItems.KINDS.get(stack.getItem())); }
    static float modelScale(ItemStack stack) { return ModItems.isSpear(stack)||isHalberd(stack)?1.35f:1f; }
    static ItemStack heldProjectileModel(ItemStack stack) {
        if(stack.isEmpty())return stack;
        var key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if(!(ModItems.isSpear(stack)&&key.getNamespace().equals("minecraft")) && !isHalberd(stack))return stack;
        ItemStack visual=stack.copy();
        visual.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,PvpOverhaul.id("thrown_visual/"+key.getPath()));
        return visual;
    }
}
