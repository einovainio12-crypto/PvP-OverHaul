package dev.pvpoverhaul;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The server derives all eligibility, targets and impulses. Clients send only an action number. */
public record SkillPayload(int action) implements CustomPacketPayload {
    public static final int TARGET_LOCK=0, DEAD_EYE=1, BOOST=2, DOUBLE_JUMP=3, POLE_JUMP=4;
    public static final Type<SkillPayload> TYPE=new Type<>(PvpOverhaul.id("skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SkillPayload> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,SkillPayload::action,SkillPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
