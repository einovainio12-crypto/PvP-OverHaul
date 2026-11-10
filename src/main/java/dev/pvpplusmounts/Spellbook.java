package dev.pvpplusmounts;
import java.util.*;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
public final class Spellbook extends Item {
 record Page(String name,Item reagent,int count,int xp,int cooldown){}
 static final Page[] P={new Page("Fleetfoot",Items.SUGAR,2,1,5),new Page("Mend",Items.GLISTERING_MELON_SLICE,1,2,20),new Page("Night Sight",Items.GOLDEN_CARROT,1,1,5),new Page("Stone Skin",Items.IRON_INGOT,3,3,45),new Page("Featherfall",Items.FEATHER,2,1,10),new Page("Frost Nova",Items.SNOWBALL,4,3,20),new Page("Blink",Items.ENDER_PEARL,1,2,12),new Page("Orbital Barrage",Items.FIRE_CHARGE,16,15,180)};
 private Spellbook(Properties p){super(p);}
 static void register(){
  var key=ResourceKey.create(Registries.ITEM,Identifier.parse("pvpplusmagic:spellbook"));
  var book=Registry.register(BuiltInRegistries.ITEM,key,new Spellbook(new Properties().setId(key).stacksTo(1).rarity(Rarity.RARE).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true)));
  CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,Identifier.parse("minecraft:combat"))).register(o->o.accept(book));
 }
 static void say(ServerPlayer p,String s){p.sendOverlayMessage(Component.literal(s));}
 static String cost(Page pg){return pg.count+" "+new ItemStack(pg.reagent).getHoverName().getString()+" + "+pg.xp+" XP level(s)";}
 public InteractionResult use(Level world,Player player,InteractionHand hand){
  if(!(player instanceof ServerPlayer p)||!(world instanceof ServerLevel l))return InteractionResult.SUCCESS;
  var stack=p.getItemInHand(hand);
  int id=Math.floorMod(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr("pvpplus_page",0),P.length);
  if(p.isShiftKeyDown()){
   int next=(id+1)%P.length; CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putInt("pvpplus_page",next));
   say(p,(next+1)+"/8  "+P[next].name+" | "+cost(P[next]));return InteractionResult.SUCCESS;
  }
  var pg=P[id];if(p.isSpectator())return InteractionResult.FAIL;
  String prefix="pvpplusmagic:"+id+":";long now=l.getServer().overworld().getGameTime();
  for(String tag:List.copyOf(p.entityTags()))if(tag.startsWith(prefix)){
   try{long due=Long.parseLong(tag.substring(prefix.length()));if(due>now){say(p,pg.name+" ready in "+((due-now+19)/20)+"s");return InteractionResult.FAIL;}}catch(NumberFormatException ignored){}
   p.removeTag(tag);
  }
  var bag=p.getInventory().getNonEquipmentItems();
  if(!p.isCreative()&&(p.experienceLevel<pg.xp||bag.stream().filter(s->s.is(pg.reagent)).mapToInt(ItemStack::getCount).sum()<pg.count)){
   say(p,pg.name+" needs "+cost(pg));return InteractionResult.FAIL;}
  boolean ok=switch(id){
   case 0 -> buff(p,MobEffects.SPEED,36000);
   case 1 -> {if(p.getHealth()>=p.getMaxHealth())yield false;p.heal(8);yield true;}
   case 2 -> buff(p,MobEffects.NIGHT_VISION,12000);
   case 3 -> buff(p,MobEffects.RESISTANCE,600);
   case 4 -> buff(p,MobEffects.SLOW_FALLING,1200);
   case 5 -> frost(p,l);
   case 6 -> blink(p,l);
   default -> barrage(p,l);};
  if(!ok){say(p,pg.name+": no valid target, destination, or benefit.");return InteractionResult.FAIL;}
  if(!p.isCreative()){
   int left=pg.count;for(var s:bag){if(s.is(pg.reagent)){int n=Math.min(left,s.getCount());s.shrink(n);left-=n;if(left==0)break;}}
   p.getInventory().setChanged();p.giveExperienceLevels(-pg.xp);
  }
  // Per-spell cooldown, shared across books. Switching pages is always available.
  p.addTag("pvpplusmagic:"+id+":"+(l.getServer().overworld().getGameTime()+20L*pg.cooldown));
  l.playSound(null,p.blockPosition(),SoundEvents.ENCHANTMENT_TABLE_USE,SoundSource.PLAYERS,.8f,1f);
  say(p,pg.name+" cast.");return InteractionResult.SUCCESS;
 }
 static boolean buff(ServerPlayer p,Holder<MobEffect> e,int ticks){
  var old=p.getEffect(e);if(old!=null&&(old.getAmplifier()>0||old.isInfiniteDuration()||old.getDuration()>ticks-200))return false;
  return p.addEffect(new MobEffectInstance(e,ticks,0));
 }
 static boolean frost(ServerPlayer p,ServerLevel l){
  boolean hit=false;
  for(var e:l.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(6))){
   if(e==p||!e.isAlive()||e.isSpectator()||p.distanceToSqr(e)>36||p.isAlliedTo(e)||!p.hasLineOfSight(e)||e instanceof Player q&&!p.canHarmPlayer(q))continue;
   if(e.hurtServer(l,l.damageSources().indirectMagic(p,p),4)){e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,80,1));hit=true;}
  }
  if(hit)l.sendParticles(ParticleTypes.SNOWFLAKE,p.getX(),p.getY()+1,p.getZ(),60,2,.6,2,.05);
  return hit;
 }
 static boolean blink(ServerPlayer p,ServerLevel l){
  if(p.isPassenger())return false;Vec3 delta=null;
  for(int i=1;i<=24;i++){var v=p.getLookAngle().scale(i*.5);var box=p.getBoundingBox().move(v);
   if(!l.hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))||!l.getWorldBorder().isWithinBounds(box)||!l.noCollision(p,box)||box.minY<l.getMinY()||box.maxY>l.getMaxY()+1)break;
   if(i>=4)delta=v;
  }if(delta==null)return false;
  p.connection.teleport(p.getX()+delta.x,p.getY()+delta.y,p.getZ()+delta.z,p.getYRot(),p.getXRot());return true;
 }
 static boolean barrage(ServerPlayer p,ServerLevel l){
  Vec3 eye=p.getEyePosition();var hit=l.clip(new ClipContext(eye,eye.add(p.getLookAngle().scale(48)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
  if(hit.getType()!=HitResult.Type.BLOCK)return false;
  BlockPos center=hit.getBlockPos().above();
  int[][] offsets={{0,0},{3,0},{-3,0},{0,3},{0,-3}};
  for(var o:offsets){var b=center.offset(o[0],0,o[1]);if(!l.hasChunkAt(b)||!l.getWorldBorder().isWithinBounds(b)||!l.canSeeSky(b)||b.getY()+18>l.getMaxY())return false;}
  boolean spawned=false;
  for(var o:offsets){var v=Vec3.atBottomCenterOf(center.offset(o[0],18,o[1]));var fire=new LargeFireball(l,p,new Vec3(0,-1,0),1);fire.setPos(v);fire.setDeltaMovement(0,-.8,0);spawned|=l.addFreshEntity(fire);}
  return spawned;
 }
}
