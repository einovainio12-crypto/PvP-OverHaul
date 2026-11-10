package dev.pvpplusmounts;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.util.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.*;
public final class DragonMounts implements ModInitializer {
 static Item horse,nautilus;
 static final String PREFIX="pvpplusmounts.dash.";
 static final Identifier SCALE=Identifier.parse("pvpoverhaul:dragon_scale");
 static Item register(String path,Item.Properties props){
  var key=ResourceKey.create(Registries.ITEM,Identifier.parse("pvpplusmounts:"+path));
  return Registry.register(BuiltInRegistries.ITEM,key,new Item(props.setId(key).rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true)));
 }
 public void onInitialize(){
  Hellfork.register();
  Spellbook.register();
  horse=register("dragon_scale_horse_armor",new Item.Properties().horseArmor(ArmorMaterials.NETHERITE).component(DataComponents.UNBREAKABLE,Unit.INSTANCE));
  nautilus=register("dragon_scale_nautilus_armor",new Item.Properties().nautilusArmor(ArmorMaterials.NETHERITE));
  ServerLivingEntityEvents.ALLOW_DEATH.register((mob,source,damage)->{
   if(!(mob.level() instanceof ServerLevel level)||source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)||!mob.getType().builtInRegistryHolder().is(EntityTypeTags.CAN_WEAR_HORSE_ARMOR)||!mob.getItemBySlot(EquipmentSlot.BODY).is(horse))return true;
   mob.setItemSlot(EquipmentSlot.BODY,ItemStack.EMPTY);
   mob.setHealth(Math.min(4F,mob.getMaxHealth()));mob.clearFire();mob.removeAllEffects();
   mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION,200,1));
   mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,400,0));
   level.broadcastEntityEvent(mob,(byte)35);
   level.playSound(null,mob.blockPosition(),SoundEvents.TOTEM_USE,SoundSource.NEUTRAL,1,1);
   return false;
  });
  UseItemCallback.EVENT.register((player,world,hand)->{
   if(player.isSpectator()||!SCALE.equals(BuiltInRegistries.ITEM.getKey(player.getItemInHand(hand).getItem()))||!(player.getVehicle() instanceof AbstractNautilus mount)||mount.getControllingPassenger()!=player||!mount.getItemBySlot(EquipmentSlot.BODY).is(nautilus))return InteractionResult.PASS;
   if(!(world instanceof ServerLevel level))return InteractionResult.SUCCESS;
   long now=level.getGameTime(),ready=0;
   for(String tag:mount.entityTags())if(tag.startsWith(PREFIX))try{ready=Math.max(ready,Long.parseLong(tag.substring(PREFIX.length())));}catch(NumberFormatException ignored){}
   if(ready>now){player.sendSystemMessage(Component.literal("Dragon dash: "+((ready-now+19)/20)+"s"));return InteractionResult.SUCCESS;}
   Vec3 origin=mount.position(),look=player.getLookAngle(),target=null;
   AABB mountBox=mount.getBoundingBox();
   if(!mount.isInWater())return InteractionResult.FAIL;
   for(int i=1;i<=24;i++){
    Vec3 delta=look.scale(i*.5),point=origin.add(delta); AABB box=mountBox.move(delta);
    boolean safe=true;
    for(var entity:mount.getSelfAndPassengers().toList()){
     AABB b=entity.getBoundingBox().move(delta);
     if(!level.hasChunksAt(BlockPos.containing(b.minX,b.minY,b.minZ),BlockPos.containing(b.maxX,b.maxY,b.maxZ))||!level.getWorldBorder().isWithinBounds(b)||!level.noCollision(entity,b)){safe=false;break;}
    }
    if(!safe||!level.getFluidState(BlockPos.containing(box.getCenter())).is(FluidTags.WATER))break;
    if(i>=4)target=point;
   }
   if(target==null){player.sendSystemMessage(Component.literal("No clear water ahead"));return InteractionResult.SUCCESS;}
   for(String tag:List.copyOf(mount.entityTags()))if(tag.startsWith(PREFIX))mount.removeTag(tag);
   if(!mount.addTag(PREFIX+(now+2400)))return InteractionResult.FAIL;
   level.sendParticles(ParticleTypes.BUBBLE,origin.x,origin.y+.5,origin.z,14,.4,.3,.4,.05);
   mount.teleportTo(target.x,target.y,target.z);
   mount.getPassengers().forEach(mount::positionRider);
   level.sendParticles(ParticleTypes.BUBBLE,target.x,target.y+.5,target.z,20,.5,.3,.5,.08);
   level.playSound(null,mount.blockPosition(),SoundEvents.ENDERMAN_TELEPORT,SoundSource.NEUTRAL,.65f,1.3f);
   return InteractionResult.SUCCESS;
  });
 }
}
