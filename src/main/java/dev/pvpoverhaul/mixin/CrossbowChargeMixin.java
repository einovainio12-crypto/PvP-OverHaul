package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(CrossbowItem.class)
public abstract class CrossbowChargeMixin {
    @ModifyConstant(method="getChargeDuration",constant=@Constant(floatValue=1.25f))
    private static float pvp$tierCharge(float original,ItemStack weapon,LivingEntity user) {
        return weapon.getItem() instanceof TierCrossbowItem crossbow?Balance.crossbowDrawTicks(crossbow.tier.rank)/20f:original;
    }
    @Inject(method="getChargeDuration",at=@At("RETURN"),cancellable=true)
    private static void pvp$minimumCharge(ItemStack weapon,LivingEntity user,CallbackInfoReturnable<Integer> cir) {
        if(weapon.getItem() instanceof TierCrossbowItem) cir.setReturnValue(Math.max(3,cir.getReturnValue()));
    }
}
