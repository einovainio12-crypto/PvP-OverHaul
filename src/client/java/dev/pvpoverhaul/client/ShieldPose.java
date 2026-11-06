package dev.pvpoverhaul.client;
import com.mojang.blaze3d.vertex.PoseStack;
/** Balanced even when another renderer cancels or throws. */
public final class ShieldPose {
 public static void render(PoseStack poses,boolean shield,Runnable render){
  if(!shield){render.run();return;}
  poses.pushPose();
  try{poses.translate(0,-.28,0);render.run();}finally{poses.popPose();}
 }
 private ShieldPose(){}
}
