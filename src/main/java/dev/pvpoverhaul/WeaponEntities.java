package dev.pvpoverhaul;
import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
/** Independently registered entity types, sharing native trident physics, saving and pickup. */
public final class WeaponEntities {
    public static final Map<Item,EntityType<WeaponProjectile>> TYPES=new IdentityHashMap<>();
    public static EntityType<WeaponProjectile> GENERIC;
    public static void initialize() {
        GENERIC=register("thrown_item");
        for(Item item:BuiltInRegistries.ITEM) {
            var id=BuiltInRegistries.ITEM.getKey(item);String path=id.getPath();
            if(item==Items.TRIDENT)continue;
            if(ModItems.KINDS.containsKey(item)||path.matches(".*_(knife|battleaxe|axe|sword|spear|bow|crossbow)$")||item==Items.MACE||item==Items.BOW||item==Items.CROSSBOW)
                TYPES.put(item,register("thrown_"+id.getNamespace()+"_"+path));
        }
    }
    private static EntityType<WeaponProjectile> register(String name) {
        var key=ResourceKey.create(Registries.ENTITY_TYPE,PvpOverhaul.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE,key,EntityType.Builder.<WeaponProjectile>of(WeaponProjectile::new,MobCategory.MISC).sized(.5f,.5f).clientTrackingRange(4).updateInterval(1).build(key));
    }
    public static WeaponProjectile create(Level level,LivingEntity owner,ItemStack stack) {
        WeaponProjectile e=new WeaponProjectile(TYPES.getOrDefault(stack.getItem(),GENERIC),level);e.initialize(owner,stack);return e;
    }
    public static final class WeaponProjectile extends ThrownTrident {
        public WeaponProjectile(EntityType<? extends ThrownTrident> type,Level level){super(type,level);}
        private void initialize(LivingEntity owner,ItemStack stack) {
            setOwner(owner);setPos(owner.getX(),owner.getEyeY()-.1,owner.getZ());setPickupItemStack(stack.copyWithCount(1));
            pickup=net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.ALLOWED;
            var access=(dev.pvpoverhaul.mixin.TridentDataAccessor)(Object)this;
            getEntityData().set(dev.pvpoverhaul.mixin.TridentDataAccessor.pvp$loyalty(),access.pvp$loyaltyFrom(stack));
            getEntityData().set(dev.pvpoverhaul.mixin.TridentDataAccessor.pvp$foil(),stack.hasFoil());
        }
    }
    public interface FlailHookVisual {
        ItemStack pvp$getFlail();
        void pvp$setFlail(ItemStack stack);
    }
    private WeaponEntities(){}
}
