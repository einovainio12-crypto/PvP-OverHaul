package dev.pvpoverhaul;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;

/** Optional material integrations. No external classes or registry-order assumptions. */
public final class CompatMaterials {
 public record Material(String id,String mod,String ingot,boolean standard,boolean externalSpear,boolean externalHorse,boolean fireproof){}
 public static final List<Material> MATERIALS=List.of(
        new Material("brass","create","create:brass_ingot",false,false,false,false),
        new Material("zinc","create","create:zinc_ingot",false,false,false,false),
        new Material("bronze","alloyed","alloyed:bronze_ingot",false,false,false,false),
        new Material("steel","alloyed","alloyed:steel_ingot",true,true,true,false),
        new Material("silver","thesilverage","thesilverage:silver_ingot",true,true,true,false),
        new Material("lead","theleadage","theleadage:lead_ingot",true,true,true,false),
        new Material("siftite","the_sift","the_sift:siftite_ingot",true,true,false,false),
        new Material("netherite_iron","advancednetherite","advancednetherite:netherite_iron_ingot",true,true,false,true),
        new Material("netherite_gold","advancednetherite","advancednetherite:netherite_gold_ingot",true,true,false,true),
        new Material("netherite_emerald","advancednetherite","advancednetherite:netherite_emerald_ingot",true,true,false,true),
        new Material("netherite_diamond","advancednetherite","advancednetherite:netherite_diamond_ingot",true,true,false,true),
        new Material("rose_gold","rosegoldequipment","rosegoldequipment:rose_gold_ingot",true,false,false,false),
        new Material("enderite","enderitemod","enderitemod:enderite_ingot",true,true,true,true));
 public static Material find(String id){return MATERIALS.stream().filter(m->m.id().equals(id)).findFirst().orElse(null);}
 public static boolean active(Material m){return FabricLoader.getInstance().isModLoaded(m.mod());}
 public static boolean visible(String path){for(var m:MATERIALS)if(path.startsWith(m.id()+"_"))return active(m);return true;}
 public static boolean externalSpear(String id){var m=find(id);return m!=null&&m.externalSpear();}
 public static boolean fireproof(String id){var m=find(id);return m!=null&&m.fireproof();}
 public static TagKey<Item> repair(String id){return TagKey.create(Registries.ITEM,PvpOverhaul.id(id+"_repair"));}
 public static ToolMaterial tool(String id,int durability,float speed,float damage,int enchant){return new ToolMaterial(id.startsWith("netherite_")?BlockTags.INCORRECT_FOR_NETHERITE_TOOL:BlockTags.INCORRECT_FOR_IRON_TOOL,durability,speed,damage,enchant,repair(id));}
 private static void add(String name,Item.Properties p){var key=ResourceKey.create(Registries.ITEM,PvpOverhaul.id(name));Item i=Registry.register(BuiltInRegistries.ITEM,key,new Item(p.setId(key)));if(visible(name))ModItems.ALL.add(i);}
 public static void initialize(){
  for(var m:MATERIALS){Tier t=Tier.valueOf(m.id().toUpperCase(Locale.ROOT));
   var armor=new ArmorMaterial(t.rank>=5?38:t.rank>=4?28:18,Map.of(ArmorType.HELMET,t.rank>=4?3:2,ArmorType.CHESTPLATE,t.rank>=4?8:6,ArmorType.LEGGINGS,t.rank>=4?6:5,ArmorType.BOOTS,t.rank>=4?3:2,ArmorType.BODY,t.rank>=4?11:7),t.material.enchantmentValue(),ArmorMaterials.IRON.equipSound(),t.rank>=5?3f:t.rank>=4?2f:0f,m.fireproof()?.1f:0f,repair(m.id()),ResourceKey.create(EquipmentAssets.ROOT_ID,PvpOverhaul.id(m.id())));
   if(!m.standard()){
    add(m.id()+"_sword",props(m).sword(t.material,3,-2.4f));add(m.id()+"_pickaxe",props(m).pickaxe(t.material,1,-2.8f));add(m.id()+"_axe",props(m).axe(t.material,6,-3.1f));add(m.id()+"_shovel",props(m).shovel(t.material,1.5f,-3f));add(m.id()+"_hoe",props(m).hoe(t.material,-2,-1f));
    for(var type:new ArmorType[]{ArmorType.HELMET,ArmorType.CHESTPLATE,ArmorType.LEGGINGS,ArmorType.BOOTS})add(m.id()+"_"+type.getName(),props(m).humanoidArmor(armor,type));
   }
   if(!m.externalHorse()){add(m.id()+"_horse_armor",props(m).horseArmor(armor));add(m.id()+"_nautilus_armor",props(m).nautilusArmor(armor));}
   add(m.id()+"_wolf_armor",props(m).wolfArmor(armor));
  }
 }
 private static Item.Properties props(Material m){var p=new Item.Properties();return m.fireproof()?p.fireResistant():p;}
 public static void bindExternal(){for(var m:MATERIALS)if(active(m)){
  Tier t=Tier.valueOf(m.id().toUpperCase(Locale.ROOT));
  if(m.externalSpear()){var key=net.minecraft.resources.Identifier.fromNamespaceAndPath(m.mod(),m.id()+"_spear");var i=BuiltInRegistries.ITEM.getValue(key);if(i!=null&&i!=Items.AIR){ModItems.SPEARS.put(t,i);ModItems.TIERS.put(i,t);}}
 }}
 private CompatMaterials(){}
}
