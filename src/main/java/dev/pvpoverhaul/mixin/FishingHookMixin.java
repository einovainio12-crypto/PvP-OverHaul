package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Let the native fishing hook stay attached while its owner holds a flail. */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @Inject(method="shouldStopFishing",at=@At("HEAD"),cancellable=true)
    private void pvp$flailRod(Player owner,CallbackInfoReturnable<Boolean> cir) {
        FishingHook hook=(FishingHook)(Object)this;
        if(owner.canInteractWithLevel() && hook.distanceToSqr(owner)<=1024
            && ("flail".equals(ModItems.KINDS.get(owner.getMainHandItem().getItem()))
            || "flail".equals(ModItems.KINDS.get(owner.getOffhandItem().getItem())))) cir.setReturnValue(false);
    }
}
