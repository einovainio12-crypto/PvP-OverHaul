package dev.pvpoverhaul;

import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class TierBowItem extends BowItem {
    public final Tier tier;
    public TierBowItem(Properties properties,Tier tier) { super(properties);this.tier=tier; }
    @Override public Predicate<ItemStack> getAllSupportedProjectiles() { return s->Ammo.supported(tier,s,false); }
    @Override protected Projectile createProjectile(Level level,LivingEntity shooter,ItemStack weapon,ItemStack ammo,boolean crit) {
        Projectile p=super.createProjectile(level,shooter,weapon,ammo,crit);
        Combat.adjustProjectile(p,shooter,tier); return p;
    }
    @Override protected void shootProjectile(LivingEntity shooter,Projectile projectile,int index,float power,float uncertainty,float angle,LivingEntity target) {
        if (!Skills.aim(shooter,projectile,power,angle))
            super.shootProjectile(shooter,projectile,index,power,Combat.uncertainty(projectile,shooter),angle,target);
    }
    @Override public boolean releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining) {
        // Gold reaches the same full-charge power in 14 ticks instead of 20; never changes other bows.
        if (tier==Tier.GOLD) {
            int held=getUseDuration(stack,entity)-remaining;
            remaining=getUseDuration(stack,entity)-Math.round(held*(20f/14f));
        }
        return super.releaseUsing(stack,level,entity,remaining);
    }
}
