package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Combat;
import net.minecraft.world.entity.projectile.arrow.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin {
    @ModifyConstant(method="onHitEntity",constant=@Constant(floatValue=8f))
    private float pvp$thrownDamage(float vanilla) { return Combat.thrownDamage((AbstractArrow)(Object)this,vanilla); }
}
