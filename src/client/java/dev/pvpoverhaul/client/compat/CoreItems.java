package dev.pvpoverhaul.client.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

/** Classification aliases used only by the optional bridge; never changes registries or stacks. */
public final class CoreItems {
    private CoreItems() {}
    public static void initialize() {
        if(net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("cartcore")
            .map(m->m.getMetadata().getVersion().getFriendlyString().equals("10.1")).orElse(false))
            dev.pvpoverhaul.Interactions.knifeReleased=player->{
                try { Class.forName("thetruetrident.cartcore.CartcoreClient").getField("shotBow").setBoolean(null,true); }
                catch(ReflectiveOperationException e) { throw new IllegalStateException("Item release adapter failed",e); }
            };
    }
    public static boolean cartSame(Object actual,Object expected) {
        if(same(actual,expected)) return true;
        var player=net.minecraft.client.Minecraft.getInstance().player;
        return expected==Items.BOW && player!=null && actual==player.getMainHandItem().getItem()
            && dev.pvpoverhaul.Interactions.canCartKnife(player,player.getMainHandItem());
    }
    public static void cartKey(com.mojang.blaze3d.platform.InputConstants.Key key,boolean pressed) {
        net.minecraft.client.KeyMapping.set(key,pressed);
        var client=net.minecraft.client.Minecraft.getInstance();
        if(!pressed && client.player!=null && client.gameMode!=null && client.player.isUsingItem()
            && dev.pvpoverhaul.Interactions.canCartKnife(client.player,client.player.getUseItem())) {
            client.options.keyUse.setDown(false);
            client.gameMode.releaseUsingItem(client.player);
        }
    }
    private static String path(Item item) {
        var id=BuiltInRegistries.ITEM.getKey(item);
        return id!=null && id.getNamespace().equals("pvpoverhaul") ? id.getPath() : "";
    }
    private static boolean axe(String p) { return p.endsWith("_battleaxe") || p.endsWith("_halberd") || p.endsWith("_flail"); }
    private static boolean sword(String p) { return p.endsWith("_knife") || p.endsWith("_scythe"); }
    public static String text(Object value) {
        String original=String.valueOf(value);
        if (!(value instanceof Item item)) return original;
        String p=path(item);
        if(p.endsWith("_halberd")) return original+" _spear _axe";
        if(axe(p)) return original+" _axe";
        if(sword(p)) return original+" _sword";
        if(p.endsWith("_hammer")) return original+" mace";
        return original;
    }
    public static boolean same(Object actual, Object expected) {
        if(actual==expected) return true;
        if(!(actual instanceof Item item) || !(expected instanceof Item)) return false;
        String p=path(item);
        if(p.isEmpty()) return false;
        if(expected==Items.BOW) return item instanceof BowItem;
        if(expected==Items.CROSSBOW) return item instanceof CrossbowItem;
        if(expected==Items.SHIELD) return item instanceof ShieldItem;
        if(expected==Items.MACE) return p.endsWith("_hammer");
        return false;
    }
    public static boolean matches(ItemStack stack,Object expected) {
        return same(stack.getItem(),expected);
    }
    public static boolean tagged(ItemStack stack,TagKey<Item> tag) {
        if(stack.is(tag)) return true;
        if(stack.isEmpty()) return false;
        String p=path(stack.getItem());
        return (tag==ItemTags.AXES && axe(p)) || (tag==ItemTags.SWORDS && sword(p)) || (tag==ItemTags.SPEARS && p.endsWith("_halberd"));
    }
    public static boolean holding(LivingEntity entity,Item expected) {
        return entity.isHolding(expected) || matches(entity.getMainHandItem(),expected) || matches(entity.getOffhandItem(),expected);
    }
    public static Component name(Item item,ItemStack stack) {
        String alias=text(item);
        return alias.equals(item.toString()) ? item.getName(stack) : item.getName(stack).copy().append(" "+alias);
    }
}
