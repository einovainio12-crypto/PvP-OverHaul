package dev.pvpoverhaul.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pvpoverhaul.ModItems;
import dev.pvpoverhaul.client.DragonWolfArmorMesh;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Select the custom mesh for this armor only; keep native equipment/glint/crack rendering. */
@Mixin(WolfArmorLayer.class)
public abstract class WolfArmorLayerMixin {
    @Shadow @Final private WolfModel adultModel;
    @Unique private WolfModel pvp$dragon;
    @Unique private boolean pvp$customArmor;
    @Inject(method="submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/WolfRenderState;FF)V",at=@At("HEAD"))
    private void pvp$chooseMesh(PoseStack poses,SubmitNodeCollector collector,int light,WolfRenderState state,float yaw,float pitch,CallbackInfo ci) {
        pvp$customArmor=state.bodyArmorItem.is(ModItems.DRAGON_SCALE_WOLF_ARMOR);
    }
    @Redirect(method="submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/WolfRenderState;FF)V",at=@At(value="FIELD",target="Lnet/minecraft/client/renderer/entity/layers/WolfArmorLayer;adultModel:Lnet/minecraft/client/model/animal/wolf/WolfModel;"),require=2)
    private WolfModel pvp$armorMesh(WolfArmorLayer layer) {
        if(!pvp$customArmor) return adultModel;
        if(pvp$dragon==null) pvp$dragon=DragonWolfArmorMesh.load();
        return pvp$dragon;
    }
}
