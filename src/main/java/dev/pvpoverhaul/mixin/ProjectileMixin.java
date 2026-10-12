package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Projectile.class)
public abstract class ProjectileMixin {
    @ModifyVariable(method="shoot",at=@At("HEAD"),argsOnly=true,ordinal=1)
    private float pvp$materialAccuracy(float original) {
        if ((Object)this instanceof AbstractArrow arrow && arrow.getPickupItemStackOrigin().getItem() instanceof TierArrowItem item)
            return item.ammo.uncertainty*(arrow.getOwner() instanceof LivingEntity owner && Skills.deadEyeActive(owner)?0.15f:1f);
        return original;
    }
}
