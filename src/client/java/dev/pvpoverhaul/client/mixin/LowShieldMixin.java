package dev.pvpoverhaul.client.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.state.level.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class LowShieldMixin {
 /** Cancellation by another renderer must still unwind our matrix frame. */
 @WrapMethod(method="submitArmWithItem")
 private void pvp$lowerShield(PlayerRenderState player,FirstPersonHandsAndItemsRenderState state,float partial,float pitch,InteractionHand hand,float swing,ItemStack stack,float equip,PoseStack poses,SubmitNodeCollector collector,int light,Operation<Void> original){
  dev.pvpoverhaul.client.ShieldPose.render(poses,stack.has(DataComponents.BLOCKS_ATTACKS),
   ()->original.call(player,state,partial,pitch,hand,swing,stack,equip,poses,collector,light));
 }
}
