package dev.pvpoverhaul;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

/** Owned, standing wolves follow their owner's actual flight without teleporting through terrain. */
public final class DragonWolfFlightGoal extends Goal {
    private final Wolf wolf;
    private LivingEntity owner;
    private boolean active,previousNoGravity;
    private int landingTicks;
    private Vec3 previousOwnerPosition;
    public DragonWolfFlightGoal(Wolf wolf) { this.wolf=wolf;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP)); }
    private boolean equippedAndAvailable() {
        owner=wolf.getOwner();
        return wolf.isAlive() && wolf.isTame() && !wolf.isBaby() && !wolf.isOrderedToSit() && !wolf.isPassenger()
            && !wolf.isLeashed() && wolf.getBodyArmorItem().is(ModItems.DRAGON_SCALE_WOLF_ARMOR)
            && owner!=null && owner.isAlive() && !owner.isRemoved() && owner.level()==wolf.level();
    }
    @Override public boolean canUse() { return equippedAndAvailable() && owner.isFallFlying() && wolf.distanceToSqr(owner)<=32*32; }
    @Override public boolean canContinueToUse() {
        return equippedAndAvailable() && wolf.distanceToSqr(owner)<=128*128
            && (owner.isFallFlying() || (active && !wolf.onGround() && landingTicks<200));
    }
    @Override public void start() {
        active=true;landingTicks=0;previousNoGravity=wolf.isNoGravity();previousOwnerPosition=owner.position();
        wolf.setNoGravity(true);wolf.getNavigation().stop();wolf.resetFallDistance();
    }
    @Override public void stop() {
        if(!active) return;
        active=false;wolf.setNoGravity(previousNoGravity);wolf.resetFallDistance();
        if(!wolf.onGround()) wolf.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,200,0,false,false));
        owner=null;previousOwnerPosition=null;
    }
    public boolean controlsGravity() { return active && !previousNoGravity; }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void tick() {
        if(owner==null) return;
        wolf.getNavigation().stop();wolf.setNoGravity(true);wolf.resetFallDistance();
        Vec3 facing=new Vec3(owner.getLookAngle().x,0,owner.getLookAngle().z).normalize();
        boolean flying=owner.isFallFlying();
        Vec3 target=owner.position().subtract(facing.scale(2.5)).add(0,flying?0.6:0,0);
        Vec3 observed=previousOwnerPosition==null?Vec3.ZERO:owner.position().subtract(previousOwnerPosition);
        previousOwnerPosition=owner.position();
        if(observed.lengthSqr()>100) observed=Vec3.ZERO;
        Vec3 motion=target.subtract(wolf.position()).scale(flying?0.20:0.12).add(flying?observed:Vec3.ZERO);
        double maximum=flying?4.0:0.8;
        if(motion.lengthSqr()>maximum*maximum) motion=motion.normalize().scale(maximum);
        if(!flying) { landingTicks++;motion=new Vec3(motion.x,Math.max(-0.30,motion.y),motion.z); }
        else landingTicks=0;
        wolf.setDeltaMovement(motion);wolf.syncVelocity=true;wolf.needsSync=true;
        wolf.getLookControl().setLookAt(owner,30,30);
    }
}
