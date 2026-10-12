package dev.pvpoverhaul;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class Combat {
    public static float uncertainty(Projectile projectile,LivingEntity shooter) {
        float value=1;
        if (projectile instanceof AbstractArrow arrow && arrow.getPickupItemStackOrigin().getItem() instanceof TierArrowItem item)
            value=item.ammo.uncertainty;
        return projectile instanceof AbstractArrow arrow && arrow.isCritArrow() && Skills.deadEyeActive(shooter,arrow.getWeaponItem()) ? value*Balance.DEAD_EYE_SPREAD : value;
    }
    public static void adjustProjectile(Projectile p,LivingEntity shooter,double multiplier) {
        if (!(p instanceof AbstractArrow arrow)) return;
        arrow.setBaseDamage(((dev.pvpoverhaul.mixin.AbstractArrowAccessor)arrow).pvp$getBaseDamage()*multiplier*(arrow.isCritArrow() && Skills.deadEyeActive(shooter,arrow.getWeaponItem())?Balance.DEAD_EYE_DAMAGE:1));
        if (arrow.getPickupItemStackOrigin().getItem() instanceof TierArrowItem item && (item.ammo==Ammo.BLAZE || item.ammo==Ammo.FIRE_CHARGE)) arrow.igniteForSeconds(5);
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
            if (item.ammo==Ammo.BLAZE || item.ammo==Ammo.FIRE_CHARGE) living.igniteForSeconds(4);
            if (item.ammo==Ammo.BREEZE) {
                Vec3 push=arrow.getDeltaMovement().normalize().scale(0.8);
                living.push(push.x,0.45,push.z);living.syncVelocity=true;
            }
        }
    }
    public static float thrownDamage(AbstractArrow projectile,float vanilla) {
        ItemStack weapon=projectile.getPickupItemStackOrigin();
        Tier t=ModItems.TIERS.get(weapon.getItem());
        if (ModItems.isKnife(weapon)) return 3+t.rank;
        if (ModItems.isSpear(weapon)) return weapon.is(Items.STONE_SPEAR)?6:5+(t==null?0:t.rank);
        if (ModItems.isBattleaxe(weapon)) return 8+t.rank;
        if (weapon.is(net.minecraft.tags.ItemTags.AXES)) return (float)weapon.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).compute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND);
        return vanilla;
    }
    private Combat() {}
}
