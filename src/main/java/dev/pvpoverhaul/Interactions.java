package dev.pvpoverhaul;

import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;

public final class Interactions {
    public static void initialize() {
        UseItemCallback.EVENT.register(Interactions::throwItem);
        UseBlockCallback.EVENT.register((player,level,hand,hit)->{
            if(player.isShiftKeyDown() && Enchantments.level(player,player.getItemInHand(hand),"throwable")>0) return throwItem(player,level,hand);
            if (player.isSpectator() || hand!=InteractionHand.MAIN_HAND || !level.getBlockState(hit.getBlockPos()).is(Blocks.FLETCHING_TABLE)) return InteractionResult.PASS;
            return useFletching(player,level,hit);
        });
    }
    private static InteractionResult throwItem(Player player,Level level,InteractionHand hand) {
            ItemStack held=player.getItemInHand(hand);
            if (player.isSpectator() || player.getCooldowns().isOnCooldown(held)) return InteractionResult.PASS;
            boolean knife=ModItems.isKnife(held);
            boolean throwable=Enchantments.level(player,held,"throwable")>0;
            if (!knife && !(player.isShiftKeyDown() && throwable)) return InteractionResult.PASS;
            if (ModItems.isSpear(held) && (Enchantments.level(player,held,"minecraft:lunge")>0 || Enchantments.level(player,held,"pole_jump")>0)) return InteractionResult.FAIL;
            if (held.isDamageableItem() && held.nextDamageWillBreak()) return InteractionResult.FAIL;
            if (level instanceof ServerLevel server) {
                if(held.isDamageableItem()) held.hurtWithoutBreaking(1,player);
                ItemStack thrown=held.consumeAndReturn(1,player);
                ThrownTrident entity=Projectile.spawnProjectileFromRotation(ThrownTrident::new,server,thrown,player,0,knife?2.2f:2.5f,1);
                if (player.hasInfiniteMaterials()) entity.pickup=AbstractArrow.Pickup.CREATIVE_ONLY;
                player.getCooldowns().addCooldown(thrown,knife?8:15);
                server.playSound(null,player.getX(),player.getY(),player.getZ(),net.minecraft.sounds.SoundEvents.TRIDENT_THROW,net.minecraft.sounds.SoundSource.PLAYERS,1,1);
            }
            return InteractionResult.SUCCESS;
    }
    private static InteractionResult useFletching(Player player,Level level,BlockHitResult hit) {
            ItemStack material=player.getMainHandItem(), weapon=player.getOffhandItem();
            Tier tier=ModItems.TIERS.get(weapon.getItem());
            // Waxing preserves names, damage and enchantments. Applying honeycomb twice has no further effect.
            if (material.is(Items.HONEYCOMB) && tier==Tier.COPPER && (weapon.getItem() instanceof TierBowItem || weapon.getItem() instanceof TierCrossbowItem)) {
                if (weapon.getMaxDamage()>tier.durability) return openFletching(player,level,hit);
                if (level instanceof ServerLevel) { weapon.set(DataComponents.MAX_DAMAGE,tier.durability*3/2); material.consume(1,player); }
                return InteractionResult.SUCCESS;
            }
            Tier upgrade=material.is(Items.COPPER_INGOT)?Tier.COPPER:material.is(Items.IRON_INGOT)?Tier.IRON:material.is(Items.GOLD_INGOT)?Tier.GOLD:material.is(Items.DIAMOND)?Tier.DIAMOND:material.is(Items.NETHERITE_INGOT)?Tier.NETHERITE:null;
            if (upgrade==null || (tier!=null && tier.rank>=upgrade.rank)) return openFletching(player,level,hit);
            boolean bow=weapon.getItem() instanceof BowItem, crossbow=weapon.getItem() instanceof CrossbowItem;
            if (!bow && !crossbow) return openFletching(player,level,hit);
            int cost=upgrade==Tier.NETHERITE?1:3;
            if (!player.hasInfiniteMaterials() && material.getCount()<cost) return InteractionResult.FAIL;
            if (level instanceof ServerLevel) {
                Item target=(bow?ModItems.BOWS:ModItems.CROSSBOWS).get(upgrade);
                // Transmute copies component patches; exclude old tier's waxing so new default durability applies.
                ItemStack result=weapon.transmuteCopy(target,1);
                result.remove(DataComponents.MAX_DAMAGE); // Explicitly restore new item's default below.
                int durability=target.getDefaultInstance().getMaxDamage();
                result.set(DataComponents.MAX_DAMAGE,durability);
                result.setDamageValue(Math.min(weapon.getDamageValue(),durability-1));
                player.setItemInHand(InteractionHand.OFF_HAND,result);material.consume(cost,player);
            }
            return InteractionResult.SUCCESS;
    }
    private static InteractionResult openFletching(Player player,Level level,BlockHitResult hit) {
        if(level instanceof ServerLevel) player.openMenu(new SimpleMenuProvider(
            (id,inventory,user)->new FletchingMenu(id,inventory,ContainerLevelAccess.create(level,hit.getBlockPos())),
            Component.translatable("container.pvpoverhaul.fletching")));
        return InteractionResult.SUCCESS;
    }
    private Interactions() {}
}
