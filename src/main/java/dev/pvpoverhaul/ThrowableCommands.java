package dev.pvpoverhaul;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
/** Command permissions, levels and exclusive sets remain vanilla; only Throwable's item restriction changes. */
public final class ThrowableCommands {
    private static final ThreadLocal<Integer> DEPTH=ThreadLocal.withInitial(()->0);
    public static void begin() { DEPTH.set(DEPTH.get()+1); }
    public static void end() { int depth=DEPTH.get()-1;if(depth<=0) DEPTH.remove();else DEPTH.set(depth); }
    public static boolean inCommand() { return DEPTH.get()>0; }
    public static void initialize() {
        EnchantmentEvents.ALLOW_ENCHANTING.register((enchantment,stack,context)->
            inCommand() && !stack.isEmpty() && enchantment.is(ResourceKey.create(Registries.ENCHANTMENT,PvpOverhaul.id("throwable")))?TriState.TRUE:TriState.DEFAULT);
    }
    private ThrowableCommands() {}
}
