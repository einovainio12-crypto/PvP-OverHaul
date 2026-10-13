package dev.pvpoverhaul;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
/** Count qualifying shield-disable attempts; normal sword hits do not consume stun resistance. */
public final class ShieldStuns {
    private static final Map<LivingEntity,Resistance> HITS=new WeakHashMap<>();
    public static final class Resistance {
        private ItemStack shield=ItemStack.EMPTY;
        private int hits;
        private long lastHit;
        public boolean hit(ItemStack current,int threshold,long now) {
            if(shield!=current || now-lastHit>160) hits=0;
            shield=current;lastHit=now;hits++;
            if(hits>=threshold) { hits=0;return false; }
            return true;
        }
        public int hits() { return hits; }
    }
    public static int threshold(ItemStack shield) {
        Tier tier=ModItems.TIERS.get(shield.getItem());
        return tier!=null && ModItems.SHIELDS.containsValue(shield.getItem())?Balance.shieldStunHits(tier.rank):1;
    }
    public static boolean resist(LivingEntity defender,ItemStack shield,float seconds) {
        int threshold=threshold(shield);if(threshold<=1 || seconds<=0) return false;
        Resistance progress=HITS.computeIfAbsent(defender,key->new Resistance());
        if(!progress.hit(shield,threshold,defender.level().getGameTime())) { HITS.remove(defender);return false; }
        if(defender instanceof net.minecraft.server.level.ServerPlayer player)
            player.sendOverlayMessage(Component.literal("Shield stun resistance: "+progress.hits()+"/"+threshold+" hits"));
        return true;
    }
    private ShieldStuns() {}
}
