package dev.pvpoverhaul.mixin;

import dev.pvpoverhaul.Balance;
import dev.pvpoverhaul.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Run the same Aerodynamics physics on the controlling client and the server. */
@Mixin(LivingEntity.class)
public abstract class GlidingMixin {
    @Shadow protected abstract double getEffectiveGravity();
    private int pvp$aerodynamics() {
        LivingEntity owner=(LivingEntity)(Object)this;
        return Math.min(2,Enchantments.level(owner,owner.getItemBySlot(EquipmentSlot.CHEST),"aerodynamics"));
    }
    @Redirect(method="updateFallFlyingMovement",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getEffectiveGravity()D"))
    private double pvp$glideGravity(LivingEntity owner) {
        return getEffectiveGravity()*Balance.glideGravityMultiplier(pvp$aerodynamics());
    }
    @ModifyArgs(method="updateFallFlyingMovement",at=@At(value="INVOKE",target="Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private void pvp$glideDrag(Args args) {
        int level=pvp$aerodynamics();
        if(level>0) { args.set(0,Balance.glideHorizontalDrag(level));args.set(2,Balance.glideHorizontalDrag(level)); }
    }
}
