package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Skills;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.ApplyEntityImpulse;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ApplyEntityImpulse.class)
public abstract class LungePoleJumpMixin {
    @Redirect(method="apply",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;addDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void pvp$downwardLunge(Entity entity,Vec3 impulse,ServerLevel level,int enchantmentLevel,EnchantedItemInUse item,Entity target,Vec3 origin) {
        ApplyEntityImpulse effect=(ApplyEntityImpulse)(Object)this;
        if(entity instanceof ServerPlayer player && item.owner()==player && effect.coordinateScale().equals(new Vec3(1,0,1)) && effect.direction().equals(new Vec3(0,0,1)))
            impulse=Skills.lungePoleImpulse(player,item.itemStack(),impulse);
        entity.addDeltaMovement(impulse);
    }
}
