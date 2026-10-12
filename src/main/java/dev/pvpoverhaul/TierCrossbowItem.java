package dev.pvpoverhaul;

import java.util.*;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;

public final class TierCrossbowItem extends CrossbowItem {
    public final Tier tier;
    public TierCrossbowItem(Properties properties,Tier tier) { super(properties);this.tier=tier; }
    @Override public Predicate<ItemStack> getAllSupportedProjectiles() { return s->Ammo.supported(tier,s,false); }
    @Override public Predicate<ItemStack> getSupportedHeldProjectiles() { return s->Ammo.supported(tier,s,true); }
    @Override public void onUseTick(Level level,LivingEntity user,ItemStack weapon,int remaining) {
        boolean wasCharged=isCharged(weapon);
        super.onUseTick(level,user,weapon,remaining);
        if (!(level instanceof ServerLevel) || wasCharged || !isCharged(weapon)) return;
        var volleys=new ArrayList<ItemStack>(weapon.getOrDefault(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.EMPTY).itemCopies().toList());
        // Vanilla draw handles Multishot and intangible copies. Each additional volley pays normal ammo cost.
        for (int i=1;i<tier.magazineSize();i++) {
            List<ItemStack> drawn=draw(weapon,user.getProjectile(weapon),user);
            if (drawn.isEmpty()) break;
            volleys.addAll(drawn);
        }
        weapon.set(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.ofNonEmpty(volleys));
    }
    @Override public void performShooting(Level level,LivingEntity shooter,InteractionHand hand,ItemStack weapon,float power,float uncertainty,LivingEntity target) {
        if (!(level instanceof ServerLevel)) return;
        var all=new ArrayList<ItemStack>(weapon.getOrDefault(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.EMPTY).itemCopies().toList());
        if (all.isEmpty()) return;
        // Each volley starts with a real projectile, followed by any vanilla Multishot copies.
        int end=1;
        while (end<all.size() && all.get(end).has(DataComponents.INTANGIBLE_PROJECTILE)) end++;
        // Creative's entire magazine is intangible: use the enchantment's count as the volley size.
        if (shooter.hasInfiniteMaterials()) end=Math.min(all.size(),net.minecraft.world.item.enchantment.EnchantmentHelper.processProjectileCount((ServerLevel)level,weapon,shooter,1));
        weapon.set(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.ofNonEmpty(all.subList(0,end)));
        float volleyPower=all.subList(0,end).stream().anyMatch(s->s.is(Items.FIREWORK_ROCKET))?1.6f:3.15f;
        super.performShooting(level,shooter,hand,weapon,volleyPower,uncertainty,target);
        if (all.subList(0,end).stream().anyMatch(s->s.getItem() instanceof ArrowItem)) Skills.finishShot(shooter);
        if (!weapon.isEmpty() && end<all.size()) weapon.set(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.ofNonEmpty(all.subList(end,all.size())));
        if (shooter instanceof net.minecraft.world.entity.player.Player player) player.getCooldowns().addCooldown(weapon,6);
    }
    @Override protected Projectile createProjectile(Level level,LivingEntity shooter,ItemStack weapon,ItemStack ammo,boolean crit) {
        Projectile p=super.createProjectile(level,shooter,weapon,ammo,crit);
        Combat.adjustProjectile(p,shooter,tier.damageMultiplier());return p;
    }
    @Override protected void shootProjectile(LivingEntity shooter,Projectile projectile,int index,float power,float uncertainty,float angle,LivingEntity target) {
        // Only netherite crossbows unlock the full skill set.
        if (!Skills.aim(shooter,projectile,power,angle))
            super.shootProjectile(shooter,projectile,index,power,Combat.uncertainty(projectile,shooter),angle,target);
    }
}
