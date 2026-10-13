package dev.pvpoverhaul.client;

import dev.pvpoverhaul.*;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Click-based emote-style wheel: Escape cancels; selection changes only future ammunition draws. */
public final class QuiverWheelScreen extends Screen {
    private final int inventorySlot;
    public QuiverWheelScreen(int inventorySlot) { super(Component.translatable("container.pvpoverhaul.quiver"));this.inventorySlot=inventorySlot; }
    private ItemStack quiver() { return minecraft.player==null?ItemStack.EMPTY:minecraft.player.getInventory().getItem(inventorySlot); }
    private double radius() { return Math.min(80,Math.min(width,height)*0.30); }
    private int hovered(double x,double y) {
        double dx=x-width/2.0,dy=y-height/2.0;
        if(dx*dx+dy*dy<24*24) return -1;
        if(dx*dx+dy*dy>(radius()+28)*(radius()+28)) return -2;
        double angle=Math.atan2(dy,dx)+Math.PI/2;
        if(angle<0) angle+=Math.PI*2;
        return (int)Math.floor(angle/(Math.PI*2/9)+0.5)%9;
    }
    private void select(int slot) {
        if(slot<-1 || slot>8 || !quiver().is(ModItems.QUIVER)) return;
        if(ClientPlayNetworking.canSend(QuiverSelectionPayload.TYPE)) ClientPlayNetworking.send(new QuiverSelectionPayload(inventorySlot,slot));
        onClose();
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean isInGameUi() { return true; }
    @Override public void tick() { if(!quiver().is(ModItems.QUIVER)) onClose(); }
    @Override public boolean keyPressed(KeyEvent event) {
        if(event.key()>=49 && event.key()<=57) { select(event.key()-49);return true; }
        if(event.key()==48) { select(-1);return true; }
        return super.keyPressed(event);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick) {
        if(event.button()==0) { int slot=hovered(event.x(),event.y());if(slot>=-1) select(slot);return true; }
        return super.mouseClicked(event,doubleClick);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTicks) {
        int cx=width/2,cy=height/2,hover=hovered(mouseX,mouseY),selected=Quivers.selected(quiver());
        double r=radius();int outer=(int)r+24;
        for(int y=-outer;y<=outer;y++) {
            int half=(int)Math.sqrt(outer*outer-y*y);g.fill(cx-half,cy+y,cx+half+1,cy+y+1,0xdd17212d);
        }
        List<ItemStack> arrows=Quivers.contents(quiver());
        for(int i=0;i<9;i++) {
            double angle=i*Math.PI*2/9-Math.PI/2;
            int x=cx+(int)Math.round(Math.cos(angle)*r),y=cy+(int)Math.round(Math.sin(angle)*r);
            int color=i==hover?0xff846bb6:i==selected?0xff3c6f71:0xff2c3949;
            g.fill(x-18,y-17,x+18,y+19,color);
            if(!arrows.get(i).isEmpty()) g.item(arrows.get(i),x-8,y-12);
            g.text(font,Integer.toString(i+1),x-15,y-15,0xffb9c4d0);
            String count=arrows.get(i).isEmpty()?"—":Integer.toString(arrows.get(i).getCount());
            g.text(font,count,x-font.width(count)/2,y+6,0xffffffff);
        }
        g.fill(cx-27,cy-18,cx+27,cy+18,hover==-1?0xff846bb6:0xff1d2935);
        String center="Inventory";g.text(font,center,cx-font.width(center)/2,cy-4,0xffffffff);
        String title="Quiver — select arrows";g.text(font,title,cx-font.width(title)/2,8,0xffffffff);
        String name=hover>=0?(arrows.get(hover).isEmpty()?"Empty slot — inventory fallback":arrows.get(hover).getHoverName().getString()):"Use inventory ammunition";
        g.text(font,name,cx-font.width(name)/2,Math.min(height-28,cy+outer+5),0xffeeeeff);
        String help="Click / 1–9 · 0 inventory · Esc cancel";g.text(font,help,cx-font.width(help)/2,height-12,0xffb9c4d0);
    }
}
