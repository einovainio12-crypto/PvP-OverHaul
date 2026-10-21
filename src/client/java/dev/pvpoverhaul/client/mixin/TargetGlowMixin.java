package dev.pvpoverhaul.client.mixin;
import dev.pvpoverhaul.client.TargetLockClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Minecraft.class)
public abstract class TargetGlowMixin {
 @Inject(method="shouldEntityAppearGlowing",at=@At("RETURN"),cancellable=true)
 private void pvp$highlight(Entity entity,CallbackInfoReturnable<Boolean> cir){if(entity==TargetLockClient.target())cir.setReturnValue(true);}
}
