package dev.pvpplusgear;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
public final class SheathKey{
 public static void init(){
  var key=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.pvpplusgear.sheath",InputConstants.Type.KEYBOARD,InputConstants.UNKNOWN.getValue(),KeyMapping.Category.register(Identifier.parse("pvpplusgear:gear"))){
   // An action should fire on a new key press, never from regaining mouse focus.
   @Override protected boolean shouldSetOnIngameFocus(){return false;}
  });
  ClientTickEvents.END_CLIENT_TICK.register(mc->{while(key.consumeClick())if(mc.player!=null&&mc.getConnection()!=null&&mc.gui.screen()==null)mc.getConnection().sendCommand("sheathswap");});
 }
}
