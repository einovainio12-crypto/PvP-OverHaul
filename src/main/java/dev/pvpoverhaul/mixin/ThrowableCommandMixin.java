package dev.pvpoverhaul.mixin;
import java.util.Collection;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.pvpoverhaul.ThrowableCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(EnchantCommand.class)
public abstract class ThrowableCommandMixin {
    @Shadow private static int enchant(CommandSourceStack source,Collection<? extends Entity> targets,Holder<Enchantment> selected,int level) throws CommandSyntaxException { throw new AssertionError(); }
    @Redirect(method={"lambda$register$0","lambda$register$1"},at=@At(value="INVOKE",target="Lnet/minecraft/server/commands/EnchantCommand;enchant(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Collection;Lnet/minecraft/core/Holder;I)I"))
    private static int pvp$commandScope(CommandSourceStack source,Collection<? extends Entity> targets,Holder<Enchantment> selected,int level) throws CommandSyntaxException {
        ThrowableCommands.begin();
        try { return enchant(source,targets,selected,level); }
        finally { ThrowableCommands.end(); }
    }
}
