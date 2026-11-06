package dev.pvpoverhaul;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Normal player breaks preserve claim checks, loot, Fortune and game-mode rules. */
public final class ScytheHarvest {
 private static boolean harvesting;
 public static boolean mature(BlockState s){return s.getBlock() instanceof CropBlock c && c.isMaxAge(s);}
 public static void initialize(){PlayerBlockBreakEvents.AFTER.register((world,player,center,state,entity)->{
  if(harvesting || !(player instanceof ServerPlayer p) || p.isShiftKeyDown() || !mature(state))return;
  var tool=p.getMainHandItem();if(!"scythe".equals(ModItems.KINDS.get(tool.getItem())))return;
  harvesting=true;
  try{
   if(!p.hasInfiniteMaterials())tool.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
   for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){
    if(x==0&&z==0 || tool.isEmpty() || p.getMainHandItem()!=tool)continue;
    BlockPos pos=center.offset(x,0,z);
    if(!world.hasChunkAt(pos)||!mature(world.getBlockState(pos)))continue;
    if(p.gameMode.destroyBlock(pos)&&!p.hasInfiniteMaterials())tool.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
   }
  }finally{harvesting=false;}
 });}
 private ScytheHarvest(){}
}
