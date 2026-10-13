package dev.pvpoverhaul.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pvpoverhaul.client.WeaponProjectileRenderState;
import dev.pvpoverhaul.GrapplingArrows;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reuse Minecraft's actual fishing-bobber sprite and line submission. */
@Mixin(ArrowRenderer.class)
public abstract class GrapplingArrowRendererMixin extends EntityRenderer<AbstractArrow,ArrowRenderState> {
    @Unique private FishingHookRenderer pvp$bobber;
    protected GrapplingArrowRendererMixin(EntityRendererProvider.Context context) { super(context); }
    @Override protected boolean affectedByCulling(AbstractArrow entity) { return !((GrapplingArrows.Visual)entity).pvp$isGrapplingArrow() && super.affectedByCulling(entity); }
    @Inject(method="<init>",at=@At("RETURN"))
    private void pvp$renderer(EntityRendererProvider.Context context,CallbackInfo ci) { pvp$bobber=new FishingHookRenderer(context); }
    @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/ArrowRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("HEAD"),cancellable=true)
    private void pvp$renderBobber(ArrowRenderState state,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera,CallbackInfo ci) {
        if(state instanceof WeaponProjectileRenderState.Grapple grapple && grapple.grappling) { pvp$bobber.submit(grapple.bobber,poses,collector,camera);ci.cancel(); }
    }
}
