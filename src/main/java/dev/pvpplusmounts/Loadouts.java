package dev.pvpplusmounts;
import java.util.*;
import java.util.function.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
public final class Loadouts {
 record Slot(Supplier<ItemStack> get,Consumer<ItemStack> set){}
 static Map<String,Slot> slots(ServerPlayer p)throws Exception{
  Map<String,Slot> map=new TreeMap<>();var inv=p.getInventory();
  for(int i=0;i<inv.getContainerSize();i++){int j=i;map.put("inv:"+j,new Slot(()->inv.getItem(j),s->inv.setItem(j,s)));}
  for(var ent:GearSlots.all(p).entrySet()){var c=ent.getValue();boolean cosmetic=(boolean)GearSlots.call(c,"hasCosmeticItems",new Class<?>[]{});
   for(int i=0;i<c.getContainerSize();i++){final int j=i;
    map.put("gear:"+ent.getKey()+":"+j,new Slot(()->c.getItem(j),s->{c.setItem(j,s);c.setChanged();}));
    if(cosmetic)map.put("skin:"+ent.getKey()+":"+j,new Slot(()->{try{return (ItemStack)GearSlots.call(c,"getCosmeticItem",new Class<?>[]{int.class},j);}catch(Exception e){throw new IllegalStateException(e);}},s->{try{if(!Boolean.TRUE.equals(GearSlots.call(c,"setCosmeticItem",new Class<?>[]{int.class,ItemStack.class},j,s)))throw new IllegalStateException();c.setChanged();}catch(Exception e){throw new IllegalStateException(e);}}));
   }
  }return map;
 }
 static int kit(CommandSourceStack src,String n,boolean save)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
  var p=src.getPlayerOrException();if(!n.matches("[a-z0-9_-]{1,32}")){src.sendFailure(Component.literal("Use 1–32 lowercase letters, digits, underscores or hyphens."));return 0;}
  if(p.containerMenu!=p.inventoryMenu||!p.inventoryMenu.getCarried().isEmpty()){src.sendFailure(Component.literal("Close your menu and clear your cursor first."));return 0;}
  try{
   var store=p.level().getServer().getCommandStorage();var key=Identifier.parse("pvppluskits:"+p.getUUID()+"/"+n);var ops=p.registryAccess().createSerializationContext(NbtOps.INSTANCE);var live=slots(p);
   if(save){var data=new CompoundTag();var bag=new CompoundTag();for(var e:live.entrySet())bag.store(e.getKey(),ItemStack.OPTIONAL_CODEC,ops,e.getValue().get.get());data.put("slots",bag);data.putInt("selected",p.getInventory().getSelectedSlot());store.set(key,data);}
   else {var data=store.get(key);var bag=data.getCompound("slots").orElseThrow();if(!bag.keySet().equals(live.keySet())){src.sendFailure(Component.literal("Slot layout changed. Kit was not loaded."));return 0;}
    Map<String,ItemStack> ready=new TreeMap<>();for(var k:live.keySet())ready.put(k,bag.read(k,ItemStack.OPTIONAL_CODEC,ops).orElseThrow());
    for(var e:ready.entrySet())live.get(e.getKey()).set.accept(e.getValue());p.getInventory().setSelectedSlot(Math.clamp(data.getIntOr("selected",0),0,8));p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();}
   src.sendSuccess(()->Component.literal((save?"Saved ":"Loaded ")+n),false);return 1;
  }catch(Exception e){src.sendFailure(Component.literal("Kit unavailable: "+e.getClass().getSimpleName()));return 0;}
 }
 static int sheath(ServerPlayer p){ItemStack sheath=GearSlots.worn(p,"legs/sheath");
  if(!BuiltInRegistries.ITEM.getKey(sheath.getItem()).toString().equals("pvpoverhaul:weapon_sheath"))return 0;
  ItemStack hand=p.getMainHandItem(),old=sheath.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).itemCopies().filter(x->!x.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
  if(!hand.isEmpty()&&(hand.getCount()!=1||hand.has(DataComponents.CONTAINER)||(!hand.has(DataComponents.WEAPON)&&!hand.has(DataComponents.ATTRIBUTE_MODIFIERS)&&!(hand.getItem() instanceof ProjectileWeaponItem))))return 0;
  sheath.set(DataComponents.CONTAINER,hand.isEmpty()?ItemContainerContents.EMPTY:ItemContainerContents.fromItems(List.of(hand.copy())));
  p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,old.copy());GearSlots.all(p).get("legs/sheath").setChanged();return 1;}
 static void register(){CommandRegistrationCallback.EVENT.register((d,ctx,env)->{
  var root=Commands.literal("kit").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));for(String op:List.of("save","load"))root.then(Commands.literal(op).then(Commands.argument("name",StringArgumentType.word()).executes(c->kit(c.getSource(),StringArgumentType.getString(c,"name"),op.equals("save")))));d.register(root);
  d.register(Commands.literal("sheathswap").executes(c->sheath(c.getSource().getPlayerOrException())));
 });}
}
