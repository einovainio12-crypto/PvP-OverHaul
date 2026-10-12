package dev.pvpoverhaul.client.mixin;
import dev.pvpoverhaul.client.PvpOverhaulClient;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method="calculateFov",at=@At("RETURN"),cancellable=true)
    private void pvp$zoom(float partialTicks,CallbackInfoReturnable<Float> cir) {
        if(PvpOverhaulClient.zooming()) cir.setReturnValue(cir.getReturnValue()*0.4f);
    }
}
