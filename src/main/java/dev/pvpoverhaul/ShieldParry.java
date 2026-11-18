package dev.pvpoverhaul;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.phys.Vec3;

/** Tracks and resolves the opening window for perfect shielding. */
public final class ShieldParry {
    private static final Map<LivingEntity,Long> PARRY_STARTED=new WeakHashMap<>();
    private static final Map<LivingEntity,Long> PARRY_COOLDOWN=new WeakHashMap<>();

    public static void track(LivingEntity defender) {
        long now=defender.level().getGameTime();
        Long cooldown=PARRY_COOLDOWN.get(defender);
        if(cooldown!=null && now>=cooldown) PARRY_COOLDOWN.remove(defender);
        if(!defender.isBlocking()) {
            PARRY_STARTED.remove(defender);
            return;
        }
        PARRY_STARTED.computeIfAbsent(defender,key->now);
    }

    public static boolean tryParry(ServerLevel level,LivingEntity defender,DamageSource source,float damage) {
        if(defender.level()!=level || !defender.isBlocking()) return false;
        Long cooldown=PARRY_COOLDOWN.get(defender);
        if(cooldown!=null && level.getGameTime()<cooldown) return false;
        Long started=PARRY_STARTED.get(defender);
        if(started==null) {
            PARRY_STARTED.put(defender,level.getGameTime());
            return false;
        }
        long elapsed=level.getGameTime()-started;
        if(elapsed<0 || elapsed>Balance.PARRY_WINDOW_TICKS) return false;

        ItemStack shield=defender.getItemBlockingWith();
        if(shield==null || shield.isEmpty()) return false;
        BlocksAttacks blocks=shield.get(DataComponents.BLOCKS_ATTACKS);
        if(blocks==null) return false;
        if(blocks.bypassedBy().map(set->set.contains(source.typeHolder())).orElse(false)) return false;
        if(!isFrontal(defender,source)) return false;

        PARRY_STARTED.remove(defender);
        PARRY_COOLDOWN.put(defender,level.getGameTime()+Balance.PARRY_COOLDOWN_TICKS);
        blocks.onBlocked(level,defender);

        if(defender instanceof ServerPlayer player)
            player.sendOverlayMessage(Component.literal("Parry!"));

        if(source.getDirectEntity() instanceof LivingEntity attacker && attacker!=defender) {
            attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,Balance.PARRY_STAGGER_TICKS,3));
            double dx=attacker.getX()-defender.getX();
            double dz=attacker.getZ()-defender.getZ();
            double length=Math.sqrt(dx*dx+dz*dz);
            if(length>1e-6) attacker.push(dx/length*0.35,0.20,dz/length*0.35);

            float counter=Balance.parryCounterDamage(damage);
            if(counter>0) {
                DamageSource counterSource;
                if(defender instanceof ServerPlayer player)
                    counterSource=level.damageSources().playerAttack(player);
                else
                    counterSource=level.damageSources().mobAttack(defender);
                attacker.hurtServer(level,counterSource,counter);
            }
        }
        return true;
    }

    private static boolean isFrontal(LivingEntity defender,DamageSource source) {
        Vec3 sourcePosition=source.getSourcePosition();
        if(sourcePosition==null && source.getDirectEntity()!=null)
            sourcePosition=source.getDirectEntity().position();
        if(sourcePosition==null) return false;
        double dx=sourcePosition.x-defender.getX();
        double dz=sourcePosition.z-defender.getZ();
        double length=Math.sqrt(dx*dx+dz*dz);
        if(length<1e-6) return false;
        Vec3 fromAttacker=new Vec3(dx/length,0,dz/length);
        Vec3 look=defender.getLookAngle();
        Vec3 facing=new Vec3(look.x,0,look.z);
        if(facing.lengthSqr()<1e-6) return false;
        facing=facing.normalize();
        return fromAttacker.dot(facing)>=-0.10;
    }

    private ShieldParry() {}
}
