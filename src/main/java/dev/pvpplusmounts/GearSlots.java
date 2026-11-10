package dev.pvpplusmounts;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
public final class GearSlots {
 static Class<?> api(String n)throws Exception{return Class.forName("eu.pb4.trinkets.api."+n);}
 @SuppressWarnings("unchecked") public static Map<String,Container> all(Player p){
  if(!FabricLoader.getInstance().isModLoaded("trinkets_updated"))return Map.of();
  try{Object a=api("TrinketsApi").getMethod("getAttachment",LivingEntity.class).invoke(null,p);
   return a==null?Map.of():(Map<String,Container>)api("TrinketAttachment").getMethod("getInventories").invoke(a);
  }catch(Exception e){throw new IllegalStateException("Unsupported Trinkets Updated API",e);}
 }
 public static ItemStack worn(Player p,String name){var c=all(p).get(name);return c==null||c.getContainerSize()==0?ItemStack.EMPTY:c.getItem(0);}
 public static ItemStack at(Inventory inv,int i){return i==101?worn(inv.player,"chest/quiver"):inv.getItem(i);}
 public static void dirty(Player p){var c=all(p).get("chest/quiver");if(c!=null)c.setChanged();}
 static Object call(Container c,String m,Class<?>[] types,Object...args)throws Exception{return api("TrinketInventory").getMethod(m,types).invoke(c,args);}
}
