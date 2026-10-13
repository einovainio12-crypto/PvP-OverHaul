package dev.pvpoverhaul.client.mixin;

import dev.pvpoverhaul.GrapplingArrows;
import dev.pvpoverhaul.client.WeaponProjectileRenderState;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.state.TippableArrowRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(TippableArrowRenderer.class)
public abstract class GrapplingArrowStateMixin {
    @Inject(method="createRenderState()Lnet/minecraft/client/renderer/entity/state/TippableArrowRenderState;",at=@At("RETURN"),cancellable=true)
    private void pvp$grappleState(CallbackInfoReturnable<TippableArrowRenderState> cir) { cir.setReturnValue(new WeaponProjectileRenderState.Grapple()); }
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/projectile/arrow/Arrow;Lnet/minecraft/client/renderer/entity/state/TippableArrowRenderState;F)V",at=@At("RETURN"))
    private void pvp$bobber(Arrow entity,TippableArrowRenderState state,float partialTicks,CallbackInfo ci) {
        if(state instanceof WeaponProjectileRenderState.Grapple grapple) {
            grapple.grappling=((GrapplingArrows.Visual)entity).pvp$isGrapplingArrow();
            grapple.bobber.lightCoords=state.lightCoords;grapple.bobber.outlineColor=state.outlineColor;
            grapple.bobber.lineOriginOffset=entity.getOwner() instanceof Player player
                ?player.getRopeHoldPosition(partialTicks).subtract(entity.getPosition(partialTicks).add(0,0.25,0)):new Vec3(0,-0.01,0);
            if(grapple.bobber.lineOriginOffset.lengthSqr()<1e-8 || !Double.isFinite(grapple.bobber.lineOriginOffset.lengthSqr())) grapple.bobber.lineOriginOffset=new Vec3(0,-0.01,0);
        }
    }
}
