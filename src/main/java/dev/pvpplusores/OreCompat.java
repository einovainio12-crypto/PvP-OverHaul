package dev.pvpplusores;
import java.util.Locale;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Weapon;
public final class OreCompat implements ModInitializer {
 public void onInitialize(){
  if(!FabricLoader.getInstance().isModLoaded("immersiveores")) return;
  for(String metal:new String[]{"vibranium","vulpus","enderium"}){
   ToolMaterial mat;
   try {mat=(ToolMaterial)Class.forName("com.blocklegend001.immersiveores.item.ModToolMaterials").getField(metal.toUpperCase(Locale.ROOT)).get(null);}
   catch(ReflectiveOperationException e){throw new IllegalStateException("Unsupported Immersive Ores material API: "+metal,e);}
   for(boolean axe:new boolean[]{true,false}){
    var id=Identifier.parse("pvpplusores:"+metal+(axe?"_battleaxe":"_arrow"));
    var key=ResourceKey.create(Registries.ITEM,id);var p=new Item.Properties().setId(key);
    Item item=axe?new Item(p.axe(mat,8,-3.2f).component(DataComponents.WEAPON,new Weapon(1,5.0f))):new ArrowItem(p);
    Registry.register(BuiltInRegistries.ITEM,key,item);
    CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,Identifier.parse("minecraft:combat"))).register(out->out.accept(item));
   }
  }
 }
}
