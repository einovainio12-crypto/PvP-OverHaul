package dev.pvpplusgear;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
public final class SheathKey{
 public static void init(){
  var key=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.pvpplusgear.sheath",InputConstants.Type.KEYBOARD,-1,KeyMapping.Category.register(Identifier.parse("pvpplusgear:gear"))));
  ClientTickEvents.END_CLIENT_TICK.register(mc->{while(key.consumeClick())if(mc.player!=null&&mc.getConnection()!=null&&mc.gui.screen()==null)mc.getConnection().sendCommand("sheathswap");});
 }
}
