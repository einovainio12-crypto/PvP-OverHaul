package dev.pvpplusdiscs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
public final class DiscUpdate implements ModInitializer {
 public void onInitialize() {
  for(String song:new String[]{"they_run","final_stand","trident_ascension","deadlock","playful_massacre","chaos_construct","battle_under_a_broken_sky","playing_with_fire","decisive_battle","rival","manepears_flute_song"}) {
   var sound=Identifier.parse("pvpplusdiscs:music_disc."+song);
   Registry.register(BuiltInRegistries.SOUND_EVENT,sound,SoundEvent.createVariableRangeEvent(sound));
   var key=ResourceKey.create(Registries.ITEM,Identifier.parse("pvpplusdiscs:music_disc_"+song));
   var item=Registry.register(BuiltInRegistries.ITEM,key,new Item(new Item.Properties().setId(key).stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG,Identifier.parse("pvpplusdiscs:"+song)))));
   CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,Identifier.parse("minecraft:tools_and_utilities"))).register(output->output.accept(item));
  }
 }
}
