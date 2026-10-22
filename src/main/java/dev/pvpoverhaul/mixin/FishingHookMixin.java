package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Let the native fishing hook stay attached while its owner holds a flail. */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin implements dev.pvpoverhaul.WeaponEntities.FlailHookVisual {
    @org.spongepowered.asm.mixin.Unique private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.world.item.ItemStack> PVP_FLAIL=
        net.minecraft.network.syncher.SynchedEntityData.defineId(FishingHook.class,net.minecraft.network.syncher.EntityDataSerializers.ITEM_STACK);
    @Inject(method="defineSynchedData",at=@At("RETURN"))
    private void pvp$defineHead(net.minecraft.network.syncher.SynchedEntityData.Builder builder,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        builder.define(PVP_FLAIL,net.minecraft.world.item.ItemStack.EMPTY);
    }
    public net.minecraft.world.item.ItemStack pvp$getFlail() { return ((FishingHook)(Object)this).getEntityData().get(PVP_FLAIL); }
    public void pvp$setFlail(net.minecraft.world.item.ItemStack stack) { ((FishingHook)(Object)this).getEntityData().set(PVP_FLAIL,stack.copyWithCount(1)); }

    @Inject(method="shouldStopFishing",at=@At("HEAD"),cancellable=true)
    private void pvp$flailRod(Player owner,CallbackInfoReturnable<Boolean> cir) {
        FishingHook hook=(FishingHook)(Object)this;
        if(owner.canInteractWithLevel() && hook.distanceToSqr(owner)<=1024
            && ("flail".equals(ModItems.KINDS.get(owner.getMainHandItem().getItem()))
            || "flail".equals(ModItems.KINDS.get(owner.getOffhandItem().getItem())))) cir.setReturnValue(false);
    }
}
