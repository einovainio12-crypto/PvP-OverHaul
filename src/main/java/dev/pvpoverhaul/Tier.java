package dev.pvpoverhaul;

import net.minecraft.world.item.ToolMaterial;

public enum Tier {
    WOOD("wood", 0, 384, ToolMaterial.WOOD),
    COPPER("copper", 1, 512, ToolMaterial.COPPER),
    IRON("iron", 2, 768, ToolMaterial.IRON),
    GOLD("gold", 3, 256, ToolMaterial.GOLD),
    ROSE_GOLD("rose_gold", 2, 900, RoseGold.TOOL),
    DIAMOND("diamond", 4, 1561, ToolMaterial.DIAMOND),
    NETHERITE("netherite", 5, 2031, ToolMaterial.NETHERITE);

    public final String id;
    public final int rank;
    public final int durability;
    public final ToolMaterial material;
    Tier(String id, int rank, int durability, ToolMaterial material) {
        this.id = id; this.rank = rank; this.durability = durability; this.material = material;
    }
    public double damageMultiplier() { return 1.0 + rank * 0.05; }
    public double bowDamageMultiplier() { return Balance.bowMultiplier(rank); }
    public int bowDrawTicks() { return this==ROSE_GOLD?16:Balance.bowDrawTicks(rank); }
    public int rangedDurability() { return this==GOLD?48:this==ROSE_GOLD?384:durability; }
    public int magazineSize() { return rank < 2 ? 1 : rank + 1; }
    public boolean targetLock() { return this==IRON || this==ROSE_GOLD || this==DIAMOND || this==NETHERITE; }
    public boolean deadEye() { return this == IRON || this == ROSE_GOLD || this == DIAMOND || this == NETHERITE; }
    public float shieldDisableScale() { return 1.0f - rank * 0.12f; }
}
