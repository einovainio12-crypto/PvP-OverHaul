package dev.pvpoverhaul.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.pvpoverhaul.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

/** Material-specific flail head and interlocking chain; ordinary rods use vanilla rendering. */
public final class FlailHookRenderer extends FishingHookRenderer {
    public static final class State extends FishingHookRenderState { public Identifier texture; public Vec3 viewOffset=Vec3.ZERO; }
    public FlailHookRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public FishingHookRenderState createRenderState() { return new State(); }
    @Override public void extractRenderState(FishingHook hook,FishingHookRenderState state,float partial) {
        super.extractRenderState(hook,state,partial);
        State custom=(State)state;
        var flail=((WeaponEntities.FlailHookVisual)hook).pvp$getFlail();
        custom.texture="flail".equals(ModItems.KINDS.get(flail.getItem()))
            ?PvpOverhaul.id("textures/item/"+BuiltInRegistries.ITEM.getKey(flail.getItem()).getPath()+".png"):null;
        custom.viewOffset=Vec3.ZERO;
        var mc=Minecraft.getInstance();
        var owner=hook.getPlayerOwner();
        // Only the hooked enemy sees the head close to their first-person camera.
        if(custom.texture!=null && mc.player!=null && hook.getHookedIn()==mc.player
            && owner!=null && owner!=mc.player && !owner.isAlliedTo(mc.player) && owner.canHarmPlayer(mc.player)
            && mc.options.getCameraType().isFirstPerson()) {
            Vec3 face=mc.player.getEyePosition(partial).add(mc.player.getViewVector(partial).scale(.48));
            custom.viewOffset=face.subtract(hook.getPosition(partial)).subtract(0,.25,0);
            custom.lineOriginOffset=custom.lineOriginOffset.subtract(custom.viewOffset);
        }
    }
    @Override public void submit(FishingHookRenderState state,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera) {
        if(!(state instanceof State custom)||custom.texture==null) { super.submit(state,poses,collector,camera);return; }
        poses.pushPose();poses.translate(custom.viewOffset.x,custom.viewOffset.y,custom.viewOffset.z);
        poses.pushPose();poses.translate(0,.25,0);poses.rotateDegrees(Axis.YP,state.ageInTicks*6);
        // UVs sample only the 3x3 head pixels (12..14) from the existing 16x16 flail sprite.
        collector.submitCustomGeometry(poses,RenderTypes.entityCutoutCull(custom.texture),(pose,buffer)->{
            float a=-.14f,b=.14f;
            face(buffer,pose,state.lightCoords,new float[][]{{a,a,b},{b,a,b},{b,b,b},{a,b,b}},0,0,1);
            face(buffer,pose,state.lightCoords,new float[][]{{b,a,a},{a,a,a},{a,b,a},{b,b,a}},0,0,-1);
            face(buffer,pose,state.lightCoords,new float[][]{{b,a,b},{b,a,a},{b,b,a},{b,b,b}},1,0,0);
            face(buffer,pose,state.lightCoords,new float[][]{{a,a,a},{a,a,b},{a,b,b},{a,b,a}},-1,0,0);
            face(buffer,pose,state.lightCoords,new float[][]{{a,b,b},{b,b,b},{b,b,a},{a,b,a}},0,1,0);
            face(buffer,pose,state.lightCoords,new float[][]{{a,a,a},{b,a,a},{b,a,b},{a,a,b}},0,-1,0);
        });
        poses.popPose();
        Vec3 end=state.lineOriginOffset;
        float width=Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth*1.4f;
        collector.submitCustomGeometry(poses,RenderTypes.lines(),(pose,buffer)->{
            int links=Math.max(1,Math.min(192,(int)Math.ceil(end.length()/.16)));
            for(int i=0;i<links;i++) {
                double t=(i+.5)/links;
                Vec3 center=point(end,t),tangent=new Vec3(end.x,end.y*(t+.5),end.z).normalize();
                if(tangent.lengthSqr()<1e-8)continue;
                Vec3 side=tangent.cross(Math.abs(tangent.y)>.9?new Vec3(1,0,0):new Vec3(0,1,0)).normalize();
                if((i&1)==1)side=tangent.cross(side).normalize();
                double halfLength=Math.max(.09,end.length()/links*.6);
                for(int k=0;k<8;k++) {
                    Vec3 v1=ring(center,tangent,side,halfLength,k*Math.PI/4);
                    Vec3 v2=ring(center,tangent,side,halfLength,(k+1)*Math.PI/4);
                    Vec3 normal=v2.subtract(v1).normalize();
                    int color=(i&1)==0?0xffb6bec6:0xff737d87;
                    lineVertex(buffer,pose,v1,normal,color,width);lineVertex(buffer,pose,v2,normal,color,width);
                }
            }
        });
        poses.popPose();
    }
    private static final Identifier GRAPPLE_HEAD=PvpOverhaul.id("textures/item/grappling_hook.png");
    private static final Identifier GRAPPLE_ROPE=PvpOverhaul.id("textures/item/grappling_rope.png");
    /** Custom grappling sprite and textured tether, independent of vanilla fishing visuals. */
    public static void submitGrapple(FishingHookRenderState state,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera) {
        poses.pushPose();poses.translate(0,.25,0);poses.rotate(camera.orientation);
        collector.submitCustomGeometry(poses,RenderTypes.entityCutoutCull(GRAPPLE_HEAD),(pose,buffer)->{
            texturedQuad(buffer,pose,state.lightCoords,new Vec3(-.28,-.28,0),new Vec3(.28,-.28,0),new Vec3(.28,.28,0),new Vec3(-.28,.28,0));
        });
        poses.popPose();
        Vec3 end=state.lineOriginOffset;
        if(!Double.isFinite(end.lengthSqr())||end.lengthSqr()<1e-8)return;
        collector.submitCustomGeometry(poses,RenderTypes.entityCutoutCull(GRAPPLE_ROPE),(pose,buffer)->{
            int segments=Math.max(1,Math.min(128,(int)Math.ceil(end.length()*2)));
            for(int i=0;i<segments;i++) {
                double t0=(double)i/segments,t1=(double)(i+1)/segments;
                Vec3 a=point(end,t0).add(0,.22*(1-t0),0),b=point(end,t1).add(0,.22*(1-t1),0);
                Vec3 tangent=b.subtract(a).normalize();
                if(tangent.lengthSqr()<1e-8)continue;
                Vec3 side=tangent.cross(Math.abs(tangent.y)>.9?new Vec3(1,0,0):new Vec3(0,1,0)).normalize().scale(.018);
                Vec3 up=tangent.cross(side).normalize().scale(.018);
                // Crossed, double-sided ribbons keep the braided texture visible from every angle.
                texturedQuad(buffer,pose,state.lightCoords,a.subtract(side),a.add(side),b.add(side),b.subtract(side));
                texturedQuad(buffer,pose,state.lightCoords,b.subtract(side),b.add(side),a.add(side),a.subtract(side));
                texturedQuad(buffer,pose,state.lightCoords,a.subtract(up),a.add(up),b.add(up),b.subtract(up));
                texturedQuad(buffer,pose,state.lightCoords,b.subtract(up),b.add(up),a.add(up),a.subtract(up));
            }
        });
    }
    private static void texturedQuad(VertexConsumer b,PoseStack.Pose pose,int light,Vec3 a,Vec3 c,Vec3 d,Vec3 e) {
        Vec3[] points={a,c,d,e};float[][] uv={{0,1},{1,1},{1,0},{0,0}};
        Vec3 normal=c.subtract(a).cross(d.subtract(a)).normalize();
        for(int i=0;i<4;i++)b.addVertex(pose,(float)points[i].x,(float)points[i].y,(float)points[i].z).setColor(-1)
            .setUv(uv[i][0],uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
            .setNormal(pose,(float)normal.x,(float)normal.y,(float)normal.z);
    }
    private static Vec3 point(Vec3 end,double t) { return new Vec3(end.x*t,end.y*(t*t+t)*.5+.25,end.z*t); }
    private static Vec3 ring(Vec3 center,Vec3 tangent,Vec3 side,double length,double angle) {
        return center.add(tangent.scale(Math.cos(angle)*length)).add(side.scale(Math.sin(angle)*.035));
    }
    private static void lineVertex(VertexConsumer b,PoseStack.Pose pose,Vec3 v,Vec3 normal,int color,float width) {
        b.addVertex(pose,(float)v.x,(float)v.y,(float)v.z).setColor(color)
            .setNormal(pose,(float)normal.x,(float)normal.y,(float)normal.z).setLineWidth(width);
    }
    private static void face(VertexConsumer b,PoseStack.Pose pose,int light,float[][] points,float nx,float ny,float nz) {
        float u0=12f/16,u1=15f/16,v0=12f/16,v1=15f/16;
        float[][] uv={{u0,v1},{u1,v1},{u1,v0},{u0,v0}};
        for(int i=0;i<4;i++)b.addVertex(pose,points[i][0],points[i][1],points[i][2]).setColor(-1)
            .setUv(uv[i][0],uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,nx,ny,nz);
    }
}
