package dev.pvpoverhaul;

import dev.pvpoverhaul.mixin.FireworkRocketAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Arrow flight followed by an ordinary one-star rocket explosion on first impact. */
public final class FireworkArrows {
    public static ItemStack explosionPayload() {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        rocket.set(DataComponents.FIREWORKS, explosionDefinition());
        return rocket;
    }

    public static Fireworks explosionDefinition() {
        FireworkExplosion star = new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL,
                IntList.of(0xff5b9b, 0xffcf43), IntList.of(), false, false);
        return new Fireworks(0, List.of(star));
    }

    public static boolean impact(AbstractArrow arrow, HitResult hit) {
        if (!(arrow.getPickupItemStackOrigin().getItem() instanceof TierArrowItem item)
                || item.ammo != Ammo.FIREWORK) return false;
        if (arrow.level() instanceof ServerLevel level && !arrow.isRemoved()) {
            Vec3 position = hit.getLocation();
            // Keep a block impact just outside the surface so vanilla visibility rays start in air.
            if (hit instanceof BlockHitResult block) {
                position = position.add(block.getDirection().getStepX() * 0.05,
                        block.getDirection().getStepY() * 0.05,
                        block.getDirection().getStepZ() * 0.05);
            }
            FireworkRocketEntity rocket = new FireworkRocketEntity(level, explosionPayload(),
                    arrow.getOwner(), position.x, position.y, position.z, true);
            rocket.setDeltaMovement(Vec3.ZERO);
            rocket.noPhysics = true;
            // Explode on the next server tick, after the native rocket's spawn reaches clients.
            // Use vanilla fireworks for particles, sound, armor, damage attribution and sight checks.
            ((FireworkRocketAccessor) rocket).pvp$setLifetime(0);
            level.addFreshEntity(rocket);
            arrow.discard();
        }
        // Replace direct arrow damage; do not stack a second hit or detonate repeatedly with Piercing.
        return true;
    }
    private FireworkArrows() {}
}
