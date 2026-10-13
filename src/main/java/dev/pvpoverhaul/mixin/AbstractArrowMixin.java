package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Combat;
import dev.pvpoverhaul.FireworkArrows;
import dev.pvpoverhaul.GrapplingArrows;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin implements GrapplingArrows.Visual {
    @Unique private static final EntityDataAccessor<Boolean> PVP_GRAPPLING=SynchedEntityData.defineId(AbstractArrow.class,EntityDataSerializers.BOOLEAN);
    @Inject(method="defineSynchedData",at=@At("RETURN"))
    private void pvp$grappleData(SynchedEntityData.Builder builder,CallbackInfo ci) { builder.define(PVP_GRAPPLING,false); }
    @Inject(method="<init>",at=@At("RETURN"))
    private void pvp$initialGrapple(CallbackInfo ci) { pvp$syncGrapple(); }
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void pvp$grappleTick(CallbackInfo ci) {
        AbstractArrow arrow=(AbstractArrow)(Object)this;pvp$syncGrapple();
        if(!arrow.level().isClientSide() && GrapplingArrows.isGrappling(arrow)
            && ((arrow.isNoPhysics() && !GrapplingArrows.tracked(arrow)) || (!arrow.isNoPhysics() && arrow.tickCount>200))) { arrow.discard();ci.cancel(); }
    }
    @Inject(method="addAdditionalSaveData",at=@At("RETURN"))
    private void pvp$saveGrapple(ValueOutput output,CallbackInfo ci) { output.putBoolean("pvpoverhaul_grapple_anchor",GrapplingArrows.tracked((AbstractArrow)(Object)this)); }
    @Inject(method="readAdditionalSaveData",at=@At("RETURN"))
    private void pvp$clearSavedAnchor(ValueInput input,CallbackInfo ci) {
        pvp$syncGrapple();if(input.getBooleanOr("pvpoverhaul_grapple_anchor",false)) ((AbstractArrow)(Object)this).discard();
    }
    @Unique private void pvp$syncGrapple() {
        AbstractArrow arrow=(AbstractArrow)(Object)this;
        if(!arrow.level().isClientSide()) arrow.getEntityData().set(PVP_GRAPPLING,GrapplingArrows.isGrappling(arrow));
    }
    @Override public boolean pvp$isGrapplingArrow() { return ((AbstractArrow)(Object)this).getEntityData().get(PVP_GRAPPLING); }
    @Inject(method="canHitEntity",at=@At("HEAD"),cancellable=true)
    private void pvp$grappleAnyEntity(Entity target,CallbackInfoReturnable<Boolean> cir) {
        AbstractArrow arrow=(AbstractArrow)(Object)this;
        if(GrapplingArrows.isGrappling(arrow)) {
            Entity owner=arrow.getOwner();
            cir.setReturnValue(target!=arrow && GrapplingArrows.canAttach(owner,target)
                && (owner==null || target.getRootVehicle()!=owner.getRootVehicle()));
        }
    }
    @Redirect(method="canHitEntity",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;canHarmPlayer(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean pvp$grappleTeammates(Player owner,Player target) { return GrapplingArrows.isGrappling((AbstractArrow)(Object)this) || owner.canHarmPlayer(target); }
    @Inject(method="onHitEntity",at=@At("HEAD"),cancellable=true)
    private void pvp$fireworkEntity(EntityHitResult hit,CallbackInfo ci) {
        if(GrapplingArrows.impact((AbstractArrow)(Object)this,hit) || FireworkArrows.impact((AbstractArrow)(Object)this,hit)) ci.cancel();
    }
    @Inject(method="onHitBlock",at=@At("HEAD"),cancellable=true)
    private void pvp$fireworkBlock(BlockHitResult hit,CallbackInfo ci) {
        if(GrapplingArrows.impact((AbstractArrow)(Object)this,hit) || FireworkArrows.impact((AbstractArrow)(Object)this,hit)) ci.cancel();
    }
    @Redirect(method="onHitEntity",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean pvp$hit(Entity target,DamageSource source,float damage) {
        boolean hit=target.hurtOrSimulate(source,damage);
        AbstractArrow arrow=(AbstractArrow)(Object)this;
        EntityHitResult result=new EntityHitResult(target);
        // Resolve the shield block before disabling it, so the stun does not bypass the blocked hit.
        Combat.arrowHit(arrow,result);
        if(hit) Combat.arrowAfterHit(arrow,result);
        return hit;
    }
    @Redirect(method="onHitEntity",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    private void pvp$fireDuration(Entity target,float vanillaSeconds) {
        AbstractArrow arrow=(AbstractArrow)(Object)this;
        if(arrow.getPickupItemStackOrigin().getItem() instanceof dev.pvpoverhaul.TierArrowItem item && Combat.fireSeconds(item.ammo)>0)
            target.igniteForSeconds(Combat.fireSeconds(item.ammo));
        else target.igniteForSeconds(vanillaSeconds);
    }
}
