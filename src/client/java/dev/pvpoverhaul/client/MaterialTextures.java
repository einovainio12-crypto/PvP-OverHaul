package dev.pvpoverhaul.client;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import dev.pvpoverhaul.PvpOverhaul;

/** Palette equipment textures reload with resource packs; original source pixels remain untouched. */
public final class MaterialTextures {
 private static final Set<Identifier> registered=new HashSet<>();
 public static void ensure(Identifier id){
  if(!id.getNamespace().equals("pvpoverhaul") || !id.getPath().contains("/compat_"))return;
  if(registered.add(id))Minecraft.getInstance().getTextureManager().registerAndLoad(id,new SimpleTexture(id){
   @Override public TextureContents loadContents(ResourceManager manager)throws IOException{
    JsonObject mapping;
    try(var reader=manager.getResourceOrThrow(PvpOverhaul.id("compat_equipment.json")).openAsReader()){mapping=JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject(id.toString());}
    if(mapping==null)throw new IOException("Missing palette definition "+id);
    var source=TextureContents.load(manager,Identifier.parse(mapping.get("source").getAsString()));
    var pixels=source.image();var palette=mapping.getAsJsonArray("palette");
    TreeSet<Integer> colors=new TreeSet<>(Comparator.comparingDouble(MaterialTextures::luminance).thenComparingInt(Integer::intValue));
    for(int c:pixels.getPixels())if((c>>>24)!=0)colors.add(c&0xffffff);
    Map<Integer,Integer> replacements=new HashMap<>();int i=0;
    for(int color:colors){int index=Math.round((float)i++*(palette.size()-1)/Math.max(1,colors.size()-1));replacements.put(color,Integer.parseInt(palette.get(index).getAsString(),16));}
    for(int y=0;y<pixels.getHeight();y++)for(int x=0;x<pixels.getWidth();x++){int c=pixels.getPixel(x,y);if((c>>>24)!=0)pixels.setPixel(x,y,(c&0xff000000)|replacements.get(c&0xffffff));}
    return source;
   }
  });
 }
 private static double luminance(int c){return .2126*((c>>16)&255)+.7152*((c>>8)&255)+.0722*(c&255);}
 private MaterialTextures(){}
}
