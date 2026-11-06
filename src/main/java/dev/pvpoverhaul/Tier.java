package dev.pvpoverhaul;

import net.minecraft.world.item.ToolMaterial;

public enum Tier {
    WOOD("wood", 0, 384, ToolMaterial.WOOD),
    COPPER("copper", 1, 512, ToolMaterial.COPPER),
    IRON("iron", 2, 768, ToolMaterial.IRON),
    GOLD("gold", 3, 256, ToolMaterial.GOLD),
    ROSE_GOLD("rose_gold", 2, 900, RoseGold.TOOL),
    DIAMOND("diamond", 4, 1561, ToolMaterial.DIAMOND),
    NETHERITE("netherite", 5, 2031, ToolMaterial.NETHERITE),
    BRASS("brass", 2, 650, CompatMaterials.tool("brass",650,8f,2f,18)),
    ZINC("zinc", 1, 220, CompatMaterials.tool("zinc",220,5f,1f,12)),
    BRONZE("bronze", 2, 600, CompatMaterials.tool("bronze",600,7f,2f,16)),
    STEEL("steel", 4, 1200, CompatMaterials.tool("steel",1200,8f,4f,12)),
    SILVER("silver", 2, 350, CompatMaterials.tool("silver",350,8f,2f,22)),
    LEAD("lead", 2, 450, CompatMaterials.tool("lead",450,5f,2f,12)),
    SIFTITE("siftite", 4, 1700, CompatMaterials.tool("siftite",1700,9f,4f,18)),
    NETHERITE_IRON("netherite_iron", 5, 2300, CompatMaterials.tool("netherite_iron",2300,9f,5f,16)),
    NETHERITE_GOLD("netherite_gold", 5, 2600, CompatMaterials.tool("netherite_gold",2600,10f,5f,18)),
    NETHERITE_EMERALD("netherite_emerald", 5, 2900, CompatMaterials.tool("netherite_emerald",2900,11f,5f,20)),
    NETHERITE_DIAMOND("netherite_diamond", 5, 3200, CompatMaterials.tool("netherite_diamond",3200,12f,5f,22));

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
    public boolean targetLock() { return rank>=2 && this!=GOLD; }
    public boolean deadEye() { return rank>=2 && this!=GOLD; }
    public float shieldDisableScale() { return 1.0f - rank * 0.12f; }
}
