package dev.pvpoverhaul.client.mixin;

import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.extensibility.*;

/** Optional, version-scoped item classification at external call sites only. */
public final class CoreBridge implements IMixinConfigPlugin {
    private static final String HOOK = "dev/pvpoverhaul/client/compat/CoreItems";
    private static final String ITEM = "net/minecraft/world/item/Item";
    private static final String STACK = "net/minecraft/world/item/ItemStack";
    private static final String[][] TARGETS = {
        {"swordcore", "2.0.0", "thetruetrident.swordcore.SwordcoreClient"},
        {"mace-utils", "10.1", "thetruetrident.macecore.B"},
        {"mace-utils", "10.1", "thetruetrident.macecore.I"},
        {"mace-utils", "10.1", "thetruetrident.macecore.MacecoreClient"},
        {"mace-utils", "10.1", "thetruetrident.macecore.PearlCatcher"},
        {"mace-utils", "10.1", "thetruetrident.macecore.elySwap"},
        {"mace-utils", "10.1", "thetruetrident.macecore.elytraPot"},
        {"cartcore", "10.1", "thetruetrident.cartcore.CartcoreClient"},
        {"cartcore", "10.1", "thetruetrident.cartcore.CartcoreCore"},
        {"cartcore", "10.1", "thetruetrident.cartcore.autoWeb"},
        {"cartcore", "10.1", "thetruetrident.cartcore.crossCartin"},
        {"cartcore", "10.1", "thetruetrident.cartcore.replenishingMain"},
        {"spearcore", "10.1", "thetruetrident.spearcore.SpearcoreClient"},
        {"spearcore", "10.1", "thetruetrident.spearcore.SpearcoreCompat"},
        {"crystalcore", "10.1", "thetruetrident.crystalcore.clickCrystal"},
        {"crystalcore", "10.1", "thetruetrident.crystalcore.compatLayer"},
        {"crystalcore", "10.1", "thetruetrident.crystalcore.coreMainClient"},
        {"crystalcore", "10.1", "thetruetrident.crystalcore.crystalFlickLogic"},
        {"crystalcore", "10.1", "thetruetrident.crystalcore.dTapProtection"},
        {"crystalcore", "10.1", "thetruetrident.crystalcore.dragRight"},
    };
    public void onLoad(String pkg) {}
    public String getRefMapperConfig() { return null; }
    public boolean shouldApplyMixin(String target, String mixin) { return true; }
    public void acceptTargets(Set<String> mine, Set<String> others) {}
    public List<String> getMixins() {
        List<String> result = new ArrayList<>();
        for (int i=0;i<TARGETS.length;i++) {
            String[] t=TARGETS[i];
            if (FabricLoader.getInstance().getModContainer(t[0]).map(m ->
                m.getMetadata().getVersion().getFriendlyString().equals(t[1])).orElse(false))
                result.add("CoreBridge$Adapter"+i);
        }
        return result;
    }
    public void postApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
    public void preApply(String name, ClassNode node, String mixin, IMixinInfo info) {
        if (!mixin.contains("CoreBridge$Adapter")) return;
        transform(node);
    }
    private static AbstractInsnNode next(AbstractInsnNode n) {
        do { n=n.getNext(); } while(n!=null && n.getOpcode()<0);
        return n;
    }
    /** Stack-neutral rewrites preserve existing frames and control flow. */
    public static int transform(ClassNode node) {
        int edits=0;
        for (MethodNode method:node.methods) for(AbstractInsnNode ins:method.instructions.toArray()) {
            if(ins instanceof MethodInsnNode call) {
                String hook=null, desc=null;
                if(node.name.equals("thetruetrident/cartcore/CartcoreClient") && call.owner.equals("net/minecraft/client/KeyMapping") && call.name.equals("set") && call.desc.equals("(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V")) {
                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"cartKey",call.desc,false));edits++;continue;
                }
                if(call.owner.equals("java/lang/String") && call.name.equals("valueOf") && call.desc.equals("(Ljava/lang/Object;)Ljava/lang/String;")) {
                    hook="text"; desc=call.desc;
                } else if(call.owner.equals(ITEM) && call.name.equals("toString") && call.desc.equals("()Ljava/lang/String;")) {
                    hook="text";desc="(Ljava/lang/Object;)Ljava/lang/String;";
                } else if(call.owner.equals(STACK) && call.name.equals("is") && call.desc.equals("(Ljava/lang/Object;)Z")) {
                    hook="matches";desc="(L"+STACK+";Ljava/lang/Object;)Z";
                } else if(call.owner.equals(STACK) && call.name.equals("is") && call.desc.equals("(Lnet/minecraft/tags/TagKey;)Z")) {
                    hook="tagged";desc="(L"+STACK+";Lnet/minecraft/tags/TagKey;)Z";
                } else if(call.owner.startsWith("net/minecraft/") && call.name.equals("isHolding") && call.desc.equals("(L"+ITEM+";)Z")) {
                    hook="holding";desc="(Lnet/minecraft/world/entity/LivingEntity;L"+ITEM+";)Z";
                } else if(call.owner.equals(ITEM) && call.name.equals("getName") && next(call) instanceof MethodInsnNode after && after.name.equals("toString")) {
                    hook="name";desc="(L"+ITEM+";L"+STACK+";)Lnet/minecraft/network/chat/Component;";
                }
                if(hook!=null) { method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,hook,desc,false));edits++; }
            } else if(ins instanceof FieldInsnNode field && field.getOpcode()==Opcodes.GETSTATIC && field.owner.equals("net/minecraft/world/item/Items") && field.desc.equals("L"+ITEM+";") && next(field) instanceof JumpInsnNode jump && (jump.getOpcode()==Opcodes.IF_ACMPEQ || jump.getOpcode()==Opcodes.IF_ACMPNE)) {
                method.instructions.insertBefore(jump,new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,node.name.equals("thetruetrident/cartcore/CartcoreClient")?"cartSame":"same","(Ljava/lang/Object;Ljava/lang/Object;)Z",false));
                jump.setOpcode(jump.getOpcode()==Opcodes.IF_ACMPEQ?Opcodes.IFNE:Opcodes.IFEQ);edits++;
            }
        }
        return edits;
    }
    @Pseudo @Mixin(targets="thetruetrident.swordcore.SwordcoreClient", remap=false) public static abstract class Adapter0 {}
    @Pseudo @Mixin(targets="thetruetrident.macecore.B", remap=false) public static abstract class Adapter1 {}
    @Pseudo @Mixin(targets="thetruetrident.macecore.I", remap=false) public static abstract class Adapter2 {}
    @Pseudo @Mixin(targets="thetruetrident.macecore.MacecoreClient", remap=false) public static abstract class Adapter3 {}
    @Pseudo @Mixin(targets="thetruetrident.macecore.PearlCatcher", remap=false) public static abstract class Adapter4 {}
    @Pseudo @Mixin(targets="thetruetrident.macecore.elySwap", remap=false) public static abstract class Adapter5 {}
    @Pseudo @Mixin(targets="thetruetrident.macecore.elytraPot", remap=false) public static abstract class Adapter6 {}
    @Pseudo @Mixin(targets="thetruetrident.cartcore.CartcoreClient", remap=false) public static abstract class Adapter7 {}
    @Pseudo @Mixin(targets="thetruetrident.cartcore.CartcoreCore", remap=false) public static abstract class Adapter8 {}
    @Pseudo @Mixin(targets="thetruetrident.cartcore.autoWeb", remap=false) public static abstract class Adapter9 {}
    @Pseudo @Mixin(targets="thetruetrident.cartcore.crossCartin", remap=false) public static abstract class Adapter10 {}
    @Pseudo @Mixin(targets="thetruetrident.cartcore.replenishingMain", remap=false) public static abstract class Adapter11 {}
    @Pseudo @Mixin(targets="thetruetrident.spearcore.SpearcoreClient", remap=false) public static abstract class Adapter12 {}
    @Pseudo @Mixin(targets="thetruetrident.spearcore.SpearcoreCompat", remap=false) public static abstract class Adapter13 {}
    @Pseudo @Mixin(targets="thetruetrident.crystalcore.clickCrystal", remap=false) public static abstract class Adapter14 {}
    @Pseudo @Mixin(targets="thetruetrident.crystalcore.compatLayer", remap=false) public static abstract class Adapter15 {}
    @Pseudo @Mixin(targets="thetruetrident.crystalcore.coreMainClient", remap=false) public static abstract class Adapter16 {}
    @Pseudo @Mixin(targets="thetruetrident.crystalcore.crystalFlickLogic", remap=false) public static abstract class Adapter17 {}
    @Pseudo @Mixin(targets="thetruetrident.crystalcore.dTapProtection", remap=false) public static abstract class Adapter18 {}
    @Pseudo @Mixin(targets="thetruetrident.crystalcore.dragRight", remap=false) public static abstract class Adapter19 {}
}
