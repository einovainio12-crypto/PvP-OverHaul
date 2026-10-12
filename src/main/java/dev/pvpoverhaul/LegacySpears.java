package dev.pvpoverhaul;

import java.util.Map;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

/** Read v0.1's removed IDs as vanilla spears; saving writes the canonical vanilla ID. */
public final class LegacySpears {
    private static final Map<String,String> IDS=Map.of(
        "pvpoverhaul:wood_spear","minecraft:wooden_spear",
        "pvpoverhaul:copper_spear","minecraft:copper_spear",
        "pvpoverhaul:iron_spear","minecraft:iron_spear",
        "pvpoverhaul:gold_spear","minecraft:golden_spear",
        "pvpoverhaul:diamond_spear","minecraft:diamond_spear",
        "pvpoverhaul:netherite_spear","minecraft:netherite_spear");
    public static Codec<Holder<Item>> codec(Codec<Holder<Item>> original) {
        return new Codec<>() {
            @Override public <T> DataResult<Pair<Holder<Item>,T>> decode(DynamicOps<T> ops,T input) {
                String id=ops.getStringValue(input).result().orElse(null);
                String canonical=IDS.get(id==null?"":id);
                return original.decode(ops,canonical==null?input:ops.createString(canonical));
            }
            @Override public <T> DataResult<T> encode(Holder<Item> value,DynamicOps<T> ops,T prefix) {
                return original.encode(value,ops,prefix);
            }
        };
    }
    private LegacySpears() {}
}
