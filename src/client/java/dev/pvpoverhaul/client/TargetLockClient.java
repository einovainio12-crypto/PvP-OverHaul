package dev.pvpoverhaul.client;
import dev.pvpoverhaul.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.*;
public final class TargetLockClient {
 private static int targetId=-1,expires;
 private static Object world;
 public static void receive(int id){var mc=Minecraft.getInstance();targetId=id;world=mc.level;expires=mc.player==null?0:mc.player.tickCount+30;}
 public static void clear(){targetId=-1;world=null;}
 public static LivingEntity target(){
  var mc=Minecraft.getInstance();
  if(mc.player==null||mc.level==null||world!=mc.level||mc.player.tickCount>expires||mc.player.isShiftKeyDown()||!mc.player.isAlive()){clear();return null;}
  var e=mc.level.getEntity(targetId);
  if(!(e instanceof LivingEntity t)||!t.isAlive()||mc.player.distanceToSqr(t)>Balance.LOCK_RANGE*Balance.LOCK_RANGE){clear();return null;}
  return t;
 }
 public static boolean track(){
  var mc=Minecraft.getInstance();if(mc.gui.screen()!=null)return false;
  LivingEntity t=target();if(t==null)return false;
  var d=t.getBoundingBox().getCenter().subtract(mc.player.getEyePosition());
  mc.player.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));
  mc.player.setXRot((float)-Math.toDegrees(Math.atan2(d.y,d.horizontalDistance())));
  return true;
 }
 private TargetLockClient(){}
}
