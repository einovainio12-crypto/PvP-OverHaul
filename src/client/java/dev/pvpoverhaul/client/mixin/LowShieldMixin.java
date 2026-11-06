package dev.pvpoverhaul.client.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.state.level.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class LowShieldMixin {
 @Inject(method="submitArmWithItem",at=@At("HEAD"))
 private void pvp$lower(PlayerRenderState player,FirstPersonHandsAndItemsRenderState state,float partial,float pitch,InteractionHand hand,float swing,ItemStack stack,float equip,PoseStack poses,SubmitNodeCollector collector,int light,CallbackInfo ci){
  poses.pushPose();if(stack.has(DataComponents.BLOCKS_ATTACKS))poses.translate(0,-.28,0);
 }
 @Inject(method="submitArmWithItem",at=@At("RETURN"))
 private void pvp$restore(PlayerRenderState player,FirstPersonHandsAndItemsRenderState state,float partial,float pitch,InteractionHand hand,float swing,ItemStack stack,float equip,PoseStack poses,SubmitNodeCollector collector,int light,CallbackInfo ci){poses.popPose();}
}
