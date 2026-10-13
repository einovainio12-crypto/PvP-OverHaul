package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.ShieldStuns;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BlocksAttacks.class)
public abstract class ShieldStunMixin {
    @Inject(method="disable",at=@At("HEAD"),cancellable=true)
    private void pvp$resistStun(ServerLevel level,LivingEntity defender,float seconds,ItemStack shield,CallbackInfo ci) {
        if(ShieldStuns.resist(defender,shield,seconds)) ci.cancel();
    }
}
