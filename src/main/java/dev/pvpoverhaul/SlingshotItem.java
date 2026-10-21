package dev.pvpoverhaul;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
/** One untiered slingshot; cobblestone ammunition, 128 uses, twelve ticks to fully draw. */
public final class SlingshotItem extends Item {
 public static EntityType<Pebble> PEBBLE;
 public SlingshotItem(Properties properties){super(properties);}
 public static void initialize(){
  var key=ResourceKey.create(Registries.ENTITY_TYPE,PvpOverhaul.id("slingshot_pebble"));
  PEBBLE=Registry.register(BuiltInRegistries.ENTITY_TYPE,key,EntityType.Builder.<Pebble>of(Pebble::new,MobCategory.MISC).sized(.25f,.25f).clientTrackingRange(4).updateInterval(1).build(key));
 }
 private static ItemStack ammo(Player p){
  for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.is(Items.COBBLESTONE))return s;}
  return ItemStack.EMPTY;
 }
 @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
  if(ammo(player).isEmpty()&&!player.hasInfiniteMaterials())return InteractionResult.FAIL;
  player.startUsingItem(hand);return InteractionResult.CONSUME;
 }
 @Override public int getUseDuration(ItemStack stack,LivingEntity user){return 72000;}
 @Override public ItemUseAnimation getUseAnimation(ItemStack stack){return ItemUseAnimation.BOW;}
 @Override public boolean releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining){
  if(!(level instanceof ServerLevel server)||!(entity instanceof Player p))return false;
  int ticks=getUseDuration(stack,p)-remaining;if(ticks<4)return false;
  var ammunition=ammo(p);if(ammunition.isEmpty()&&!p.hasInfiniteMaterials())return false;
  Pebble pebble=new Pebble(PEBBLE,level);pebble.setOwner(p);pebble.setPos(p.getX(),p.getEyeY()-.1,p.getZ());pebble.setItem(new ItemStack(Items.COBBLESTONE));
  pebble.shootFromRotation(p,p.getXRot(),p.getYRot(),0,Math.min(1f,ticks/12f)*2.5f,1);
  if(!server.addFreshEntity(pebble))return false;
  ammunition.consume(1,p);stack.hurtAndBreak(1,p,p.getUsedItemHand());p.getCooldowns().addCooldown(stack,8);
  level.playSound(null,p.getX(),p.getY(),p.getZ(),net.minecraft.sounds.SoundEvents.SNOWBALL_THROW,net.minecraft.sounds.SoundSource.PLAYERS,.7f,.8f);
  return true;
 }
 public static final class Pebble extends Snowball {
  public Pebble(EntityType<? extends Snowball> type,Level level){super(type,level);}
  @Override protected Item getDefaultItem(){return Items.COBBLESTONE;}
  @Override protected void onHitEntity(EntityHitResult hit){
   if(level() instanceof ServerLevel server){
    if(getOwner() instanceof Player owner && hit.getEntity() instanceof Player other && !owner.canHarmPlayer(other))return;
    hit.getEntity().hurtServer(server,damageSources().thrown(this,getOwner()),4f);
   }
  }
 }
}
