package dev.pvpoverhaul;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
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
        long lockUntil,deadUntil,nextLock,nextDead,nextBoost,nextPole;
        final long[] lastPacket=new long[6];
        Item lockWeapon,deadWeapon;
        UUID target; boolean jumped; int airborneTicks;
        State() { Arrays.fill(lastPacket,-100); }
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
        if (action<0 || action>5 || !p.isAlive() || p.isSpectator()) return;
        State s=state(p);long now=p.level().getGameTime();
        if (now-s.lastPacket[action]<2) return;
        s.lastPacket[action]=now;
        ItemStack held=rangedWeapon(p);
        switch(action) {
            case SkillPayload.TARGET_LOCK -> {
                if (skillTier(held)==null || !skillTier(held).targetLock()) { message(p,"Target Lock needs a copper-or-higher bow or netherite crossbow.");return; }
                if (coolingDown(p,now,s.nextLock,"Target Lock")) return;
                LivingEntity target=findTarget(p);
                if (target==null) { p.sendOverlayMessage(Component.literal("No visible target near your crosshair."));return; }
                s.target=target.getUUID();s.lockUntil=now+Balance.LOCK_DURATION;s.nextLock=now+Balance.LOCK_COOLDOWN;
                s.lockWeapon=held.getItem();
                p.sendOverlayMessage(Component.literal("Target lock: one charged shot within 3 seconds"));
            }
            case SkillPayload.DEAD_EYE -> {
                if (skillTier(held)==null || !skillTier(held).deadEye()) { message(p,"Dead Eye needs an iron, diamond or netherite bow, or netherite crossbow.");return; }
                if (coolingDown(p,now,s.nextDead,"Dead Eye")) return;
                s.deadUntil=now+Balance.DEAD_EYE_DURATION;s.nextDead=now+Balance.DEAD_EYE_COOLDOWN;
                s.deadWeapon=held.getItem();
                p.sendOverlayMessage(Component.literal("Dead Eye: one charged shot within 3 seconds"));
            }
            case SkillPayload.BOOST, SkillPayload.OVERLOAD -> {
                boolean overload=action==SkillPayload.OVERLOAD;
                String label=overload?"Overload":"Boost";
                ItemStack elytra=p.getItemBySlot(EquipmentSlot.CHEST);
                int enchantment=Enchantments.level(p,elytra,overload?"overload":"boost");
                if (!elytra.is(Items.ELYTRA) || enchantment==0) { message(p,label+" needs an equipped Elytra with its enchantment.");return; }
                if (!p.isFallFlying() || p.isPassenger() || p.getAbilities().flying) { message(p,"Start gliding before using "+label+".");return; }
                if (coolingDown(p,now,s.nextBoost,label)) return;
                int cost=overload?Balance.overloadCost(elytra.getMaxDamage()):Balance.BOOST_DURABILITY_COST;
                if (elytra.getMaxDamage()-elytra.getDamageValue()<=cost+1) { message(p,label+" needs more than "+(cost+1)+" durability remaining.");return; }
                Vec3 velocity=p.getDeltaMovement(),direction=p.getLookAngle();
                double amount=overload?Balance.allowedImpulse(velocity.x,velocity.y,velocity.z,direction.x,direction.y,direction.z,Balance.OVERLOAD_STRENGTH,Balance.OVERLOAD_SPEED_CAP)
                    :Balance.allowedBoostImpulse(velocity.x,velocity.y,velocity.z,direction.x,direction.y,direction.z,enchantment,0);
                if (amount<1e-6) { message(p,label+" speed limit reached.");return; }
                s.nextBoost=now+(overload?Balance.OVERLOAD_COOLDOWN:Balance.boostCooldown(enchantment,0));
                impulse(p,direction.normalize().scale(amount));
                // Overload's exact half-maximum cost is intentional; Unbreaking does not discount it.
                if(overload) elytra.setDamageValue(elytra.getDamageValue()+cost);
                else elytra.hurtAndBreak(cost,p,EquipmentSlot.CHEST);
                p.causeFoodExhaustion(0.6f);
                message(p,label+" activated ("+cost+" durability).");
            }
            case SkillPayload.DOUBLE_JUMP -> {
                if (p.onGround() || s.airborneTicks<3 || s.jumped || p.isFallFlying() || p.isPassenger() || p.isInWater() || p.getAbilities().flying) return;
                int n=Enchantments.level(p,p.getItemBySlot(EquipmentSlot.FEET),"double_jump");
                if (n==0 || Enchantments.level(p,p.getItemBySlot(EquipmentSlot.FEET),"speed")>0) return;
                s.jumped=true; p.setDeltaMovement(p.getDeltaMovement().x,0.42+n*0.06,p.getDeltaMovement().z);
                syncMovement(p);
                // Preserve accumulated fall distance so this cannot silently erase falling damage or smash setups.
            }
            case SkillPayload.POLE_JUMP -> {
                held=p.getMainHandItem();
                int pole=Enchantments.level(p,held,"pole_jump");
                if (!ModItems.isSpear(held) || pole==0) { message(p,"Pole Jump needs a spear with the Pole Jump enchantment.");return; }
                if (!p.onGround() || p.isPassenger() || p.isFallFlying()) { message(p,"Pole Jump requires standing on the ground.");return; }
                if (coolingDown(p,now,s.nextPole,"Pole Jump")) return;
                if (Enchantments.level(p,held,"throwable")>0) return;
                s.nextPole=now+40; Vec3 look=p.getLookAngle();
                impulse(p,new Vec3(look.x*0.6,0.65+pole*0.1,look.z*0.6));
                held.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
            }
        }
    }
    private static void message(ServerPlayer p,String text) { p.sendOverlayMessage(Component.literal(text)); }
    private static boolean coolingDown(ServerPlayer p,long now,long until,String skill) {
        if(now>=until) return false;
        message(p,skill+" cooldown: "+((until-now+19)/20)+"s");return true;
    }
    private static void syncMovement(ServerPlayer p) {
        p.needsSync=true;p.syncVelocity=true;p.connection.send(new ClientboundSetEntityMotionPacket(p));
    }
    private static void impulse(ServerPlayer p,Vec3 impulse) { p.push(impulse);syncMovement(p); }
    private static ItemStack rangedWeapon(LivingEntity p) {
        if(p.isUsingItem() && skillTier(p.getUseItem())!=null) return p.getUseItem();
        return skillTier(p.getMainHandItem())!=null?p.getMainHandItem():p.getOffhandItem();
    }
    private static boolean nearCrosshair(ServerPlayer p,LivingEntity target) {
        Vec3 eye=p.getEyePosition(),look=p.getLookAngle();
        return target.getBoundingBox().inflate(0.25).clip(eye,eye.add(look.scale(Balance.LOCK_RANGE))).isPresent()
            || look.dot(target.getBoundingBox().getCenter().subtract(eye).normalize())>Balance.LOCK_CONE;
    }
    private static LivingEntity findTarget(ServerPlayer p) {
        return p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(Balance.LOCK_RANGE),e->e!=p && e.isAlive() && !e.isSpectator() &&
            p.distanceToSqr(e)<=Balance.LOCK_RANGE*Balance.LOCK_RANGE && !p.isAlliedTo(e) && (!(e instanceof Player other) || p.canHarmPlayer(other)) && p.hasLineOfSight(e) &&
            nearCrosshair(p,e))
            .stream().min(Comparator.comparingDouble(e->1-p.getLookAngle().dot(e.getBoundingBox().getCenter().subtract(p.getEyePosition()).normalize()))).orElse(null);
    }
    public static boolean deadEyeActive(LivingEntity user) {
        return deadEyeActive(user,rangedWeapon(user));
    }
    public static boolean deadEyeActive(LivingEntity user,ItemStack firedWeapon) {
        Tier tier=firedWeapon==null?null:skillTier(firedWeapon);
        if(!(user instanceof ServerPlayer p) || tier==null || !tier.deadEye()) return false;
        State s=STATES.get(p.getUUID());
        return s!=null && p.level().getGameTime()<s.deadUntil && firedWeapon.getItem()==s.deadWeapon;
    }
    public static boolean aim(LivingEntity user,Projectile projectile,float power,float angle) {
        if (!(user instanceof ServerPlayer p)) return false;
        if (!(projectile instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow)) return false;
        Tier firedTier=arrow.getWeaponItem()==null?null:skillTier(arrow.getWeaponItem());
        if (firedTier==null || !firedTier.targetLock()) return false;
        State s=STATES.get(p.getUUID());
        if (s==null || s.target==null || arrow.getWeaponItem().getItem()!=s.lockWeapon || p.level().getGameTime()>=s.lockUntil || angle!=0 || power<3f) return false;
        Entity entity=p.level().getEntity(s.target);
        if (!(entity instanceof LivingEntity target) || !target.isAlive() || target.isSpectator() || p.distanceToSqr(target)>Balance.LOCK_RANGE*Balance.LOCK_RANGE ||
            !p.hasLineOfSight(target) || p.isAlliedTo(target) || (target instanceof Player other && !p.canHarmPlayer(other))) return false;
        if (!nearCrosshair(p,target)) return false;
        Vec3 delta=target.getBoundingBox().getCenter().subtract(projectile.position());
        // Aim correction at release only; arrows retain vanilla flight, gravity and collision.
        double flightTime=delta.horizontalDistance()/power;
        projectile.shoot(delta.x,delta.y+0.025*flightTime*flightTime,delta.z,power,Combat.uncertainty(projectile,user));return true;
    }
    public static void finishShot(LivingEntity user) {
        if (user instanceof ServerPlayer p) {
            State s=STATES.get(p.getUUID());
            if (s!=null) { s.lockUntil=0;s.deadUntil=0;s.target=null; }
        }
    }
    private static Tier skillTier(ItemStack stack) {
        if (stack.getItem() instanceof TierBowItem bow) return bow.tier;
        if (stack.getItem() instanceof TierCrossbowItem crossbow && crossbow.tier==Tier.NETHERITE) return crossbow.tier;
        return null;
    }
    private Skills() {}
}
