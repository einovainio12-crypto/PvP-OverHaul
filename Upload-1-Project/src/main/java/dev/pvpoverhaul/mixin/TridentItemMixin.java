package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Combat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(TridentItem.class)
public abstract class TridentItemMixin {
    @Redirect(method={"use","releaseUsing"},at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private boolean pvp$depthStrider(Player player) { return player.isInWaterOrRain() || Combat.depthStriderRiptide(player); }
}
