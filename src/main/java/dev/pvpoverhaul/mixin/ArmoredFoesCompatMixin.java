package dev.pvpoverhaul.mixin;
import net.minecraft.world.entity.*;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Mob.class)
public abstract class ArmoredFoesCompatMixin {
 @Inject(method="finalizeSpawn",at=@At("RETURN"))
 private void pvp$materials(ServerLevelAccessor level,DifficultyInstance difficulty,EntitySpawnReason reason,SpawnGroupData group,CallbackInfoReturnable<SpawnGroupData> cir){dev.pvpoverhaul.ArmoredFoesCompat.onSpawn((Mob)(Object)this,reason);}
}
