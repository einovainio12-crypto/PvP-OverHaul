package dev.pvpoverhaul.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.pvpoverhaul.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class PvpOverhaulClient implements ClientModInitializer {
    private static KeyMapping zoom,lock,dead,boost,pole,overload,quiver,sheath;
    private boolean jumpWasDown;
    @Override public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(TargetLockPayload.TYPE,(payload,context)->TargetLockClient.receive(payload.entityId()));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->TargetLockClient.clear());
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(net.minecraft.world.entity.EntityTypes.FISHING_BOBBER,FlailHookRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(SlingshotItem.PEBBLE,net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        for(var type:WeaponEntities.TYPES.values()) net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(type,net.minecraft.client.renderer.entity.ThrownTridentRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(WeaponEntities.GENERIC,net.minecraft.client.renderer.entity.ThrownTridentRenderer::new);
        KeyMapping.Category category=KeyMapping.Category.register(PvpOverhaul.id("combat"));
        zoom=key("zoom",InputConstants.KEY_Z,category);
        lock=key("target_lock",InputConstants.KEY_R,category);
        dead=key("dead_eye",InputConstants.KEY_V,category);
        boost=key("boost",InputConstants.KEY_B,category);
        pole=key("pole_jump",InputConstants.KEY_G,category);
        overload=key("overload",InputConstants.KEY_O,category);
        sheath=key("sheath_swap",InputConstants.KEY_H,category);
        quiver=key("quiver",InputConstants.KEY_C,category);
        ItemTooltipCallback.EVENT.register((stack,context,flag,lines)->{
            Tier tier=stack.getItem() instanceof TierBowItem bow?bow.tier:
                stack.getItem() instanceof TierCrossbowItem crossbow && crossbow.tier==Tier.NETHERITE?crossbow.tier:null;
            if(stack.getItem() instanceof TierBowItem || stack.getItem() instanceof TierCrossbowItem)
                lines.add(Component.literal("Zoom: hold "+zoom.getTranslatedKeyMessage().getString()));
            if(Skills.canTargetLock(stack)) lines.add(Component.literal("Target Lock: "+lock.getTranslatedKeyMessage().getString()+", toggle tracking; sneak to release"));
            if(tier!=null && tier.deadEye()) lines.add(Component.literal("Dead Eye: "+dead.getTranslatedKeyMessage().getString()+", rapid burst at three nearest enemies"));
            if(stack.is(Items.ELYTRA)) {
                lines.add(Component.literal("Boost: "+boost.getTranslatedKeyMessage().getString()+" while gliding (requires Boost)"));
                lines.add(Component.literal("Overload: "+overload.getTranslatedKeyMessage().getString()+" while gliding (requires Overload)"));
                lines.add(Component.literal("Overload costs "+Balance.overloadCost(stack.getMaxDamage())+" durability; Unbreaking does not reduce it"));
            }
            if(stack.getItem() instanceof TierArrowItem arrow && arrow.ammo==Ammo.GRAPPLING) {
                lines.add(Component.literal("Hit a block/player to pull yourself; Sneak releases"));
                lines.add(Component.literal("48-block reach · 5-second tether · no direct damage"));
            }
            if(stack.is(ModItems.SHEATH)) lines.add(Component.literal(sheath.getTranslatedKeyMessage().getString()+": swap held weapon with sheath (carry in inventory or offhand)"));
            if(stack.is(ModItems.QUIVER)) {
                lines.add(Component.literal("Right-click: store arrows (9 stacks)"));
                lines.add(Component.literal(quiver.getTranslatedKeyMessage().getString()+": radial ammunition selector"));
                lines.add(Component.literal("Shift + "+quiver.getTranslatedKeyMessage().getString()+": open arrow storage"));
            }
            if(stack.is(ModItems.DRAGON_SCALE_WOLF_ARMOR)) lines.add(Component.literal("Equip your standing adult wolf; it follows your Elytra flight"));
        });
        ClientTickEvents.END_CLIENT_TICK.register(mc->{
            if (mc.player==null || mc.gui.screen()!=null) { jumpWasDown=false;return; }
            while(quiver.consumeClick()) {
                int slot=Quivers.find(mc.player);
                if(slot>=0) {
                    if(mc.player.isShiftKeyDown()) {
                        if(ClientPlayNetworking.canSend(QuiverSelectionPayload.TYPE)) ClientPlayNetworking.send(new QuiverSelectionPayload(slot,-2));
                        else mc.player.sendOverlayMessage(Component.literal("PvP+ must also be installed on the server."));
                    } else mc.gui.setScreen(new QuiverWheelScreen(slot));
                    return;
                }
                mc.player.sendOverlayMessage(Component.literal("Carry a quiver to select arrows."));
            }
            while(sheath.consumeClick()) send(SkillPayload.SHEATH_SWAP);
            while(lock.consumeClick()) send(SkillPayload.TARGET_LOCK);
            TargetLockClient.track();
            while(dead.consumeClick()) send(SkillPayload.DEAD_EYE);
            while(boost.consumeClick()) send(SkillPayload.BOOST);
            while(pole.consumeClick()) send(SkillPayload.POLE_JUMP);
            while(overload.consumeClick()) send(SkillPayload.OVERLOAD);
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
        else if(Minecraft.getInstance().player!=null)
            Minecraft.getInstance().player.sendOverlayMessage(net.minecraft.network.chat.Component.literal("PvP+ must also be installed on the server."));
    }
    public static boolean zooming() {
        var mc=Minecraft.getInstance();
        if (zoom==null || !zoom.isDown() || mc.player==null || mc.gui.screen()!=null) return false;
        var item=mc.player.getMainHandItem().getItem();
        var offhand=mc.player.getOffhandItem().getItem();
        return item instanceof TierBowItem || item instanceof TierCrossbowItem || offhand instanceof TierBowItem || offhand instanceof TierCrossbowItem;
    }
}
