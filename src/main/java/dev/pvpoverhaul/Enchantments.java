package dev.pvpoverhaul;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class Enchantments {
    public static int level(ItemStack stack,String name) {
        Identifier id=name.indexOf(':')>=0?Identifier.parse(name):PvpOverhaul.id(name);
        var key=ResourceKey.create(Registries.ENCHANTMENT,id);
        for(var holder:stack.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS,
                net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).keySet())
            if(holder.is(key)) return EnchantmentHelper.getItemEnchantmentLevel(holder,stack);
        return 0;
    }
    public static int level(LivingEntity owner,ItemStack stack,String name) {
        Identifier id=name.indexOf(':')>=0 ? Identifier.parse(name) : PvpOverhaul.id(name);
        var key=ResourceKey.create(Registries.ENCHANTMENT,id);
        var holder=owner.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key);
        return holder.map(h->EnchantmentHelper.getItemEnchantmentLevel(h,stack)).orElse(0);
    }
    private Enchantments() {}
}
