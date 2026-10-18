package dev.pvpoverhaul.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.pvpoverhaul.ThrownItemVisual;
import dev.pvpoverhaul.client.WeaponProjectileRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Use the synchronized original item model; ordinary tridents retain their native renderer. */
@Mixin(ThrownTridentRenderer.class)
public abstract class ThrownWeaponRendererMixin extends EntityRenderer<ThrownTrident,ThrownTridentRenderState> {
    @Unique private ItemModelResolver pvp$resolver;
    protected ThrownWeaponRendererMixin(EntityRendererProvider.Context context) { super(context); }
    @Inject(method="<init>",at=@At("RETURN"))
    private void pvp$resolver(EntityRendererProvider.Context context,CallbackInfo ci) { pvp$resolver=context.getItemModelResolver(); }
    @Inject(method="createRenderState()Lnet/minecraft/client/renderer/entity/state/ThrownTridentRenderState;",at=@At("RETURN"),cancellable=true)
    private void pvp$itemState(CallbackInfoReturnable<ThrownTridentRenderState> cir) { cir.setReturnValue(new WeaponProjectileRenderState()); }
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/projectile/arrow/ThrownTrident;Lnet/minecraft/client/renderer/entity/state/ThrownTridentRenderState;F)V",at=@At("RETURN"))
    private void pvp$itemModel(ThrownTrident entity,ThrownTridentRenderState state,float partialTicks,CallbackInfo ci) {
        if(state instanceof WeaponProjectileRenderState custom) {
            var stack=((ThrownItemVisual)entity).pvp$getVisualItem();custom.customWeapon=!stack.isEmpty();
            custom.modelPitchOffset=ThrownItemVisual.modelPitchOffset(stack);
            custom.modelScale=ThrownItemVisual.modelScale(stack);
            custom.spinDegrees=ThrownItemVisual.spins(stack) && (entity.isNoPhysics() || entity.getDeltaMovement().lengthSqr()>1e-5)?state.ageInTicks*36f:0;
            pvp$resolver.updateForNonLiving(custom.weapon,ThrownItemVisual.heldProjectileModel(stack),ItemDisplayContext.NONE,entity);
        }
    }
    @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/ThrownTridentRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("HEAD"),cancellable=true)
    private void pvp$renderWeapon(ThrownTridentRenderState state,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera,CallbackInfo ci) {
        if(!(state instanceof WeaponProjectileRenderState custom) || !custom.customWeapon) return;
        poses.pushPose();poses.rotateDegrees(Axis.YP,state.yRot-90);poses.rotateDegrees(Axis.ZP,state.xRot+custom.modelPitchOffset+custom.spinDegrees);
        poses.scale(custom.modelScale,custom.modelScale,custom.modelScale);
        custom.weapon.submit(poses,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,state.outlineColor);
        poses.popPose();super.submit(state,poses,collector,camera);ci.cancel();
    }
}
