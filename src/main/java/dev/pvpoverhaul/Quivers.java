package dev.pvpoverhaul;

import java.util.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import java.util.function.Predicate;

/** Native component storage and a consume-once bridge to vanilla projectile drawing. */
public final class Quivers {
    public static final int SLOTS=9;
    private static final String SELECTED="pvpoverhaul_selected_arrow";
    private static final String ACTIVE="pvpoverhaul_active_quiver";
    private static final Map<Player,Integer> WARNED=new WeakHashMap<>();
    private record Origin(Player owner,int inventorySlot,ItemStack quiver,int arrowSlot,ItemStack before) {}
    private static final Map<ItemStack,Origin> DRAWN=Collections.synchronizedMap(new WeakHashMap<>());
    public static List<ItemStack> contents(ItemStack quiver) {
        List<ItemStack> result=new ArrayList<>(quiver.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).itemCopies().limit(SLOTS).toList());
        while(result.size()<SLOTS) result.add(ItemStack.EMPTY);
        return result;
    }
    public static void setContents(ItemStack quiver,List<ItemStack> items) {
        if(items.size()!=SLOTS) throw new IllegalArgumentException("Quiver needs nine slots");
        quiver.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(items));
    }
    public static int selected(ItemStack quiver) {
        var tag=quiver.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if(!tag.contains(SELECTED)) {
            List<ItemStack> arrows=contents(quiver);
            for(int i=0;i<SLOTS;i++) if(!arrows.get(i).isEmpty() && arrows.get(i).getItem() instanceof ArrowItem) return i;
            return -1;
        }
        int slot=tag.getIntOr(SELECTED,-1);
        return slot>=0 && slot<SLOTS?slot:-1;
    }
    public static int ammunitionSlot(ItemStack quiver,Predicate<ItemStack> supported) {
        List<ItemStack> arrows=contents(quiver);
        if(quiver.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().contains(SELECTED)) {
            int slot=selected(quiver);
            return slot>=0 && !arrows.get(slot).isEmpty() && arrows.get(slot).getItem() instanceof ArrowItem && supported.test(arrows.get(slot))?slot:-1;
        }
        for(int i=0;i<SLOTS;i++) if(!arrows.get(i).isEmpty() && arrows.get(i).getItem() instanceof ArrowItem && supported.test(arrows.get(i))) return i;
        return -1;
    }
    public static void select(ItemStack quiver,int slot) {
        if(slot<-1 || slot>=SLOTS) throw new IllegalArgumentException("Invalid arrow selection");
        CustomData.update(DataComponents.CUSTOM_DATA,quiver,tag->tag.putInt(SELECTED,slot));
    }
    public static boolean active(ItemStack quiver) {
        return quiver.is(ModItems.QUIVER) && quiver.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBooleanOr(ACTIVE,false);
    }
    public static void activate(List<ItemStack> carried,ItemStack selectedQuiver) {
        for(ItemStack stack:carried) if(stack.is(ModItems.QUIVER)) {
            CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{
                if(stack==selectedQuiver) tag.putBoolean(ACTIVE,true);else tag.remove(ACTIVE);
            });
        }
    }
    public static void select(Player player,int inventorySlot,int arrowSlot) {
        ItemStack quiver=player.getInventory().getItem(inventorySlot);
        if(!quiver.is(ModItems.QUIVER)) return;
        List<ItemStack> carried=new ArrayList<>();
        for(int i=0;i<36;i++) carried.add(player.getInventory().getItem(i));
        carried.add(player.getOffhandItem());activate(carried,quiver);select(quiver,arrowSlot);
    }
    public static int find(Player player) {
        List<ItemStack> carried=new ArrayList<>();
        for(int i=0;i<=40;i++) carried.add(player.getInventory().getItem(i));
        return preferredSlot(carried,player.getInventory().getSelectedSlot());
    }
    public static int preferredSlot(List<ItemStack> carried,int selected) {
        if(selected>=0 && selected<36 && carried.get(selected).is(ModItems.QUIVER)) return selected;
        for(int i=0;i<36;i++) if(active(carried.get(i))) return i;
        if(active(carried.get(40))) return 40;
        if(carried.get(40).is(ModItems.QUIVER)) return 40;
        for(int i=0;i<36;i++) if(carried.get(i).is(ModItems.QUIVER)) return i;
        return -1;
    }
    public static ItemStack ammunition(Player player,ItemStack weapon) {
        // The storage menu owns a live snapshot. Do not draw from that snapshot concurrently.
        if(player.containerMenu instanceof QuiverMenu) return ItemStack.EMPTY;
        if(!(weapon.getItem() instanceof ProjectileWeaponItem ranged)) return ItemStack.EMPTY;
        int inventorySlot=find(player);
        if(inventorySlot<0) return ItemStack.EMPTY;
        ItemStack quiver=player.getInventory().getItem(inventorySlot);int slot=ammunitionSlot(quiver,ranged.getAllSupportedProjectiles());
        if(slot<0) {
            if(blocksInventoryFallback(player,weapon) && !player.level().isClientSide()
                    && player.tickCount-WARNED.getOrDefault(player,-100)>=40) {
                player.sendOverlayMessage(Component.literal("Your selected quiver arrow is not supported by this weapon. Choose another arrow or 0 for inventory ammo."));
                WARNED.put(player,player.tickCount);
            }
            return ItemStack.EMPTY;
        }
        ItemStack ammo=contents(quiver).get(slot);
        if(ammo.isEmpty() || !(ammo.getItem() instanceof ArrowItem) || !ranged.getAllSupportedProjectiles().test(ammo)) return ItemStack.EMPTY;
        if(!player.level().isClientSide()) DRAWN.put(ammo,new Origin(player,inventorySlot,quiver,slot,ammo.copy()));
        return ammo;
    }
    public static boolean blocksInventoryFallback(Player player,ItemStack weapon) {
        if(player.containerMenu instanceof QuiverMenu || !(weapon.getItem() instanceof ProjectileWeaponItem ranged)) return false;
        int inventorySlot=find(player);if(inventorySlot<0) return false;
        ItemStack quiver=player.getInventory().getItem(inventorySlot);
        if(!quiver.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().contains(SELECTED)) return false;
        int slot=selected(quiver);if(slot<0) return false;
        ItemStack arrow=contents(quiver).get(slot);
        return !arrow.isEmpty() && arrow.getItem() instanceof ArrowItem && !ranged.getAllSupportedProjectiles().test(arrow);
    }
    public static boolean validDraw(ItemStack ammo,LivingEntity shooter) {
        Origin origin=DRAWN.get(ammo);
        if(origin==null) return true;
        if(shooter!=origin.owner || origin.owner.getInventory().getItem(origin.inventorySlot)!=origin.quiver) return false;
        ItemStack current=contents(origin.quiver).get(origin.arrowSlot);
        return current.getCount()==origin.before.getCount() && ItemStack.isSameItemSameComponents(current,origin.before);
    }
    public static boolean commitDraw(ItemStack quiver,int slot,ItemStack before,ItemStack remaining) {
        if(slot<0 || slot>=SLOTS || !quiver.is(ModItems.QUIVER) || remaining.getCount()>before.getCount()
            || (!remaining.isEmpty() && !ItemStack.isSameItemSameComponents(before,remaining))) return false;
        List<ItemStack> contents=contents(quiver);ItemStack current=contents.get(slot);
        if(current.getCount()!=before.getCount() || !ItemStack.isSameItemSameComponents(current,before)) return false;
        contents.set(slot,remaining.copy());setContents(quiver,contents);return true;
    }
    public static boolean finishDraw(ItemStack ammo) {
        Origin origin=DRAWN.remove(ammo);
        if(origin==null) return true;
        if(origin.owner.getInventory().getItem(origin.inventorySlot)!=origin.quiver
            || !commitDraw(origin.quiver,origin.arrowSlot,origin.before,ammo)) return false;
        origin.owner.getInventory().setChanged();origin.owner.inventoryMenu.broadcastChanges();return true;
    }
    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(QuiverSelectionPayload.TYPE,QuiverSelectionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(QuiverSelectionPayload.TYPE,(payload,context)->{
            ServerPlayer player=context.player();
            if(!player.isAlive() || player.isSpectator() || payload.inventorySlot()<0 || (payload.inventorySlot()>=36 && payload.inventorySlot()!=40)
                    || payload.arrowSlot()<-2 || payload.arrowSlot()>=SLOTS) return;
            ItemStack quiver=player.getInventory().getItem(payload.inventorySlot());
            if(!quiver.is(ModItems.QUIVER)) return;
            if(payload.arrowSlot()==-2) {
                player.openMenu(new SimpleMenuProvider((id,inventory,user)->new QuiverMenu(id,inventory,payload.inventorySlot()),Component.translatable("container.pvpoverhaul.quiver")));
                return;
            }
            if(player.containerMenu instanceof QuiverMenu) return;
            select(player,payload.inventorySlot(),payload.arrowSlot());player.getInventory().setChanged();player.inventoryMenu.broadcastChanges();
            ItemStack weapon=player.getMainHandItem().getItem() instanceof ProjectileWeaponItem?player.getMainHandItem():player.getOffhandItem();
            player.sendOverlayMessage(Component.literal(blocksInventoryFallback(player,weapon)?"Selected arrow is not supported by this weapon. Choose another arrow or 0 for inventory ammo.":
                payload.arrowSlot()<0?"Using inventory ammunition":"Quiver arrow slot "+(payload.arrowSlot()+1)+" selected; applies to the next shot/load."));
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server->{DRAWN.clear();WARNED.clear();});
    }
    private Quivers() {}
}
