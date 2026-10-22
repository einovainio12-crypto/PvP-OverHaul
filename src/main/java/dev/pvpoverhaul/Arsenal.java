package dev.pvpoverhaul;
import java.util.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
public final class Arsenal {
    private static boolean secondary;
    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((target,source,base,taken,blocked)->{
            if(!secondary&&!blocked&&taken>0&&source.getEntity() instanceof Player p&&source.getDirectEntity()==p)hit(p,target,p.getMainHandItem());
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((victim,source)->{
            if(source.getEntity() instanceof Player&&victim instanceof net.minecraft.world.entity.monster.Monster&&victim.getRandom().nextFloat()<.05f)
                victim.spawnAtLocation((ServerLevel)victim.level(),new ItemStack(ModItems.COMBAT_CACHE));
        });
    }
    public static void freeze(LivingEntity target,int level){target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40+20*level,Math.min(2,level-1)));target.setTicksFrozen(Math.max(target.getTicksFrozen(),80+20*level));}
    public static void hit(Player p,LivingEntity target,ItemStack weapon) {
        if(!(p.level() instanceof ServerLevel level)||secondary||target instanceof Player other&&!p.canHarmPlayer(other))return;
        String kind=ModItems.KINDS.getOrDefault(weapon.getItem(),"");
        int ice=Enchantments.level(weapon,"ice_aspect");if(ice>0)freeze(target,ice);
        if(ModItems.isKnife(weapon)&&Enchantments.level(weapon,"minecraft:flame")>0)target.igniteForSeconds(4);
        int leech=Enchantments.level(weapon,"leeching");if(leech>0)p.heal(.5f*leech);
        int stun=Enchantments.level(weapon,"stunning");if(stun>0&&p.getRandom().nextFloat()<.1f*stun)target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,20+10*stun,3));
        if(kind.equals("hammer")){target.push(0,.25,0);target.syncVelocity=true;}
        int chain=Enchantments.level(weapon,"chaining");
        if(chain>0||kind.equals("scythe")){
            secondary=true;
            try{int count=0;for(LivingEntity next:level.getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(chain>0?3:1.5))){
                if(next==p||next==target||p.isAlliedTo(next)||next instanceof Player other&&!p.canHarmPlayer(other)||!p.hasLineOfSight(next))continue;
                if(count++>=(chain>0?chain:2))break;next.hurtServer(level,p.damageSources().playerAttack(p),chain>0?2:1.5f);
            }}finally{secondary=false;}
        }
    }
    private static void message(Player p,String text){if(p instanceof ServerPlayer server)server.sendOverlayMessage(net.minecraft.network.chat.Component.literal(text));}
    public static InteractionResult use(Player p,Level world,InteractionHand hand){
        if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;ItemStack s=p.getItemInHand(hand);
        if(s.is(ModItems.SHEATH)){
            if(world instanceof ServerLevel){
                var stored=s.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).itemCopies().filter(x->!x.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
                ItemStack off=p.getOffhandItem();if(!off.isEmpty()&&!WeaponEntities.TYPES.containsKey(off.getItem())&&!off.is(Items.TRIDENT))return InteractionResult.FAIL;
                s.set(DataComponents.CONTAINER,off.isEmpty()?ItemContainerContents.EMPTY:ItemContainerContents.fromItems(List.of(off.copyWithCount(1))));p.setItemInHand(InteractionHand.OFF_HAND,stored.copy());
            }return InteractionResult.SUCCESS;
        }
        if(s.is(ModItems.STORM_CORE)){
            ItemStack hammer=p.getOffhandItem();if(hammer.getItem()!=ModItems.ARSENAL.get("netherite_hammer")||Enchantments.level(hammer,"stormbound")>0)return InteractionResult.FAIL;
            if(world instanceof ServerLevel){s.consume(1,p);if(p.getRandom().nextFloat()<.05f){
                for(String name:List.of("stormbound","throwable","minecraft:loyalty","minecraft:channeling","minecraft:riptide")){
                    var key=ResourceKey.create(Registries.ENCHANTMENT,name.contains(":")?net.minecraft.resources.Identifier.parse(name):PvpOverhaul.id(name));
                    hammer.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key),name.endsWith("loyalty")?3:1);
                }message(p,"The storm answers. Your hammer is Stormbound.");
            }else message(p,"The storm core fades. Try another core.");}return InteractionResult.SUCCESS;
        }
        if(s.is(ModItems.HEALING_TOTEM)){
            if(p.getHealth()>=p.getMaxHealth())return InteractionResult.FAIL;
            if(world instanceof ServerLevel){p.heal(8);s.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);p.getCooldowns().addCooldown(s,600);}return InteractionResult.SUCCESS;
        }
        if(s.is(ModItems.COMBAT_CACHE)){
            if(world instanceof ServerLevel){s.consume(1,p);Item reward=p.getRandom().nextFloat()<.02f?ModItems.STORM_CORE:new ArrayList<>(ModItems.ARSENAL.values()).get(p.getRandom().nextInt(ModItems.ARSENAL.size()));ItemStack prize=new ItemStack(reward);if(!p.getInventory().add(prize))p.spawnAtLocation((ServerLevel)world,prize);}return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    private Arsenal(){}
}
