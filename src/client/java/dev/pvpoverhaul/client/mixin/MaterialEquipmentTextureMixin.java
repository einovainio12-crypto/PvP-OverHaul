package dev.pvpoverhaul.client.mixin;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EquipmentClientInfo.Layer.class)
public abstract class MaterialEquipmentTextureMixin {
 @Inject(method="getTextureLocation",at=@At("RETURN"))
 private void pvp$palette(EquipmentClientInfo.LayerType type,CallbackInfoReturnable<Identifier> cir){dev.pvpoverhaul.client.MaterialTextures.ensure(cir.getReturnValue());}
}
