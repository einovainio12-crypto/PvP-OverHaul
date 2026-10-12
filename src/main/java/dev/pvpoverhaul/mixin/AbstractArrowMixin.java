package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Combat;
import dev.pvpoverhaul.FireworkArrows;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
    @Inject(method="onHitEntity",at=@At("HEAD"),cancellable=true)
    private void pvp$fireworkEntity(EntityHitResult hit,CallbackInfo ci) {
        if(FireworkArrows.impact((AbstractArrow)(Object)this,hit)) ci.cancel();
    }
    @Inject(method="onHitBlock",at=@At("HEAD"),cancellable=true)
    private void pvp$fireworkBlock(BlockHitResult hit,CallbackInfo ci) {
        if(FireworkArrows.impact((AbstractArrow)(Object)this,hit)) ci.cancel();
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
}
