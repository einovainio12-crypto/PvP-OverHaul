package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(LivingEntity.class)
public abstract class FirstJumpMixin {
    @ModifyVariable(method="jumpFromGround",at=@At("STORE"),ordinal=0)
    private float pvp$higherFirstJump(float power) {
        if(!((Object)this instanceof Player player)) return power;
        var boots=player.getItemBySlot(EquipmentSlot.FEET);
        return Enchantments.level(boots,"speed")>0?power:power*Balance.firstJumpMultiplier(Enchantments.level(boots,"double_jump"));
    }
}
