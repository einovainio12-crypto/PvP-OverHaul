package dev.pvpoverhaul;

import java.util.Map;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

/** Read removed item IDs as their native replacements; saving writes the canonical native ID. */
public final class LegacySpears {
    private static final Map<String,String> IDS=Map.of(
        "pvpoverhaul:wood_spear","minecraft:wooden_spear",
        "pvpoverhaul:copper_spear","minecraft:copper_spear",
        "pvpoverhaul:iron_spear","minecraft:iron_spear",
        "pvpoverhaul:gold_spear","minecraft:golden_spear",
        "pvpoverhaul:diamond_spear","minecraft:diamond_spear",
        "pvpoverhaul:netherite_spear","minecraft:netherite_spear",
        "pvpoverhaul:stone_arrow","minecraft:arrow",
        "pvpoverhaul:flint_arrow","minecraft:arrow",
        "pvpoverhaul:wood_bow","minecraft:bow",
        "pvpoverhaul:wood_crossbow","minecraft:crossbow");
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
