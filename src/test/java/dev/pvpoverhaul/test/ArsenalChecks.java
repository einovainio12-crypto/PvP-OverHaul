package dev.pvpoverhaul.test;
import dev.pvpoverhaul.*;
import java.nio.file.Path;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import com.mojang.serialization.JsonOps;
public final class ArsenalChecks {
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void run(HolderLookup.Provider provider,Path path){
        check(ModItems.ARSENAL.size()==49,"Seven weapon kinds across seven materials");
        var unique=new java.util.HashSet<>(WeaponEntities.TYPES.values());
        check(unique.size()==WeaponEntities.TYPES.size(),"Weapon types share a registry ID");
        for(var entry:ModItems.ARSENAL.entrySet()){
            var type=WeaponEntities.TYPES.get(entry.getValue());check(type!=null,"Missing dedicated projectile "+entry.getKey());
            check(!BuiltInRegistries.ENTITY_TYPE.getKey(type).equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityTypes.TRIDENT)),"Weapon still registered as trident");
            ItemStack s=new ItemStack(entry.getValue());check(ThrownItemVisual.usesItemModel(s),"Missing original item model");
            check(Combat.thrownBaseDamage(s,8)>=3,"Missing thrown base damage");
        }
        for(Item item:new Item[]{Items.MACE,Items.IRON_AXE,Items.IRON_SWORD,Items.DIAMOND_SPEAR})check(WeaponEntities.TYPES.containsKey(item),"Missing native weapon entity");
        var registry=provider.lookupOrThrow(Registries.ENCHANTMENT);
        var flame=registry.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT,Identifier.withDefaultNamespace("flame")));
        for(Item item:ModItems.KNIVES.values())check(flame.value().getSupportedItems().contains(item.builtInRegistryHolder()),"Knife cannot take Flame");
        check(flame.value().getSupportedItems().contains(ModItems.STONE_KNIFE.builtInRegistryHolder()),"Stone knife Flame missing");
        var hammer=new ItemStack(ModItems.ARSENAL.get("netherite_hammer"));
        for(String n:new String[]{"pvpoverhaul:stormbound","pvpoverhaul:throwable","minecraft:loyalty","minecraft:channeling"}){
            var e=registry.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT,Identifier.parse(n)));check(e.value().getSupportedItems().contains(hammer.getItem().builtInRegistryHolder()),"Storm hammer rejects "+n);hammer.enchant(e,n.endsWith("loyalty")?3:1);
        }
        hammer.setDamageValue(71);hammer.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Storm hammer"));
        ItemStack sheath=new ItemStack(ModItems.SHEATH);sheath.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(java.util.List.of(hammer)));
        var ops=provider.createSerializationContext(JsonOps.INSTANCE);ItemStack loaded=ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,sheath).getOrThrow()).getOrThrow();
        check(ItemStack.isSameItemSameComponents(hammer,loaded.get(DataComponents.CONTAINER).itemCopies().findFirst().orElseThrow()),"Sheath lost weapon components");
        var ice=new ItemStack(BuiltInRegistries.ITEM.getValue(PvpOverhaul.id("ice_arrow")));check(ProjectileWeaponItem.ARROW_ONLY.test(ice),"Ice arrow absent from native ammunition tag");
        check(Ammo.supported(Tier.IRON,ice,false)&&!Ammo.supported(Tier.COPPER,ice,false),"Ice tier bounds incorrect");
        System.out.println("PVP_ARSENAL_49_WEAPONS_UNIQUE_ENTITIES_FLAME_STORM_SHEATH_AND_ICE_PASSED");
    }
}
