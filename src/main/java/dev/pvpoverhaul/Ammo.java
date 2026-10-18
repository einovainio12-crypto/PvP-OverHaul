package dev.pvpoverhaul;

import net.minecraft.world.item.*;

public enum Ammo {
    WOOD("wood_arrow",0,1.8f),
    COPPER("copper_arrow",1,0.8f), IRON("iron_arrow",2,0.6f), GOLD("gold_arrow",3,0.5f),
    DRAGON("dragon_scale_arrow",4,0.2f), NETHERITE("netherite_arrow",5,0.05f),
    ICE("ice_arrow",2,0.7f), BLUNT("blunt_arrow",2,0.9f), BLAZE("blaze_arrow",4,0.7f), BREEZE("breeze_arrow",4,0.7f),
    FIREWORK("firework_arrow",4,0.8f), FIRE_CHARGE("fire_charge_arrow",4,0.8f), GRAPPLING("grappling_arrow",0,1.0f);

    public final String id;
    public final int rank;
    public final float uncertainty;
    Ammo(String id,int rank,float uncertainty) { this.id=id; this.rank=rank; this.uncertainty=uncertainty; }
    public static boolean supported(Tier tier, ItemStack stack, boolean rockets) {
        if (stack.getItem() instanceof TierArrowItem arrow) {
            Ammo ammo=arrow.ammo;
            if (ammo==GOLD) return tier==Tier.GOLD || tier.rank>=4;
            if (ammo==IRON || ammo==BLUNT) return tier==Tier.IRON || tier.rank>=4;
            return ammo.rank<=tier.rank;
        }
        if (stack.is(Items.SPECTRAL_ARROW)) return tier==Tier.GOLD || tier.rank>=4;
        if (stack.is(Items.FIREWORK_ROCKET)) return rockets && tier.rank>=4;
        return ProjectileWeaponItem.ARROW_ONLY.test(stack);
    }
}
