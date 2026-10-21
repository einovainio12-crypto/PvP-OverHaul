package dev.pvpoverhaul.client;

import dev.pvpoverhaul.*;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

/** Click-based emote-style wheel: Escape cancels; selection changes only future ammunition draws. */
public final class QuiverWheelScreen extends Screen {
    private final int inventorySlot;
    private final IntConsumer selectionHandler;
    public QuiverWheelScreen(int inventorySlot) { this(inventorySlot,null); }
    public QuiverWheelScreen(int inventorySlot,IntConsumer selectionHandler) {
        super(Minecraft.getInstance(),Minecraft.getInstance()==null?null:Minecraft.getInstance().font,Component.translatable("container.pvpoverhaul.quiver"));
        this.inventorySlot=inventorySlot;this.selectionHandler=selectionHandler;
    }
    @Override protected void init() {
        for(int i=0;i<9;i++) {
            final int slot=i;int[] point=slotPoint(i);
            addWidget(Button.builder(Component.literal("Arrow slot "+(i+1)),button->choose(slot))
                .bounds(point[0]-18,point[1]-17,36,36).build());
        }
        int[] center=inventoryPoint();
        addWidget(Button.builder(Component.literal("Inventory ammunition"),button->choose(-1)).bounds(center[0]-27,center[1]-18,54,36).build());
    }
    private void choose(int slot) { if(selectionHandler!=null) selectionHandler.accept(slot);else select(slot); }
    private ItemStack quiver() { return minecraft.player==null?ItemStack.EMPTY:minecraft.player.getInventory().getItem(inventorySlot); }
    private boolean supported(ItemStack arrow) {
        if(minecraft.player==null || arrow.isEmpty()) return true;
        ItemStack main=minecraft.player.getMainHandItem(),off=minecraft.player.getOffhandItem();
        ItemStack held=main.getItem() instanceof ProjectileWeaponItem?main:off;
        return !(held.getItem() instanceof ProjectileWeaponItem ranged) || ranged.getAllSupportedProjectiles().test(arrow);
    }
    private double radius() { return Math.min(80,Math.min(width,height)*0.30); }
    private boolean grid() { return width<240 || height<210; }
    public int[] slotPoint(int slot) {
        if(grid()) return new int[]{width/2+(slot%3-1)*44,height/2+(slot/3-1)*40-15};
        double angle=slot*Math.PI*2/9-Math.PI/2;
        return new int[]{width/2+(int)Math.round(Math.cos(angle)*radius()),height/2+(int)Math.round(Math.sin(angle)*radius())};
    }
    private int[] inventoryPoint() { return new int[]{width/2,grid()?height/2+62:height/2}; }
    private int hovered(double x,double y) {
        int[] center=inventoryPoint();
        if(Math.abs(x-center[0])<=27 && Math.abs(y-center[1])<=18) return -1;
        for(int i=0;i<9;i++) {
            int[] point=slotPoint(i);
            if(x>=point[0]-18 && x<point[0]+18 && y>=point[1]-17 && y<point[1]+19) return i;
        }
        return -2;
    }
    private void select(int slot) {
        if(slot<-1 || slot>8 || !quiver().is(ModItems.QUIVER)) return;
        if(!ClientPlayNetworking.canSend(QuiverSelectionPayload.TYPE)) {
            minecraft.player.sendOverlayMessage(Component.literal("PvP+ must also be installed on the server."));onClose();return;
        }
        Quivers.select(minecraft.player,inventorySlot,slot);
        ClientPlayNetworking.send(new QuiverSelectionPayload(inventorySlot,slot));
        onClose();
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean isInGameUi() { return true; }
    @Override public void tick() { if(!quiver().is(ModItems.QUIVER)) onClose(); }
    @Override public boolean keyPressed(KeyEvent event) {
        if(event.key()>=49 && event.key()<=57) { choose(event.key()-49);return true; }
        if(event.key()>=321 && event.key()<=329) { choose(event.key()-321);return true; }
        if(event.key()==48 || event.key()==320) { choose(-1);return true; }
        return super.keyPressed(event);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTicks) {
        int cx=width/2,cy=height/2,hover=hovered(mouseX,mouseY),selected=Quivers.selected(quiver());
        double r=radius();int outer=(int)r+24;
        for(int y=-outer;y<=outer;y++) {
            int half=(int)Math.sqrt(outer*outer-y*y);g.fill(cx-half,cy+y,cx+half+1,cy+y+1,0xdd17212d);
        }
        List<ItemStack> arrows=Quivers.contents(quiver());
        for(int i=0;i<9;i++) {
            int[] point=slotPoint(i);int x=point[0],y=point[1];
            int color=!supported(arrows.get(i))?0xff713d43:i==hover?0xff846bb6:i==selected?0xff3c6f71:0xff2c3949;
            g.fill(x-18,y-17,x+18,y+19,color);
            if(!arrows.get(i).isEmpty()) g.item(arrows.get(i),x-8,y-12);
            g.text(font,Integer.toString(i+1),x-15,y-15,0xffb9c4d0);
            String count=arrows.get(i).isEmpty()?"—":Integer.toString(arrows.get(i).getCount());
            g.text(font,count,x-font.width(count)/2,y+6,0xffffffff);
        }
        int[] centerPoint=inventoryPoint();
        g.fill(centerPoint[0]-27,centerPoint[1]-18,centerPoint[0]+27,centerPoint[1]+18,hover==-1?0xff846bb6:0xff1d2935);
        String center="Inventory";g.text(font,center,centerPoint[0]-font.width(center)/2,centerPoint[1]-4,0xffffffff);
        String title="Quiver — select arrows";g.text(font,title,cx-font.width(title)/2,8,0xffffffff);
        String name=hover>=0?(arrows.get(hover).isEmpty()?"Empty slot — choose another slot or 0":arrows.get(hover).getHoverName().getString()):"Use inventory ammunition";
        if(hover>=0 && !supported(arrows.get(hover))) name+=" — not supported by held weapon";
        g.text(font,name,cx-font.width(name)/2,Math.min(height-28,cy+outer+5),0xffeeeeff);
        String help="Click / 1–9 · 0 inventory · Esc cancel";g.text(font,help,cx-font.width(help)/2,height-12,0xffb9c4d0);
    }
}
