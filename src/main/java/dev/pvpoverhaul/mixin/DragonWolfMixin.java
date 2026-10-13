package dev.pvpoverhaul.mixin;

import dev.pvpoverhaul.DragonWolfFlightGoal;
import dev.pvpoverhaul.ModItems;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class DragonWolfMixin {
    @Unique private DragonWolfFlightGoal pvp$flight;
    @Inject(method="registerGoals",at=@At("RETURN"))
    private void pvp$flightGoal(CallbackInfo ci) {
        Wolf wolf=(Wolf)(Object)this;pvp$flight=new DragonWolfFlightGoal(wolf);wolf.getGoalSelector().addGoal(0,pvp$flight);
    }
    @Inject(method="canArmorAbsorb",at=@At("RETURN"),cancellable=true)
    private void pvp$scaleProtection(DamageSource source,CallbackInfoReturnable<Boolean> cir) {
        if(((Wolf)(Object)this).getBodyArmorItem().is(ModItems.DRAGON_SCALE_WOLF_ARMOR) && !source.is(DamageTypeTags.BYPASSES_WOLF_ARMOR)) cir.setReturnValue(true);
    }
    @Inject(method="addAdditionalSaveData",at=@At("RETURN"))
    private void pvp$saveFlight(ValueOutput output,CallbackInfo ci) {
        output.putBoolean("pvpoverhaul_flight_gravity",pvp$flight!=null && pvp$flight.controlsGravity());
    }
    @Inject(method="readAdditionalSaveData",at=@At("RETURN"))
    private void pvp$resetSavedFlight(ValueInput input,CallbackInfo ci) {
        if(input.getBooleanOr("pvpoverhaul_flight_gravity",false)) ((Wolf)(Object)this).setNoGravity(false);
    }
}
