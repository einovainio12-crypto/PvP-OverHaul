package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Combat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
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
