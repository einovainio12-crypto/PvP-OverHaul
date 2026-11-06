package dev.pvpoverhaul;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;

/** Occasionally vary existing naturally spawned armor; never add armor to unarmored slots. */
public final class ArmoredFoesCompat {
 public static void onSpawn(Mob mob,EntitySpawnReason reason){
  if(!FabricLoader.getInstance().isModLoaded("armoredfoes") || !(mob instanceof Monster)
    || reason!=EntitySpawnReason.NATURAL && reason!=EntitySpawnReason.CHUNK_GENERATION || mob.getRandom().nextInt(8)!=0)return;
  String namespace="pvpoverhaul",material="rose_gold";
  List<CompatMaterials.Material> candidates=CompatMaterials.MATERIALS.stream().filter(CompatMaterials::active).filter(m->Tier.valueOf(m.id().toUpperCase(Locale.ROOT)).rank<=2).toList();
  if(candidates.isEmpty())return;
  int pick=mob.getRandom().nextInt(candidates.size());
  if(pick<candidates.size()){var m=candidates.get(pick);material=m.id().equals("rose_gold")?"rose_golden":m.id();namespace=m.standard()?m.mod():"pvpoverhaul";}
  String[] kinds={"helmet","chestplate","leggings","boots"};EquipmentSlot[] slots={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
  for(int i=0;i<slots.length;i++){
   var old=mob.getItemBySlot(slots[i]);if(old.isEmpty())continue;
   var oldId=BuiltInRegistries.ITEM.getKey(old.getItem());
   if(!oldId.getNamespace().equals("minecraft") || !oldId.getPath().startsWith("iron_"))continue;
   Item replacement=BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(namespace,material+"_"+kinds[i]));
   if(replacement!=null&&replacement!=Items.AIR)mob.setItemSlot(slots[i],old.transmuteCopy(replacement));
  }
 }
 private ArmoredFoesCompat(){}
}
