package dev.pvpoverhaul;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

/** Uses the existing synchronized crafting screen, with arrow-only outputs. */
public final class FletchingMenu extends CraftingMenu {
    private final ContainerLevelAccess table;
    private final Player player;
    public FletchingMenu(int id,Inventory inventory,ContainerLevelAccess table) {
        super(id,inventory,table);this.table=table;this.player=inventory.player;
    }
    @Override public boolean stillValid(Player user) { return stillValid(table,user,Blocks.FLETCHING_TABLE); }
    @Override public void slotsChanged(Container container) { super.slotsChanged(container);filterResult(); }
    @Override public void finishPlacingRecipe(ServerLevel level,RecipeHolder<CraftingRecipe> recipe) {
        super.finishPlacingRecipe(level,recipe);filterResult();
    }
    private void filterResult() {
        if(player instanceof ServerPlayer server && !resultSlots.getItem(0).isEmpty()
                && !(resultSlots.getItem(0).getItem() instanceof ArrowItem)) {
            resultSlots.setItem(0,ItemStack.EMPTY);setRemoteSlot(0,ItemStack.EMPTY);
            server.connection.send(new ClientboundContainerSetSlotPacket(containerId,incrementStateId(),0,ItemStack.EMPTY));
        }
    }
}
