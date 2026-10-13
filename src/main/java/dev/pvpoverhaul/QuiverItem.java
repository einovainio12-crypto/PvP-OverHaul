package dev.pvpoverhaul;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class QuiverItem extends Item {
    public QuiverItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(level instanceof ServerLevel) {
            int slot=hand==InteractionHand.MAIN_HAND?player.getInventory().getSelectedSlot():40;
            player.openMenu(new SimpleMenuProvider((id,inventory,user)->new QuiverMenu(id,inventory,slot),
                Component.translatable("container.pvpoverhaul.quiver")));
        }
        return InteractionResult.SUCCESS;
    }
}
