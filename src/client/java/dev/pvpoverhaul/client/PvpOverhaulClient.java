package dev.pvpoverhaul.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.pvpoverhaul.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class PvpOverhaulClient implements ClientModInitializer {
    private static KeyMapping zoom,lock,dead,boost,pole;
    private boolean jumpWasDown;
    @Override public void onInitializeClient() {
        KeyMapping.Category category=KeyMapping.Category.register(PvpOverhaul.id("combat"));
        zoom=key("zoom",InputConstants.KEY_Z,category);
        lock=key("target_lock",InputConstants.KEY_R,category);
        dead=key("dead_eye",InputConstants.KEY_V,category);
        boost=key("boost",InputConstants.KEY_B,category);
        pole=key("pole_jump",InputConstants.KEY_G,category);
        ClientTickEvents.END_CLIENT_TICK.register(mc->{
            if (mc.player==null || mc.gui.screen()!=null) { jumpWasDown=false;return; }
            while(lock.consumeClick()) send(SkillPayload.TARGET_LOCK);
            while(dead.consumeClick()) send(SkillPayload.DEAD_EYE);
            while(boost.consumeClick()) send(SkillPayload.BOOST);
            while(pole.consumeClick()) send(SkillPayload.POLE_JUMP);
            boolean jump=mc.options.keyJump.isDown();
            if (jump && !jumpWasDown && !mc.player.onGround()) send(SkillPayload.DOUBLE_JUMP);
            jumpWasDown=jump;
        });
    }
    private static KeyMapping key(String name,int code,KeyMapping.Category category) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.pvpoverhaul."+name,InputConstants.Type.KEYBOARD,code,category));
    }
    private static void send(int action) {
        if (ClientPlayNetworking.canSend(SkillPayload.TYPE)) ClientPlayNetworking.send(new SkillPayload(action));
    }
    public static boolean zooming() {
        var mc=Minecraft.getInstance();
        if (zoom==null || !zoom.isDown() || mc.player==null || mc.gui.screen()!=null) return false;
        var item=mc.player.getMainHandItem().getItem();
        return item instanceof TierBowItem || item instanceof TierCrossbowItem;
    }
}
