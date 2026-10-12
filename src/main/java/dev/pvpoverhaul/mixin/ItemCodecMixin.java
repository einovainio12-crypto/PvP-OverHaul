package dev.pvpoverhaul.mixin;
import com.mojang.serialization.Codec;
import dev.pvpoverhaul.LegacySpears;
import net.minecraft.core.*;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Item.class)
public abstract class ItemCodecMixin {
    @Redirect(method="<clinit>",at=@At(value="INVOKE",target="Lnet/minecraft/core/DefaultedRegistry;holderByNameCodec()Lcom/mojang/serialization/Codec;"))
    private static Codec<Holder<Item>> pvp$legacySpears(DefaultedRegistry<Item> registry) {
        return LegacySpears.codec(registry.holderByNameCodec());
    }
}
