package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Quivers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Player.class)
public abstract class QuiverPlayerMixin {
    @Inject(method="getProjectile",at=@At("HEAD"),cancellable=true)
    private void pvp$quiverAmmo(ItemStack weapon,CallbackInfoReturnable<ItemStack> cir) {
        ItemStack arrow=Quivers.ammunition((Player)(Object)this,weapon);
        if(!arrow.isEmpty()) cir.setReturnValue(arrow);
    }
}
