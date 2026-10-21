package dev.pvpoverhaul;

import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class TierBowItem extends BowItem {
    public final Tier tier;
    public TierBowItem(Properties properties,Tier tier) { super(properties);this.tier=tier; }
    public boolean fireBurst(net.minecraft.server.level.ServerPlayer player,net.minecraft.world.InteractionHand hand,ItemStack weapon,LivingEntity target) {
        ItemStack ammo;
        ItemStack available=player.getProjectile(weapon);
        if(available.isEmpty())return false;
        var drawn=draw(weapon,available,player);
        if(drawn.isEmpty())return false;
        ammo=drawn.getFirst();
        if(ammo.isEmpty())return false;
        shoot((net.minecraft.server.level.ServerLevel)player.level(),player,hand,weapon,java.util.List.of(ammo),3.15f,0,true,target);
        return true;
    }
    @Override public Predicate<ItemStack> getAllSupportedProjectiles() { return s->Ammo.supported(tier,s,false); }
    @Override protected Projectile createProjectile(Level level,LivingEntity shooter,ItemStack weapon,ItemStack ammo,boolean crit) {
        Projectile p=super.createProjectile(level,shooter,weapon,ammo,crit);
        Combat.adjustProjectile(p,shooter,tier.bowDamageMultiplier()); return p;
    }
    @Override protected void shootProjectile(LivingEntity shooter,Projectile projectile,int index,float power,float uncertainty,float angle,LivingEntity target) {
        if (!Skills.aim(shooter,projectile,power,angle))
            super.shootProjectile(shooter,projectile,index,power,Combat.uncertainty(projectile,shooter),angle,target);
    }
    @Override public boolean releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining) {
        int held=getUseDuration(stack,entity)-remaining;
        int scaledHeld=Math.round(held*(20f/tier.bowDrawTicks()));
        boolean fullCharge=BowItem.getPowerForTime(scaledHeld)>=1;
        boolean fired=super.releaseUsing(stack,level,entity,getUseDuration(stack,entity)-scaledHeld);
        if (fired && fullCharge && !level.isClientSide()) Skills.finishShot(entity);
        return fired;
    }
}
