package dev.pvpoverhaul;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;

public final class Skills {
    private static final Map<UUID,State> STATES=new HashMap<>();
    private static State state(ServerPlayer p) { return STATES.computeIfAbsent(p.getUUID(),id->new State()); }
    private static final class State {
        long lockUntil,deadUntil,nextLock,nextDead,nextBoost,nextPole,lastPacket=-100;
        UUID target; boolean jumped; int airborneTicks;
    }
    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(SkillPayload.TYPE,SkillPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SkillPayload.TYPE,(payload,context)->activate(context.player(),payload.action()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->STATES.remove(handler.player.getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(server->{
            for (ServerPlayer p:server.getPlayerList().getPlayers()) {
                State s=state(p);
                if (p.onGround() || p.isInWater() || p.onClimbable()) { s.jumped=false;s.airborneTicks=0; }
                else s.airborneTicks++;
                if (!p.isAlive() || p.isSpectator()) { s.jumped=false;s.airborneTicks=0;s.lockUntil=0;s.deadUntil=0; }
            }
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server->STATES.clear());
    }
    public static void activate(ServerPlayer p,int action) {
        if (action<0 || action>4 || !p.isAlive() || p.isSpectator()) return;
        State s=state(p);long now=p.level().getGameTime();
        if (now-s.lastPacket<2) return;
        s.lastPacket=now;
        ItemStack held=p.getMainHandItem();
        switch(action) {
            case SkillPayload.TARGET_LOCK -> {
                if (skillTier(held)==null || !skillTier(held).targetLock() || now<s.nextLock) return;
                LivingEntity target=findTarget(p);
                if (target==null) { p.sendOverlayMessage(Component.literal("No visible target near your crosshair."));return; }
                s.target=target.getUUID();s.lockUntil=now+100;s.nextLock=now+240;
                p.sendOverlayMessage(Component.literal("Target lock: 5 seconds"));
            }
            case SkillPayload.DEAD_EYE -> {
                if (skillTier(held)==null || !skillTier(held).deadEye() || now<s.nextDead) return;
                s.deadUntil=now+60;s.nextDead=now+300;
                p.sendOverlayMessage(Component.literal("Dead Eye: 3 seconds"));
            }
            case SkillPayload.BOOST -> {
                ItemStack elytra=p.getItemBySlot(EquipmentSlot.CHEST);int boost=Enchantments.level(p,elytra,"boost");
                if (!p.isFallFlying() || boost==0 || now<s.nextBoost) return;
                int aerodynamics=Enchantments.level(p,elytra,"aerodynamics");
                s.nextBoost=now+60-aerodynamics*10;
                impulse(p,p.getLookAngle().scale(0.6+boost*0.2+aerodynamics*0.1));
                elytra.hurtAndBreak(1,p,EquipmentSlot.CHEST);
            }
            case SkillPayload.DOUBLE_JUMP -> {
                if (p.onGround() || s.airborneTicks<3 || s.jumped || p.isFallFlying() || p.isPassenger() || p.isInWater() || p.getAbilities().flying) return;
                int n=Enchantments.level(p,p.getItemBySlot(EquipmentSlot.FEET),"double_jump");
                if (n==0) return;
                s.jumped=true; p.setDeltaMovement(p.getDeltaMovement().x,0.42+n*0.06,p.getDeltaMovement().z);
                p.needsSync=true;p.syncVelocity=true;
                // Preserve accumulated fall distance so this cannot silently erase falling damage or smash setups.
            }
            case SkillPayload.POLE_JUMP -> {
                int pole=Enchantments.level(p,held,"pole_jump");
                if (!ModItems.isSpear(held) || pole==0 || !p.onGround() || now<s.nextPole || p.isPassenger() || p.isFallFlying()) return;
                if (Enchantments.level(p,held,"throwable")>0) return;
                s.nextPole=now+40; Vec3 look=p.getLookAngle();
                impulse(p,new Vec3(look.x*0.6,0.65+pole*0.1,look.z*0.6));
                held.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
            }
        }
    }
    private static void impulse(ServerPlayer p,Vec3 impulse) { p.push(impulse);p.syncVelocity=true; }
    private static LivingEntity findTarget(ServerPlayer p) {
        return p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(32),e->e!=p && e.isAlive() && !e.isSpectator() &&
            !p.isAlliedTo(e) && (!(e instanceof Player other) || p.canHarmPlayer(other)) && p.hasLineOfSight(e) &&
            p.getLookAngle().dot(e.getEyePosition().subtract(p.getEyePosition()).normalize())>0.96)
            .stream().min(Comparator.comparingDouble(e->1-p.getLookAngle().dot(e.getEyePosition().subtract(p.getEyePosition()).normalize()))).orElse(null);
    }
    public static boolean deadEyeActive(LivingEntity user) {
        if (!(user instanceof ServerPlayer p) || skillTier(p.getMainHandItem())==null || !skillTier(p.getMainHandItem()).deadEye()) return false;
        State s=STATES.get(p.getUUID()); return s!=null && p.level().getGameTime()<s.deadUntil;
    }
    public static boolean aim(LivingEntity user,Projectile projectile,float power,float angle) {
        if (!(user instanceof ServerPlayer p) || skillTier(p.getMainHandItem())==null || !skillTier(p.getMainHandItem()).targetLock()) return false;
        State s=STATES.get(p.getUUID());
        if (s==null || s.target==null || p.level().getGameTime()>=s.lockUntil || angle!=0) return false;
        Entity entity=p.level().getEntity(s.target);
        if (!(entity instanceof LivingEntity target) || !target.isAlive() || target.isSpectator() || p.distanceToSqr(target)>1024 ||
            !p.hasLineOfSight(target) || p.isAlliedTo(target) || (target instanceof Player other && !p.canHarmPlayer(other))) return false;
        Vec3 delta=target.getEyePosition().subtract(projectile.position());
        // Aim correction at release only; arrows retain vanilla flight, gravity and collision.
        projectile.shoot(delta.x,delta.y+delta.horizontalDistance()*0.12,delta.z,power,Combat.uncertainty(projectile,user));return true;
    }
    private static Tier skillTier(ItemStack stack) {
        if (stack.getItem() instanceof TierBowItem bow) return bow.tier;
        if (stack.getItem() instanceof TierCrossbowItem crossbow && crossbow.tier==Tier.NETHERITE) return crossbow.tier;
        return null;
    }
    private Skills() {}
}
