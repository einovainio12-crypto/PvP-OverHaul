package dev.pvpoverhaul;

import java.util.List;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

/** Source quiver stays locked while its component-backed inventory is open. */
public final class QuiverMenu extends AbstractContainerMenu {
    private final Inventory inventory;
    private final int source;
    private final ItemStack quiver;
    private final SimpleContainer arrows;
    public QuiverMenu(int id,Inventory inventory,int source) {
        super(MenuType.GENERIC_9x1,id);this.inventory=inventory;this.source=source;this.quiver=inventory.getItem(source);
        arrows=new SimpleContainer(Quivers.SLOTS) {
            private boolean ready;
            { List<ItemStack> saved=Quivers.contents(quiver);for(int i=0;i<Quivers.SLOTS;i++) setItem(i,saved.get(i));ready=true; }
            @Override public boolean canPlaceItem(int slot,ItemStack stack) { return stack.getItem() instanceof ArrowItem; }
            @Override public void setChanged() {
                super.setChanged();
                if(ready && inventory.getItem(source)==quiver) {
                    List<ItemStack> saved=new java.util.ArrayList<>();for(int i=0;i<Quivers.SLOTS;i++) saved.add(getItem(i).copy());
                    Quivers.setContents(quiver,saved);inventory.setChanged();
                }
            }
        };
        for(int i=0;i<9;i++) addSlot(new Slot(arrows,i,8+i*18,18) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof ArrowItem; }
        });
        for(int row=0;row<3;row++) for(int col=0;col<9;col++) addPlayerSlot(col+row*9+9,8+col*18,50+row*18);
        for(int col=0;col<9;col++) addPlayerSlot(col,8+col*18,108);
    }
    private void addPlayerSlot(int index,int x,int y) {
        addSlot(new Slot(inventory,index,x,y) {
            @Override public boolean mayPickup(Player player) { return index!=source; }
            @Override public boolean mayPlace(ItemStack stack) { return index!=source; }
        });
    }
    @Override public boolean stillValid(Player player) { return player==inventory.player && inventory.getItem(source)==quiver && quiver.is(ModItems.QUIVER); }
    @Override public void clicked(int slot,int button,ContainerInput input,Player player) {
        if(input==ContainerInput.SWAP && button==source) return;
        super.clicked(slot,button,input,player);
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0 || index>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if(!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),before=stack.copy();
        if(index<9) { if(!moveItemStackTo(stack,9,45,true)) return ItemStack.EMPTY; }
        else if(!(stack.getItem() instanceof ArrowItem) || !moveItemStackTo(stack,0,9,false)) return ItemStack.EMPTY;
        if(stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
}
