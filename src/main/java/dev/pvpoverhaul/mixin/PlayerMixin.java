package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Combat;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Player.class)
public abstract class PlayerMixin {
    @ModifyArg(method="attack",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),index=1)
    private float pvp$knifeBonus(float damage) { return Combat.knifeDamage((Player)(Object)this,damage); }
}
