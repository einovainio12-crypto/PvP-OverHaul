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
        long lockUntil,deadUntil,nextLock,nextDead,nextBoost,nextPole,nextBurst;
        ItemStack tracked=ItemStack.EMPTY,burstWeapon=ItemStack.EMPTY;
        final Deque<UUID> burst=new ArrayDeque<>();
        LivingEntity shootingTarget;
        final long[] lastPacket=new long[6];
        Item lockWeapon,deadWeapon;
        UUID target; boolean jumped; int airborneTicks;
        State() { Arrays.fill(lastPacket,-100); }
    }
    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(SkillPayload.TYPE,SkillPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TargetLockPayload.TYPE,TargetLockPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SkillPayload.TYPE,(payload,context)->activate(context.player(),payload.action()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->STATES.remove(handler.player.getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(server->{
            for (ServerPlayer p:server.getPlayerList().getPlayers()) {
                State s=state(p);
                if (p.onGround() || p.isInWater() || p.onClimbable()) { s.jumped=false;s.airborneTicks=0; }
                else s.airborneTicks++;
                tickCombat(p,s);
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
                if(s.target!=null){clearLock(p,s);return;}
                if (skillTier(held)==null || !skillTier(held).targetLock()) { message(p,"Target Lock needs a copper-or-higher bow or netherite crossbow.");return; }
                if (coolingDown(p,now,s.nextLock,"Target Lock")) return;
                LivingEntity target=findTarget(p);
                if (target==null) { p.sendOverlayMessage(Component.literal("No visible target near your crosshair."));return; }
                s.target=target.getUUID();s.lockUntil=Long.MAX_VALUE;s.nextLock=now+Balance.LOCK_COOLDOWN;
                s.lockWeapon=held.getItem();s.tracked=held;sendLock(p,target.getId());
                p.sendOverlayMessage(Component.literal("Target Lock active. Use Target Lock again or sneak to release."));
            }
            case SkillPayload.DEAD_EYE -> {
                if (skillTier(held)==null || !skillTier(held).deadEye()) { message(p,"Dead Eye needs an iron, diamond or netherite bow, or netherite crossbow.");return; }
                if (coolingDown(p,now,s.nextDead,"Dead Eye")) return;
                if(!s.burst.isEmpty())return;
                List<LivingEntity> targets=p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(Balance.LOCK_RANGE),e->validTarget(p,e,true));
                targets.sort(Comparator.comparingDouble(p::distanceToSqr));
                if(targets.isEmpty()){message(p,"No visible enemies in range.");return;}
                targets.stream().limit(3).forEach(t->s.burst.add(t.getUUID()));
                s.burstWeapon=held;s.nextBurst=now;s.nextDead=now+Balance.DEAD_EYE_COOLDOWN;
                tickBurst(p,s);

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
                impulse(p,poleImpulse(look,pole));
                held.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
            }
        }
    }
    private static void message(ServerPlayer p,String text) { p.sendOverlayMessage(Component.literal(text)); }
    public static Vec3 poleImpulse(Vec3 look,int level) {
        return new Vec3(look.x*0.6,0.65+Math.min(3,level)*0.1,look.z*0.6);
    }
    public static Vec3 lungePoleImpulse(ServerPlayer player,ItemStack spear,Vec3 normal) {
        int pole=Enchantments.level(spear,"pole_jump");
        if(!ModItems.isSpear(spear) || pole==0 || Enchantments.level(spear,"minecraft:lunge")==0 || player.getXRot()<55
            || !player.onGround() || player.isPassenger() || player.isFallFlying() || player.isInWater()) return normal;
        State state=state(player);long now=player.level().getGameTime();
        if(now<state.nextPole) return Vec3.ZERO;
        state.nextPole=now+40;
        // Native Lunge already paid durability/exhaustion; its effect sends motion and applies impulse grace.
        return poleImpulse(player.getLookAngle(),pole);
    }
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
            nearCrosshair(p,e) && validTarget(p,e,false))
            .stream().min(Comparator.comparingDouble(e->1-p.getLookAngle().dot(e.getBoundingBox().getCenter().subtract(p.getEyePosition()).normalize()))).orElse(null);
    }
    public static boolean deadEyeActive(LivingEntity user) { return false; }
    public static boolean deadEyeActive(LivingEntity user,ItemStack weapon) { return false; }
    public static boolean validTarget(ServerPlayer p,LivingEntity target,boolean automatic) {
        if(target==p || !target.isAlive() || target.isSpectator() || target.level()!=p.level()
            || p.distanceToSqr(target)>Balance.LOCK_RANGE*Balance.LOCK_RANGE || p.isAlliedTo(target) || !p.hasLineOfSight(target))return false;
        if(target instanceof Player other)return !other.isCreative() && p.canHarmPlayer(other);
        if(target instanceof net.minecraft.world.entity.TamableAnimal pet && pet.isTame())return false;
        if(target instanceof net.minecraft.world.entity.decoration.ArmorStand)return false;
        return !automatic || target instanceof net.minecraft.world.entity.monster.Enemy;
    }
    private static void sendLock(ServerPlayer p,int id) {
        if(ServerPlayNetworking.canSend(p,TargetLockPayload.TYPE))ServerPlayNetworking.send(p,new TargetLockPayload(id));
    }
    private static void clearLock(ServerPlayer p,State s){if(s.target==null)return;s.target=null;s.tracked=ItemStack.EMPTY;s.lockUntil=0;sendLock(p,-1);}
    private static void tickCombat(ServerPlayer p,State s){
        if(!p.isAlive()||p.isSpectator()){clearLock(p,s);s.burst.clear();return;}
        if(s.target!=null){
            Entity e=p.level().getEntity(s.target);
            if(p.isShiftKeyDown() || rangedWeapon(p)!=s.tracked || !(e instanceof LivingEntity t) || !validTarget(p,t,false))clearLock(p,s);
            else if(p.tickCount%10==0)sendLock(p,e.getId());
        }
        tickBurst(p,s);
    }
    private static void tickBurst(ServerPlayer p,State s){
        if(s.burst.isEmpty()||p.level().getGameTime()<s.nextBurst)return;
        if(rangedWeapon(p)!=s.burstWeapon||s.burstWeapon.isEmpty()||!p.isAlive()||p.isSpectator()){s.burst.clear();return;}
        Entity e=p.level().getEntity(s.burst.removeFirst());s.nextBurst=p.level().getGameTime()+2;
        if(!(e instanceof LivingEntity target)||!validTarget(p,target,true))return;
        s.shootingTarget=target;
        try{
            var hand=p.getMainHandItem()==s.burstWeapon?net.minecraft.world.InteractionHand.MAIN_HAND:net.minecraft.world.InteractionHand.OFF_HAND;
            boolean fired=s.burstWeapon.getItem() instanceof TierBowItem bow?bow.fireBurst(p,hand,s.burstWeapon,target):
                s.burstWeapon.getItem() instanceof TierCrossbowItem crossbow && crossbow.fireBurst(p,hand,s.burstWeapon,target);
            if(!fired){s.burst.clear();message(p,"Dead Eye stopped: no compatible ammunition.");}
        }finally{s.shootingTarget=null;}
    }
    public static boolean aim(LivingEntity user,Projectile projectile,float power,float angle) {
        if(!(user instanceof ServerPlayer p))return false;
        State s=STATES.get(p.getUUID());if(s==null)return false;
        LivingEntity target=s.shootingTarget;
        if(target==null && s.target!=null && p.level().getEntity(s.target) instanceof LivingEntity t && validTarget(p,t,false))target=t;
        if(target==null||angle!=0)return false;
        Vec3 delta=target.getBoundingBox().getCenter().subtract(projectile.position());
        double flightTime=delta.horizontalDistance()/Math.max(.1,power);
        projectile.shoot(delta.x,delta.y+0.025*flightTime*flightTime,delta.z,power,0);return true;
    }
    public static void finishShot(LivingEntity user) { /* Tracking remains until released or invalid. */ }
    private static Tier skillTier(ItemStack stack) {
        if (stack.getItem() instanceof TierBowItem bow) return bow.tier;
        if (stack.getItem() instanceof TierCrossbowItem crossbow && crossbow.tier==Tier.NETHERITE) return crossbow.tier;
        return null;
    }
    private Skills() {}
}
