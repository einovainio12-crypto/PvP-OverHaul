package dev.pvpoverhaul;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;

public final class LootAdditions {
    public static LootPool.Builder dragonScalePool() {
        return LootPool.lootPool().setRolls(Holder.direct(new ConstantValue(1)))
                .when(Holder.direct(LootItemRandomChanceCondition.randomChance(0.35f).build()))
                .add(LootItem.lootTableItem(ModItems.DRAGON_SCALE).apply(
                        SetItemCountFunction.setCount(Holder.direct(new UniformGenerator(
                                Holder.direct(new ConstantValue(1)),Holder.direct(new ConstantValue(3)))))));
    }
    public static void initialize() {
        LootTableEvents.MODIFY.register((key,builder,source,registries)->{
            if(key.equals(BuiltInLootTables.END_CITY_TREASURE) && source.isBuiltin()) builder.withPool(dragonScalePool());
        });
    }
    private LootAdditions() {}
}
