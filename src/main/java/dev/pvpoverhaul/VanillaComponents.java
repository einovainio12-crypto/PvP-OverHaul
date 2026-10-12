package dev.pvpoverhaul;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
public final class VanillaComponents {
    public static void initialize() {
        DefaultItemComponentEvents.MODIFY.register(context->context.modify(Items.TRIDENT,builder->builder.set(
            DataComponents.ATTRIBUTE_MODIFIERS,TridentItem.createAttributes().withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
            new AttributeModifier(PvpOverhaul.id("trident_reach"),1,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND))));
    }
    private VanillaComponents() {}
}
