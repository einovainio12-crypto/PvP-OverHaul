package dev.pvpoverhaul.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.pvpoverhaul.PvpOverhaul;
import java.io.Reader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Native wolf skeleton with the supplied armor mesh and scaled dragon wings. */
public final class DragonWolfArmorMesh {
    private static float f(JsonArray array,int index) { return array.get(index).getAsFloat(); }
    public static LayerDefinition layer(Reader reader) {
        JsonObject data=JsonParser.parseReader(reader).getAsJsonObject();
        MeshDefinition mesh=new MeshDefinition();PartDefinition root=mesh.getRoot();
        for(var entry:data.getAsJsonArray("parts")) {
            JsonObject part=entry.getAsJsonObject();String name=part.get("name").getAsString();
            JsonArray pivot=part.getAsJsonArray("pivot"),rotation=part.getAsJsonArray("rotation");
            CubeListBuilder cubes=CubeListBuilder.create();
            for(var value:part.getAsJsonArray("cubes")) {
                JsonObject cube=value.getAsJsonObject();JsonArray origin=cube.getAsJsonArray("origin"),size=cube.getAsJsonArray("size"),uv=cube.getAsJsonArray("uv");
                cubes.texOffs(uv.get(0).getAsInt(),uv.get(1).getAsInt()).mirror(cube.get("mirror").getAsBoolean())
                    .addBox(f(origin,0),f(origin,1),f(origin,2),f(size,0),f(size,1),f(size,2),new CubeDeformation(cube.get("inflate").getAsFloat()));
            }
            PartPose pose=PartPose.offsetAndRotation(f(pivot,0),f(pivot,1),f(pivot,2),f(rotation,0),f(rotation,1),f(rotation,2));
            PartDefinition definition;
            if(name.equals("head") || name.equals("tail")) {
                definition=root.addOrReplaceChild(name,CubeListBuilder.create(),pose)
                    .addOrReplaceChild("real_"+name,cubes,PartPose.ZERO);
            } else definition=root.addOrReplaceChild(name,cubes,pose);
            if(name.equals("upper_body")) addScaledWings(definition);
        }
        return LayerDefinition.create(mesh,data.get("texture_width").getAsInt(),data.get("texture_height").getAsInt());
    }
    private static void addScaledWings(PartDefinition upperBody) {
        CubeDeformation wingInflation=new CubeDeformation(0.1f);
        upperBody.addOrReplaceChild("left_wing",CubeListBuilder.create().texOffs(44,14)
            .addBox(0,-4,-1,7,8,2,wingInflation),PartPose.offset(3,0,0));
        upperBody.addOrReplaceChild("right_wing",CubeListBuilder.create().texOffs(44,14).mirror()
            .addBox(-7,-4,-1,7,8,2,wingInflation),PartPose.offset(-3,0,0));
    }
    public static AdultWolfModel load() {
        try(var reader=Minecraft.getInstance().getResourceManager().getResourceOrThrow(
                PvpOverhaul.id("models/entity/dragon_scale_wolf_armor.json")).openAsReader()) {
            return new DragonScaleArmorModel(layer(reader).bakeRoot());
        } catch(Exception error) { throw new IllegalStateException("Cannot load Dragon Scale wolf armor mesh",error); }
    }
    private static final class DragonScaleArmorModel extends AdultWolfModel {
        private final ModelPart leftWing,rightWing;
        private DragonScaleArmorModel(ModelPart root) {
            super(root);
            ModelPart upperBody=root.getChild("upper_body");
            leftWing=upperBody.getChild("left_wing");rightWing=upperBody.getChild("right_wing");
        }
        @Override public void setupAnim(net.minecraft.client.renderer.entity.state.WolfRenderState state) {
            super.setupAnim(state);
            float flap=(float)Math.sin(state.ageInTicks*0.35f)*0.06f;
            leftWing.zRot=0.16f+flap;rightWing.zRot=-0.16f-flap;
        }
    }
    private DragonWolfArmorMesh() {}
}
