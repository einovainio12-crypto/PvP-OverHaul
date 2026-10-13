package dev.pvpoverhaul.client;

import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public final class WeaponProjectileRenderState extends ThrownTridentRenderState {
    public final ItemStackRenderState weapon=new ItemStackRenderState();
    public boolean customWeapon;
    public static final class Grapple extends net.minecraft.client.renderer.entity.state.TippableArrowRenderState {
        public final net.minecraft.client.renderer.entity.state.FishingHookRenderState bobber=new net.minecraft.client.renderer.entity.state.FishingHookRenderState();
        public boolean grappling;
    }
}
