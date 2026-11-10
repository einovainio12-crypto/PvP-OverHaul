package dev.pvpplusmounts;

import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.phys.Vec3;

public final class Hellfork extends TridentItem {
 private Hellfork(Properties properties){super(properties);}
 static void register(){
  var key=ResourceKey.create(Registries.ITEM,Identifier.parse("pvpplushell:hellfork"));
  var fork=Registry.register(BuiltInRegistries.ITEM,key,new Hellfork(new Properties().setId(key).rarity(Rarity.EPIC)
   .delayedComponent(DataComponents.ENCHANTMENTS,lookup->{
    var e=new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
    e.upgrade(lookup.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.RIPTIDE),3);return e.toImmutable();})
   .durability(250).fireResistant().enchantable(1).attributes(createAttributes()).component(DataComponents.TOOL,createToolProperties()).component(DataComponents.WEAPON,new Weapon(1))));
  ServerLivingEntityEvents.ALLOW_DAMAGE.register((mob,source,amount)->!(source.is(DamageTypeTags.IS_FIRE)
   && (mob.getMainHandItem().is(fork)||mob.getOffhandItem().is(fork))));
  var bastions=Set.of(BuiltInLootTables.BASTION_TREASURE,BuiltInLootTables.BASTION_OTHER,BuiltInLootTables.BASTION_BRIDGE,BuiltInLootTables.BASTION_HOGLIN_STABLE);
  LootTableEvents.MODIFY.register((id,table,source,registries)->{
   if(source.isBuiltin() && bastions.contains(id))table.withPool(LootPool.lootPool().setRolls(Holder.direct(new ConstantValue(1)))
    .when(Holder.direct(LootItemRandomChanceCondition.randomChance(.02f).build())).add(LootItem.lootTableItem(fork)));
  });
 }
 @Override public void inventoryTick(ItemStack stack,net.minecraft.server.level.ServerLevel level,Entity owner,EquipmentSlot slot){
  super.inventoryTick(stack,level,owner,slot);
  var r=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.RIPTIDE);
  if(stack.getEnchantments().getLevel(r)<3)stack.enchant(r,3);
 }
 private static boolean fueled(Player p){return p.isInLava()||p.getOffhandItem().is(Items.LAVA_BUCKET);}
 @Override public InteractionResult use(Level world,Player p,InteractionHand hand){
  if(!fueled(p))return super.use(world,p,hand);
  if(p.getItemInHand(hand).nextDamageWillBreak())return InteractionResult.FAIL;
  p.startUsingItem(hand);return InteractionResult.CONSUME;
 }
 @Override public boolean releaseUsing(ItemStack stack,Level world,LivingEntity user,int left){
  if(!(user instanceof Player p)||!fueled(p))return super.releaseUsing(stack,world,user,left);
  if(getUseDuration(stack,p)-left<10||stack.nextDamageWillBreak())return false;
  if(!world.isClientSide()){
   stack.hurtAndBreak(1,p,p.getUsedItemHand());
   world.playSound(null,p.blockPosition(),SoundEvents.TRIDENT_RIPTIDE_2.value(),SoundSource.PLAYERS,1,0.88f);
  }
  if(p.onGround())p.move(MoverType.SELF,new Vec3(0,1.2,0));
  var v=p.getLookAngle().scale(2.25);
  p.push(v.x,v.y,v.z);p.startAutoSpinAttack(20,8f,stack);
  return true;
 }
}
