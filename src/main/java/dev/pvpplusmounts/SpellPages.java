package dev.pvpplusmounts;
import java.util.List;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
final class SpellPages extends ChestMenu{
 static final int[] cells={10,12,14,16,19,21,23,25};
 static final String[] desc={"Speed I • 30 minutes","Restore four hearts","Night Vision • 10 minutes","Resistance I • 30 seconds","Slow Falling • one minute","Damage and slow nearby foes","Blink up to 12 blocks","Call down five fireballs"};
 final InteractionHand hand;final ItemStack book;
 static SimpleContainer icons(int active){
  var c=new SimpleContainer(27);
  for(int i=0;i<8;i++){var page=Spellbook.P[i];var icon=new ItemStack(page.reagent());
   icon.set(DataComponents.CUSTOM_NAME,Component.literal((i==active?"✓ ":"")+page.name()));
   icon.set(DataComponents.LORE,new ItemLore(List.of(Component.literal(desc[i]),Component.literal(Spellbook.cost(page)),Component.literal("Cooldown: "+page.cooldown()+"s"))));
   c.setItem(cells[i],icon);}return c;
 }
 SpellPages(int n,Inventory inv,InteractionHand h,ItemStack b,int active){super(MenuType.GENERIC_9x3,n,inv,icons(active),3);hand=h;book=b;}
 @Override public boolean stillValid(Player p){return p.getItemInHand(hand)==book;}
 @Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
 @Override public void clicked(int slot,int button,ContainerInput input,Player p){
  if(!stillValid(p)||input!=ContainerInput.PICKUP||!getCarried().isEmpty())return;
  for(int i=0;i<cells.length;i++)if(slot==cells[i]){final int page=i;CustomData.update(DataComponents.CUSTOM_DATA,book,t->t.putInt("pvpplus_page",page));if(p instanceof net.minecraft.server.level.ServerPlayer sp)sp.closeContainer();p.sendOverlayMessage(Component.literal("Selected: "+Spellbook.P[page].name()));return;}
 }
}
