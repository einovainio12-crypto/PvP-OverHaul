package dev.pvpoverhaul.mixin;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(ThrownTrident.class)
public interface TridentDataAccessor {
    @Accessor("ID_LOYALTY") static EntityDataAccessor<Byte> pvp$loyalty(){throw new AssertionError();}
    @Accessor("ID_FOIL") static EntityDataAccessor<Boolean> pvp$foil(){throw new AssertionError();}
    @Invoker("getLoyaltyFromItem") byte pvp$loyaltyFrom(ItemStack stack);
}
