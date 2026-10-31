package dev.pvpoverhaul;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** Native rose-gold equipment; no dependency on the older equipment mod. */
public final class RoseGold {
    public static final TagKey<Item> REPAIR=TagKey.create(Registries.ITEM,PvpOverhaul.id("rose_gold_repair"));
    public static final ToolMaterial TOOL=new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL,384,12f,2.5f,25,REPAIR);
    public static final ArmorMaterial ARMOR=new ArmorMaterial(18,
        Map.of(ArmorType.HELMET,2,ArmorType.CHESTPLATE,6,ArmorType.LEGGINGS,5,ArmorType.BOOTS,2,ArmorType.BODY,5),
        25,ArmorMaterials.GOLD.equipSound(),0f,0f,REPAIR,
        ResourceKey.create(EquipmentAssets.ROOT_ID,PvpOverhaul.id("rose_gold")));
    public static Item INGOT;
    private static Item item(String name,Item.Properties properties) {
        var key=ResourceKey.create(Registries.ITEM,PvpOverhaul.id(name));
        Item item=Registry.register(BuiltInRegistries.ITEM,key,new Item(properties.setId(key)));
        ModItems.ALL.add(item);return item;
    }
    public static void initialize() {
        item("raw_rose_gold",new Item.Properties());
        INGOT=item("rose_gold_ingot",new Item.Properties());
        item("rose_gold_nugget",new Item.Properties());
        item("rose_gold_sword",new Item.Properties().sword(TOOL,3,-2.4f));
        item("rose_gold_pickaxe",new Item.Properties().pickaxe(TOOL,1,-2.8f));
        item("rose_gold_axe",new Item.Properties().axe(TOOL,6,-3.1f));
        item("rose_gold_shovel",new Item.Properties().shovel(TOOL,1.5f,-3f));
        item("rose_gold_hoe",new Item.Properties().hoe(TOOL,-2,-1f));
        for(ArmorType type:new ArmorType[]{ArmorType.HELMET,ArmorType.CHESTPLATE,ArmorType.LEGGINGS,ArmorType.BOOTS})
            item("rose_gold_"+type.getName(),new Item.Properties().humanoidArmor(ARMOR,type));
        for(String name:new String[]{"rose_gold_block","raw_rose_gold_block"}) {
            var key=ResourceKey.create(Registries.BLOCK,PvpOverhaul.id(name));
            Block block=Registry.register(BuiltInRegistries.BLOCK,key,new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PINK).strength(3f,6f).sound(SoundType.METAL).requiresCorrectToolForDrops().setId(key)));
            var itemKey=ResourceKey.create(Registries.ITEM,PvpOverhaul.id(name));
            Item blockItem=Registry.register(BuiltInRegistries.ITEM,itemKey,new BlockItem(block,new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
            ModItems.ALL.add(blockItem);
        }
    }
    private RoseGold() {}
}
