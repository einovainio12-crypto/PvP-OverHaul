package dev.pvpoverhaul.mixin;

import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fabric intentionally returns null for a resource whose load conditions fail. */
@Pseudo
@Mixin(targets="com.zurrtum.create.content.processing.RecipeLoaderTask",remap=false)
public abstract class CreateRecipeLoaderMixin {
 @Inject(method="apply(Lnet/minecraft/resources/Identifier;Lnet/minecraft/server/packs/resources/Resource;)Lnet/minecraft/resources/RegistryLoadTask$PendingRegistration;",
  at=@At(value="INVOKE",target="Lnet/minecraft/resources/RegistryLoadTask$PendingRegistration;value()Lcom/mojang/datafixers/util/Either;"),cancellable=true, locals=LocalCapture.CAPTURE_FAILHARD)
 private void pvp$skipConditionalRecipe(Identifier id,Resource resource,CallbackInfoReturnable<Object> cir,
   @Coerce Object entry){
  if(entry==null)cir.setReturnValue(null);
 }
}
