package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Combat;
import dev.pvpoverhaul.ShieldParry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Player.class)
public abstract class PlayerMixin {
    @ModifyArg(method="attack",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),index=1)
    private float pvp$knifeBonus(float damage) { return Combat.knifeDamage((Player)(Object)this,damage); }

    @Inject(method="hurtServer",at=@At("HEAD"),cancellable=true)
    private void pvp$parryDamage(ServerLevel level,DamageSource source,float damage,CallbackInfoReturnable<Boolean> cir) {
        if(ShieldParry.tryParry(level,(Player)(Object)this,source,damage)) cir.setReturnValue(false);
    }
}
