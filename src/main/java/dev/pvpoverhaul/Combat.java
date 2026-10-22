package dev.pvpoverhaul;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class Combat {
    public static int fireSeconds(Ammo ammo) { return ammo==Ammo.BLAZE?8:ammo==Ammo.FIRE_CHARGE?4:0; }
    public static float uncertainty(Projectile projectile,LivingEntity shooter) {
        float value=1;
        if (projectile instanceof AbstractArrow arrow && arrow.getPickupItemStackOrigin().getItem() instanceof TierArrowItem item)
            value=item.ammo.uncertainty;
        return projectile instanceof AbstractArrow arrow && arrow.isCritArrow() && Skills.deadEyeActive(shooter,arrow.getWeaponItem()) ? value*Balance.DEAD_EYE_SPREAD : value;
    }
    public static void adjustProjectile(Projectile p,LivingEntity shooter,double multiplier) {
        if (!(p instanceof AbstractArrow arrow)) return;
        arrow.setBaseDamage(((dev.pvpoverhaul.mixin.AbstractArrowAccessor)arrow).pvp$getBaseDamage()*multiplier*(arrow.isCritArrow() && Skills.deadEyeActive(shooter,arrow.getWeaponItem())?Balance.DEAD_EYE_DAMAGE:1));
        if (arrow.getPickupItemStackOrigin().getItem() instanceof TierArrowItem item && fireSeconds(item.ammo)>0) arrow.igniteForSeconds(fireSeconds(item.ammo));
    }
    public static float knifeDamage(Player player,float damage) {
        return ModItems.isKnife(player.getMainHandItem()) && (player.isShiftKeyDown() || player.isInvisible()) ? damage*2 : damage;
    }
    public static boolean depthStriderRiptide(Player p) {
        return Enchantments.level(p,p.getItemBySlot(EquipmentSlot.FEET),"minecraft:depth_strider")>0;
    }
    public static void arrowHit(AbstractArrow arrow,EntityHitResult hit) {
        if (!(arrow.level() instanceof ServerLevel level) || !(arrow.getPickupItemStackOrigin().getItem() instanceof TierArrowItem item)) return;
        Entity target=hit.getEntity();
        if (arrow.getOwner() instanceof Player attacker && target instanceof Player defender && !attacker.canHarmPlayer(defender)) return;
        if (item.ammo==Ammo.BLUNT && target instanceof LivingEntity living && living.isBlocking()) {
            // Only frontal hits can stun; uses vanilla blocking's own cooldown scaling.
            Vec3 incoming=arrow.position().subtract(living.position()).normalize();
            if (living.getLookAngle().dot(incoming)>0) {
                ItemStack shield=living.getUseItem(); BlocksAttacks blocks=shield.get(DataComponents.BLOCKS_ATTACKS);
                if (blocks!=null) blocks.disable(level,living,2f,shield);
            }
        }
    }
    public static void arrowAfterHit(AbstractArrow arrow,EntityHitResult hit) {
        if (!(arrow.level() instanceof ServerLevel level) || !(arrow.getPickupItemStackOrigin().getItem() instanceof TierArrowItem item)) return;
        Entity target=hit.getEntity();
        if (arrow.getOwner() instanceof Player attacker && target instanceof Player defender && !attacker.canHarmPlayer(defender)) return;
        if (target instanceof LivingEntity living) {
            if(item.ammo==Ammo.ICE) Arsenal.freeze(living,2);
            if (fireSeconds(item.ammo)>0) living.igniteForSeconds(fireSeconds(item.ammo));
            if (item.ammo==Ammo.BREEZE) {
                Vec3 push=arrow.getDeltaMovement().normalize().scale(0.8);
                living.push(push.x,0.45,push.z);living.syncVelocity=true;
            }
        }
    }
    public static float thrownDamage(AbstractArrow projectile,float vanilla) {
        ItemStack weapon=projectile.getPickupItemStackOrigin();
        float base=thrownBaseDamage(weapon,vanilla);
        return base+Balance.thrownSharpnessBonus(Enchantments.level(weapon,"minecraft:sharpness"));
    }
    public static float thrownBaseDamage(ItemStack weapon,float vanilla) {
        if(weapon.is(Items.MACE)) return Math.max(6,(float)weapon.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND));
        if(ModItems.KINDS.containsKey(weapon.getItem()))return (float)weapon.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND);
        if(weapon.is(ModItems.STONE_KNIFE)) return 4;
        if(weapon.is(ModItems.STONE_BATTLEAXE)) return 9;
        Tier t=ModItems.TIERS.get(weapon.getItem());
        if (ModItems.isKnife(weapon)) return 3+t.rank;
        if (ModItems.isSpear(weapon)) return weapon.is(Items.STONE_SPEAR)?6:5+(t==null?0:t.rank);
        if (ModItems.isBattleaxe(weapon)) return 8+t.rank;
        if (weapon.is(net.minecraft.tags.ItemTags.AXES)) return (float)weapon.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).compute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND);
        if(Enchantments.level(weapon,"throwable")>0) {
            float damage=(float)weapon.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
                .compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND);
            return Math.max(4,damage);
        }
        return vanilla;
    }
    /** Projectile hits do not satisfy vanilla Wind Burst's melee falling predicate. */
    public static void thrownMaceBurst(AbstractArrow projectile,Entity victim) {
        ItemStack stack=projectile.getPickupItemStackOrigin();
        int strength=Enchantments.level(stack,"minecraft:wind_burst");
        if(!stack.is(Items.MACE)||strength<=0||!(projectile.level() instanceof ServerLevel level)
            ||!(projectile.getOwner() instanceof LivingEntity owner)) return;
        var registry=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var wind=registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.WIND_BURST);
        var inUse=new net.minecraft.world.item.enchantment.EnchantedItemInUse(stack,null,owner,broken->{});
        for(var effect:wind.value().getEffects(net.minecraft.world.item.enchantment.EnchantmentEffectComponents.POST_ATTACK))
            if(effect.effect() instanceof net.minecraft.world.item.enchantment.effects.ExplodeEffect)
                effect.effect().apply(level,strength,inUse,owner,victim.position());
    }
    public static ItemStack withoutThrownWindBurst(ItemStack stack) {
        if(!stack.is(Items.MACE))return stack;
        ItemStack copy=stack.copy();
        net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(copy,
            mutable->mutable.removeIf(holder->holder.is(net.minecraft.world.item.enchantment.Enchantments.WIND_BURST)));
        return copy;
    }
    private Combat() {}
}
