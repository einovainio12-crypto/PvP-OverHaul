package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Quivers;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ProjectileWeaponItem.class)
public abstract class QuiverDrawMixin {
    @Inject(method="draw",at=@At("HEAD"),cancellable=true)
    private static void pvp$validateAmmo(ItemStack weapon,ItemStack ammo,LivingEntity shooter,CallbackInfoReturnable<List<ItemStack>> cir) {
        if(!Quivers.validDraw(ammo,shooter)) cir.setReturnValue(List.of());
    }
    @Inject(method="draw",at=@At("RETURN"),cancellable=true)
    private static void pvp$consumeAmmo(ItemStack weapon,ItemStack ammo,LivingEntity shooter,CallbackInfoReturnable<List<ItemStack>> cir) {
        if(!Quivers.finishDraw(ammo)) cir.setReturnValue(List.of());
    }
}
