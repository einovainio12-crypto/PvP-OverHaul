package dev.pvpoverhaul.mixin;

import dev.pvpoverhaul.ShieldParry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityParryMixin {
    @Inject(method="tick",at=@At("HEAD"))
    private void pvp$trackShieldParry(CallbackInfo ci) {
        ShieldParry.track((LivingEntity)(Object)this);
    }

    @Inject(method="hurtServer",at=@At("HEAD"),cancellable=true)
    private void pvp$parryDamage(ServerLevel level,DamageSource source,float damage,CallbackInfoReturnable<Boolean> cir) {
        if(ShieldParry.tryParry(level,(LivingEntity)(Object)this,source,damage)) cir.setReturnValue(false);
    }
}
