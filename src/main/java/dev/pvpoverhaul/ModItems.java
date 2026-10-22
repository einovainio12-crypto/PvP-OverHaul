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
    public static final Map<String,Item> ARSENAL=new LinkedHashMap<>();
    public static final Map<Item,String> KINDS=new IdentityHashMap<>();
    public static Item SLINGSHOT;
    public static Item SHEATH,STORM_CORE,HEALING_TOTEM,COMBAT_CACHE;
    public static final List<Item> ALL=new ArrayList<>();
    public static final Map<Item,Tier> TIERS=new IdentityHashMap<>();
    public static Item DRAGON_SCALE,STONE_BATTLEAXE,STONE_KNIFE,STONE_BOW,STONE_CROSSBOW,DRAGON_SCALE_WOLF_ARMOR,QUIVER;

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
        SLINGSHOT=register("slingshot",new Item.Properties().durability(128).repairable(Items.STRING),SlingshotItem::new);
        DRAGON_SCALE=register("dragon_scale",new Item.Properties().rarity(Rarity.RARE),Item::new);
        DRAGON_SCALE_WOLF_ARMOR=register("dragon_scale_wolf_armor",new Item.Properties()
            .wolfArmor(net.minecraft.world.item.equipment.ArmorMaterials.ARMADILLO_SCUTE).durability(128).repairable(DRAGON_SCALE).rarity(Rarity.RARE)
            .component(DataComponents.EQUIPPABLE,net.minecraft.world.item.equipment.Equippable.builder(EquipmentSlot.BODY)
                .setEquipSound(net.minecraft.world.item.equipment.ArmorMaterials.ARMADILLO_SCUTE.equipSound())
                .setAsset(ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID,PvpOverhaul.id("dragon_scale")))
                .setAllowedEntities(net.minecraft.world.entity.EntityTypes.WOLF).setCanBeSheared(true)
                .setShearingSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ARMOR_UNEQUIP_WOLF)).build()),Item::new);
        QUIVER=register("quiver",new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER,ItemContainerContents.EMPTY),QuiverItem::new);
        // Basic stone ranged weapons retain vanilla firing behavior and sit below copper in durability.
        STONE_BOW=register("stone_bow",new Item.Properties().durability(448)
            .enchantable(ToolMaterial.STONE.enchantmentValue()).repairable(ToolMaterial.STONE.repairItems()),BowItem::new);
        STONE_CROSSBOW=register("stone_crossbow",new Item.Properties().durability(448)
            .enchantable(ToolMaterial.STONE.enchantmentValue()).repairable(ToolMaterial.STONE.repairItems())
            .component(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.EMPTY),CrossbowItem::new);
        for (Tier t:Tier.values()) {
            if (t!=Tier.WOOD) {
                BOWS.put(t,tiered(t,"bow",base(t).durability(t.rangedDurability()),p->new TierBowItem(p,t)));
                CROSSBOWS.put(t,tiered(t,"crossbow",base(t).durability(t.rangedDurability()).component(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.EMPTY),p->new TierCrossbowItem(p,t)));
            }
            KNIVES.put(t,tiered(t,"knife",base(t).attributes(melee(3+t.rank,3.2,-1))
                .component(DataComponents.WEAPON,new Weapon(1)),Item::new));
            // Extend the existing game item instead of registering a second spear per material.
            Item spear=switch(t) {
                case WOOD -> Items.WOODEN_SPEAR;
                case COPPER -> Items.COPPER_SPEAR;
                case IRON -> Items.IRON_SPEAR;
                case GOLD -> Items.GOLDEN_SPEAR;
                case DIAMOND -> Items.DIAMOND_SPEAR;
                case NETHERITE -> Items.NETHERITE_SPEAR;
            };
            SPEARS.put(t,spear);TIERS.put(spear,t);
            BATTLEAXES.put(t,tiered(t,"battleaxe",base(t).axe(t.material,8,-3.2f)
                .component(DataComponents.WEAPON,new Weapon(1,5.0f)),Item::new));
            SHIELDS.put(t,tiered(t,"shield",base(t).equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(DataComponents.BLOCKS_ATTACKS, context->new BlocksAttacks(0.25f,t.shieldDisableScale(),
                    List.of(new BlocksAttacks.DamageReduction(90f,Optional.empty(),0,1)),
                    new BlocksAttacks.ItemDamageFunction(3,1,1),Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                    Optional.of(SoundEvents.SHIELD_BLOCK),Optional.of(SoundEvents.SHIELD_BREAK)))
                .component(DataComponents.BREAK_SOUND,SoundEvents.SHIELD_BREAK),ShieldItem::new));
        }
        STONE_BATTLEAXE=register("stone_battleaxe",new Item.Properties()
            .durability(ToolMaterial.STONE.durability()).enchantable(ToolMaterial.STONE.enchantmentValue())
            .repairable(ToolMaterial.STONE.repairItems()).axe(ToolMaterial.STONE,8,-3.2f)
            .component(DataComponents.WEAPON,new Weapon(1,5.0f)),Item::new);
        STONE_KNIFE=register("stone_knife",new Item.Properties()
            .durability(ToolMaterial.STONE.durability()).enchantable(ToolMaterial.STONE.enchantmentValue())
            .repairable(ToolMaterial.STONE.repairItems()).attributes(melee(4,3.2,-1))
            .component(DataComponents.WEAPON,new Weapon(1)),Item::new);
        for(String material:new String[]{"wood","stone","copper","iron","gold","diamond","netherite"}) {
            Tier tier=Arrays.stream(Tier.values()).filter(t->t.id.equals(material)).findFirst().orElse(null);
            int rank=tier==null?1:tier.rank;
            for(String kind:new String[]{"flail","hammer","longsword","scythe","halberd"}) {
                double damage=switch(kind){case "hammer"->9;case "flail"->7;case "longsword","halberd"->6;case "scythe"->5;default->4;};
                double speed=switch(kind){case "hammer"->.7;case "flail"->.9;case "halberd"->1;case "scythe"->1.1;case "longsword"->1.3;default->2.5;};
                double reach=kind.equals("halberd")?1.25:List.of("longsword","scythe").contains(kind)?.75:0;
                Item.Properties props=tier==null?new Item.Properties().durability(131).enchantable(5).repairable(ToolMaterial.STONE.repairItems()):base(tier);
                if(kind.equals("halberd")) {
                    // Same per-material kinetic timings as vanilla spears. Components are
                    // constructed here: vanilla item components are not bound at registration time.
                    float[] v=switch(material) {
                        case "wood" -> new float[]{.7f,.75f,5,14,10,15};
                        case "stone" -> new float[]{.82f,.7f,4.5f,13,9,13.75f};
                        case "copper" -> new float[]{.82f,.65f,4,12,8.25f,12.5f};
                        case "iron" -> new float[]{.95f,.6f,2.5f,11,6.75f,11.25f};
                        case "gold" -> new float[]{.7f,.7f,3.5f,13,8.5f,13.75f};
                        case "diamond" -> new float[]{1.075f,.5f,3,10,6.5f,10};
                        default -> new float[]{1.2f,.4f,2.5f,9,5.5f,8.75f};
                    };
                    props.component(DataComponents.KINETIC_WEAPON,new KineticWeapon(10,(int)(v[1]*20),
                        KineticWeapon.Condition.ofAttackerSpeed((int)(v[2]*20),v[3]),
                        KineticWeapon.Condition.ofAttackerSpeed((int)(v[4]*20),5.1f),
                        KineticWeapon.Condition.ofRelativeSpeed((int)(v[5]*20),4.6f),.38f,v[0],
                        Optional.of(material.equals("wood")?SoundEvents.SPEAR_WOOD_USE:SoundEvents.SPEAR_USE),
                        Optional.of(material.equals("wood")?SoundEvents.SPEAR_WOOD_HIT:SoundEvents.SPEAR_HIT)))
                        .component(DataComponents.ATTACK_RANGE,new AttackRange(0,4.5f,0,6.5f,.125f,.5f))
                        .component(DataComponents.USE_EFFECTS,new UseEffects(true,false,1));
                }
                Item item=register(material+"_"+kind,props.attributes(melee(damage+rank,speed,reach)).component(DataComponents.WEAPON,new Weapon(1,List.of("hammer","halberd","flail").contains(kind)?5f:0f)),kind.equals("hammer")?HammerItem::new:Item::new);
                ARSENAL.put(material+"_"+kind,item);KINDS.put(item,kind);if(tier!=null)TIERS.put(item,tier);
            }
        }
        SHEATH=register("weapon_sheath",new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER,ItemContainerContents.EMPTY),Item::new);
        STORM_CORE=register("storm_core",new Item.Properties().rarity(Rarity.EPIC),Item::new);
        COMBAT_CACHE=register("combat_cache",new Item.Properties().rarity(Rarity.RARE),Item::new);
        HEALING_TOTEM=register("healing_totem",new Item.Properties().durability(64).rarity(Rarity.RARE),Item::new);
        for (Ammo a:Ammo.values()) register(a.id,new Item.Properties(),p->new TierArrowItem(p,a));
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,net.minecraft.resources.Identifier.withDefaultNamespace("combat"))).register(output->ALL.forEach(output::accept));
    }
    public static boolean isKnife(ItemStack s) { return s.is(STONE_KNIFE) || KNIVES.containsValue(s.getItem()); }
    public static boolean isSpear(ItemStack s) { return s.is(net.minecraft.tags.ItemTags.SPEARS); }
    public static boolean isBattleaxe(ItemStack s) { return s.is(STONE_BATTLEAXE) || BATTLEAXES.containsValue(s.getItem()); }
    private ModItems() {}
}
