package dev.pvpoverhaul;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.*;
import net.minecraft.world.item.*;
/** Optional Rose Gold Equipment material; registers no ingots, blocks or armor. */
public final class RoseGold {
 public static final TagKey<Item> REPAIR=TagKey.create(Registries.ITEM,PvpOverhaul.id("rose_gold_repair"));
 public static final ToolMaterial TOOL=new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL,384,12f,2.5f,25,REPAIR);
 public static boolean isIngot(ItemStack stack){return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("rosegoldequipment:rose_gold_ingot");}
 private RoseGold(){}
}
