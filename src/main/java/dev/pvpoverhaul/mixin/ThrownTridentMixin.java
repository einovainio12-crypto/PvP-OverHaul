package dev.pvpoverhaul.mixin;
import dev.pvpoverhaul.Combat;
import dev.pvpoverhaul.ThrownItemVisual;
import dev.pvpoverhaul.ThrownTridentReturnSlot;
import net.minecraft.network.syncher.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.projectile.arrow.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin implements ThrownItemVisual, ThrownTridentReturnSlot {
    @Unique private static final EntityDataAccessor<ItemStack> PVP_VISUAL_ITEM=SynchedEntityData.defineId(ThrownTrident.class,EntityDataSerializers.ITEM_STACK);
    @Unique private int pvp$returnSlot=-1;
    @Inject(method="defineSynchedData",at=@At("RETURN"))
    private void pvp$defineVisual(SynchedEntityData.Builder builder,CallbackInfo ci) { builder.define(PVP_VISUAL_ITEM,ItemStack.EMPTY); }
    @Inject(method={"<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
        "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"},at=@At("RETURN"))
    private void pvp$initialVisual(CallbackInfo ci) { pvp$syncVisual(); }
    @Inject(method="readAdditionalSaveData",at=@At("RETURN"))
    private void pvp$restoreVisual(ValueInput input,CallbackInfo ci) { pvp$syncVisual(); }
    @Inject(method="<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",at=@At("RETURN"))
    private void pvp$rememberReturnSlot(CallbackInfo ci) {
        ThrownTrident self=(ThrownTrident)(Object)this;
        if(self.getOwner() instanceof Player player)
            pvp$setReturnSlot(player.getUsedItemHand()==InteractionHand.OFF_HAND
                ? Inventory.SLOT_OFFHAND : player.getInventory().getSelectedSlot());
    }
    @Inject(method="addAdditionalSaveData",at=@At("RETURN"))
    private void pvp$saveReturnSlot(ValueOutput output,CallbackInfo ci) {
        if(pvp$returnSlot>=0) output.putInt("pvpplus_loyalty_return_slot",pvp$returnSlot);
    }
    @Inject(method="readAdditionalSaveData",at=@At("RETURN"))
    private void pvp$loadReturnSlot(ValueInput input,CallbackInfo ci) {
        pvp$returnSlot=input.getIntOr("pvpplus_loyalty_return_slot",-1);
    }
    @Inject(method="tryPickup",at=@At("HEAD"),cancellable=true)
    private void pvp$returnToOriginalSlot(Player player,CallbackInfoReturnable<Boolean> cir) {
        ThrownTrident self=(ThrownTrident)(Object)this;
        if(pvp$returnSlot<0 || !self.isNoPhysics() || self.getOwner()!=player
            || self.pickup!=AbstractArrow.Pickup.ALLOWED
            || self.getEntityData().get(dev.pvpoverhaul.mixin.TridentDataAccessor.pvp$loyalty())<=0) return;
        Inventory inventory=player.getInventory();
        if(pvp$returnSlot>=Inventory.INVENTORY_SIZE) return;
        ItemStack returned=self.getPickupItemStackOrigin().copy(),existing=inventory.getItem(pvp$returnSlot);
        if(existing.isEmpty()) inventory.setItem(pvp$returnSlot,returned);
        else if(existing.isStackable() && ItemStack.isSameItemSameComponents(existing,returned)
            && existing.getCount()<existing.getMaxStackSize()) existing.grow(returned.getCount());
        else return;
        cir.setReturnValue(true);
    }
    @Inject(method="tick",at=@At("HEAD"))
    private void pvp$refreshVisual(CallbackInfo ci) { pvp$syncVisual(); }
    @Unique private void pvp$syncVisual() {
        ThrownTrident self=(ThrownTrident)(Object)this;
        if(self.level().isClientSide()) return;
        ItemStack source=self.getPickupItemStackOrigin(),current=self.getEntityData().get(PVP_VISUAL_ITEM);
        ItemStack next=ThrownItemVisual.usesItemModel(source)?source:ItemStack.EMPTY;
        if(!ItemStack.isSameItemSameComponents(current,next) || current.getCount()!=(next.isEmpty()?0:1))
            self.getEntityData().set(PVP_VISUAL_ITEM,next.isEmpty()?ItemStack.EMPTY:next.copyWithCount(1));
    }
    @Override public ItemStack pvp$getVisualItem() { return ((ThrownTrident)(Object)this).getEntityData().get(PVP_VISUAL_ITEM); }
    @Override public void pvp$setReturnSlot(int slot) { pvp$returnSlot=slot; }
    @Redirect(method="onHitEntity",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean pvp$weaponEffects(net.minecraft.world.entity.Entity victim,net.minecraft.world.damagesource.DamageSource source,float damage) {
        boolean hit=victim.hurtOrSimulate(source,damage);ThrownTrident self=(ThrownTrident)(Object)this;
        if(hit && self.getOwner() instanceof net.minecraft.world.entity.player.Player p && victim instanceof net.minecraft.world.entity.LivingEntity target) dev.pvpoverhaul.Arsenal.hit(p,target,self.getPickupItemStackOrigin());
        if(hit) Combat.thrownMaceBurst(self,victim);
        return hit;
    }
    // Wind Burst is dispatched once at impact; preserve all other native post-hit effects.
    @ModifyArg(method="onHitEntity",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/enchantment/EnchantmentHelper;doPostAttackEffectsWithItemSourceOnBreak(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)V"),index=3)
    private ItemStack pvp$skipDuplicateWindBurst(ItemStack stack) { return Combat.withoutThrownWindBurst(stack); }
    @ModifyConstant(method="onHitEntity",constant=@Constant(floatValue=8f))
    private float pvp$thrownDamage(float vanilla) { return Combat.thrownDamage((AbstractArrow)(Object)this,vanilla); }
}
