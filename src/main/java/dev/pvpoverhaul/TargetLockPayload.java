package dev.pvpoverhaul;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record TargetLockPayload(int entityId) implements CustomPacketPayload {
 public static final Type<TargetLockPayload> TYPE=new Type<>(PvpOverhaul.id("target_lock"));
 public static final StreamCodec<RegistryFriendlyByteBuf,TargetLockPayload> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,TargetLockPayload::entityId,TargetLockPayload::new);
 @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
