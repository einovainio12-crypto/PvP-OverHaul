package dev.pvpoverhaul;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record QuiverSelectionPayload(int inventorySlot,int arrowSlot) implements CustomPacketPayload {
    public static final Type<QuiverSelectionPayload> TYPE=new Type<>(PvpOverhaul.id("quiver_selection"));
    public static final StreamCodec<RegistryFriendlyByteBuf,QuiverSelectionPayload> CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,QuiverSelectionPayload::inventorySlot,
        ByteBufCodecs.VAR_INT,QuiverSelectionPayload::arrowSlot,QuiverSelectionPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
