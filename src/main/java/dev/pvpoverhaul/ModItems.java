package dev.pvpoverhaul;

import java.util.*;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;

public final class ModItems {
    public static final Map<Tier,Item> BOWS=new EnumMap<>(Tier.class), CROSSBOWS=new EnumMap<>(Tier.class),
        KNIVES=new EnumMap<>(Tier.class), SPEARS=new EnumMap<>(Tier.class),
        BATTLEAXES=new EnumMap<>(Tier.class), SHIELDS=new EnumMap<>(Tier.class);
    public static final List<Item> ALL=new ArrayList<>();
    public static final Map<Item,Tier> TIERS=new IdentityHashMap<>();
    public static Item DRAGON_SCALE;

    private static Item register(String path, Item.Properties properties, Function<Item.Properties,Item> factory) {
        ResourceKey<Item> key=ResourceKey.create(Registries.ITEM,PvpOverhaul.id(path));
        Item item=Registry.register(BuiltInRegistries.ITEM,key,factory.apply(properties.setId(key)));
        ALL.add(item);
        return item;
    }
    private static Item tiered(Tier t,String suffix,Item.Properties props,Function<Item.Properties,Item> factory) {
        Item item=register(t.id+"_"+suffix,props,factory); TIERS.put(item,t); return item;
    }
    private static Item.Properties base(Tier t) {
        Item.Properties p=new Item.Properties().durability(t.durability).enchantable(t.material.enchantmentValue())
            .repairable(t.material.repairItems());
        return t==Tier.NETHERITE ? p.fireResistant() : p;
    }
    private static ItemAttributeModifiers melee(double damage, double speed, double reach) {
        var builder=ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,damage-1,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,speed-4,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
        if (reach!=0) builder.add(Attributes.ENTITY_INTERACTION_RANGE,
            new AttributeModifier(PvpOverhaul.id("weapon_reach"),reach,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
        return builder.build();
    }
    public static void initialize() {
        DRAGON_SCALE=register("dragon_scale",new Item.Properties().rarity(Rarity.RARE),Item::new);
        for (Tier t:Tier.values()) {
            BOWS.put(t,tiered(t,"bow",base(t),p->new TierBowItem(p,t)));
            CROSSBOWS.put(t,tiered(t,"crossbow",base(t).component(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.EMPTY),p->new TierCrossbowItem(p,t)));
            KNIVES.put(t,tiered(t,"knife",base(t).attributes(melee(3+t.rank,3.2,-1))
                .component(DataComponents.WEAPON,new Weapon(1)),Item::new));
            SPEARS.put(t,tiered(t,"spear",new Item.Properties().spear(t.material,0.95f,0.95f,0.6f,2.5f,11f,6.75f,5.1f,11.25f,4.6f),Item::new));
            BATTLEAXES.put(t,tiered(t,"battleaxe",base(t).axe(t.material,8,-3.2f)
                .component(DataComponents.WEAPON,new Weapon(1,5.0f)),Item::new));
            SHIELDS.put(t,tiered(t,"shield",base(t).equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(DataComponents.BLOCKS_ATTACKS, context->new BlocksAttacks(0.25f,t.shieldDisableScale(),
                    List.of(new BlocksAttacks.DamageReduction(90f,Optional.empty(),0,1)),
                    new BlocksAttacks.ItemDamageFunction(3,1,1),Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                    Optional.of(SoundEvents.SHIELD_BLOCK),Optional.of(SoundEvents.SHIELD_BREAK)))
                .component(DataComponents.BREAK_SOUND,SoundEvents.SHIELD_BREAK),ShieldItem::new));
        }
        for (Ammo a:Ammo.values()) register(a.id,new Item.Properties(),p->new TierArrowItem(p,a));
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,net.minecraft.resources.Identifier.withDefaultNamespace("combat"))).register(output->ALL.forEach(output::accept));
    }
    public static boolean isKnife(ItemStack s) { return KNIVES.containsValue(s.getItem()); }
    public static boolean isSpear(ItemStack s) { return SPEARS.containsValue(s.getItem()) || s.has(DataComponents.KINETIC_WEAPON); }
    public static boolean isBattleaxe(ItemStack s) { return BATTLEAXES.containsValue(s.getItem()); }
    private ModItems() {}
}
