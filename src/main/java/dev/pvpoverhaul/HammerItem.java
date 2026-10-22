package dev.pvpoverhaul;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Storm Core awakening enables a charged, dry-land Riptide. Sneak-use remains Throwable. */
public final class HammerItem extends Item {
    public HammerItem(Properties properties) { super(properties); }
    public static boolean awakened(ItemStack stack) {
        return stack.getItem()==ModItems.ARSENAL.get("netherite_hammer")
            && Enchantments.level(stack,"minecraft:riptide")>0;
    }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.TRIDENT; }
    @Override public int getUseDuration(ItemStack stack,LivingEntity user) { return 72000; }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(!awakened(stack)||player.isShiftKeyDown()) return InteractionResult.PASS;
        if(player.isPassenger()||stack.nextDamageWillBreak()||player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
    @Override public boolean releaseUsing(ItemStack stack,Level level,LivingEntity user,int remaining) {
        if(!(user instanceof Player player)||!awakened(stack)||player.isPassenger()
            ||getUseDuration(stack,user)-remaining<10||stack.nextDamageWillBreak()
            ||player.getCooldowns().isOnCooldown(stack)) return false;
        float strength=EnchantmentHelper.getTridentSpinAttackStrength(stack,player);
        if(strength<=0) return false;
        // Match vanilla Riptide client prediction and server movement; deliberately omit the water gate.
        Vec3 impulse=player.getLookAngle().scale(strength);
        player.push(impulse.x,impulse.y,impulse.z);
        player.startAutoSpinAttack(20,8.0f,stack);
        if(player.onGround()) player.move(MoverType.SELF,new Vec3(0,1.2,0));
        if(level instanceof ServerLevel) {
            stack.hurtWithoutBreaking(4,player);
            player.awardStat(Stats.ITEM_USED.get(this));
            level.playSound(null,player,SoundEvents.TRIDENT_RIPTIDE_3.value(),SoundSource.PLAYERS,1,1);
        }
        return true;
    }
}
