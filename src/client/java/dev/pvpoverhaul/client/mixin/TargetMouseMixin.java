package dev.pvpoverhaul.client.mixin;
import dev.pvpoverhaul.client.TargetLockClient;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MouseHandler.class)
public abstract class TargetMouseMixin {
 @Inject(method="turnPlayer",at=@At("TAIL"))
 private void pvp$track(double time,CallbackInfo ci){TargetLockClient.track();}
}
