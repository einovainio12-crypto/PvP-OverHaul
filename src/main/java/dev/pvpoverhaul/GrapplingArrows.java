package dev.pvpoverhaul;

import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.*;

/** One temporary, collision-preserving tether per shooter; player targets are never moved. */
public final class GrapplingArrows {
    public interface Visual { boolean pvp$isGrapplingArrow(); }
    public static final double ATTACH_RANGE=48,TETHER_RANGE=64,SPEED_CAP=1.35;
    public static final int DURATION=100;
    private record Tether(ServerPlayer owner,AbstractArrow hook,ServerPlayer target,Vec3 anchor,BlockPos block,long expires) {}
    private static final Map<UUID,Tether> ACTIVE=new HashMap<>();
    public static boolean isGrappling(AbstractArrow arrow) { return arrow.getPickupItemStackOrigin().getItem() instanceof TierArrowItem item && item.ammo==Ammo.GRAPPLING; }
    public static boolean tracked(AbstractArrow hook) { return ACTIVE.values().stream().anyMatch(tether->tether.hook==hook); }
    public static Vec3 pullMotion(Vec3 velocity,Vec3 toward) {
        if(!Double.isFinite(velocity.lengthSqr()) || !Double.isFinite(toward.lengthSqr()) || toward.lengthSqr()<1e-8) return Vec3.ZERO;
        Vec3 result=velocity.scale(0.82).add(toward.normalize().scale(0.22)).add(0,0.08,0);
        return result.lengthSqr()>SPEED_CAP*SPEED_CAP?result.normalize().scale(SPEED_CAP):result;
    }
    public static boolean impact(AbstractArrow hook,HitResult hit) {
        if(!isGrappling(hook)) return false;
        if(hook.level().isClientSide()) return true;
        Entity shooter=hook.getOwner();
        if(!(shooter instanceof ServerPlayer owner) || !owner.isAlive() || owner.isSpectator()) { hook.discard();return true; }
        ServerPlayer target=null;BlockPos block=null;Vec3 anchor=hit.getLocation();
        if(hit instanceof EntityHitResult entityHit) {
            if(!(entityHit.getEntity() instanceof ServerPlayer player) || player==owner || !player.isAlive() || player.isSpectator()) { hook.discard();return true; }
            target=player;anchor=player.position().add(0,player.getBbHeight()*0.55,0);
        } else if(hit instanceof BlockHitResult blockHit) {
            block=blockHit.getBlockPos();
            anchor=anchor.add(blockHit.getDirection().getStepX()*0.7,blockHit.getDirection().getStepY()*0.7,blockHit.getDirection().getStepZ()*0.7);
        }
        if(owner.position().distanceToSqr(anchor)>ATTACH_RANGE*ATTACH_RANGE || owner.isShiftKeyDown() || owner.isPassenger()) {
            hook.discard();owner.sendOverlayMessage(Component.literal("Grapple: anchor must be within 48 blocks; release Sneak to grapple."));return true;
        }
        release(owner.getUUID());
        hook.setPos(anchor);hook.setDeltaMovement(Vec3.ZERO);hook.setNoPhysics(true);hook.setNoGravity(true);hook.setCritArrow(false);hook.pickup=AbstractArrow.Pickup.DISALLOWED;
        ACTIVE.put(owner.getUUID(),new Tether(owner,hook,target,anchor,block,owner.level().getGameTime()+DURATION));
        owner.sendOverlayMessage(Component.literal("Grapple attached — Sneak to release"));return true;
    }
    private static void release(UUID owner) { Tether tether=ACTIVE.remove(owner);if(tether!=null) tether.hook.discard(); }
    public static void initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->release(handler.player.getUUID()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server->ACTIVE.clear());
        ServerTickEvents.END_SERVER_TICK.register(server->{
            var iterator=ACTIVE.values().iterator();
            while(iterator.hasNext()) {
                Tether tether=iterator.next();ServerPlayer player=tether.owner;AbstractArrow hook=tether.hook;
                Vec3 anchor=tether.target==null?tether.anchor:tether.target.position().add(0,tether.target.getBbHeight()*0.55,0);
                boolean valid=player.isAlive() && !player.isRemoved() && !player.isSpectator() && !player.isShiftKeyDown() && !player.isPassenger()
                    && !hook.isRemoved() && hook.level()==player.level() && player.level().getGameTime()<tether.expires
                    && player.position().distanceToSqr(anchor)<=TETHER_RANGE*TETHER_RANGE;
                if(tether.target!=null) valid&=tether.target.isAlive() && !tether.target.isRemoved() && !tether.target.isSpectator() && tether.target.level()==player.level();
                if(tether.block!=null) valid&=player.level().hasChunkAt(tether.block) && !player.level().getBlockState(tether.block).isAir();
                Vec3 direction=anchor.subtract(player.position().add(0,player.getBbHeight()*0.5,0));
                if(!valid || direction.lengthSqr()<1.6*1.6) { hook.discard();iterator.remove();continue; }
                hook.setPos(anchor);hook.setDeltaMovement(Vec3.ZERO);hook.needsSync=true;
                player.setDeltaMovement(pullMotion(player.getDeltaMovement(),direction));player.syncVelocity=true;player.needsSync=true;
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
            }
        });
    }
    private GrapplingArrows() {}
}
