package dev.pvpoverhaul;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PvpOverhaul implements ModInitializer {
    public static final String ID="pvpoverhaul";
    public static final Logger LOGGER=LoggerFactory.getLogger(ID);
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(ID,path); }
    @Override public void onInitialize() {
        ModItems.initialize();VanillaComponents.initialize();Skills.initialize();Interactions.initialize();
        LOGGER.info("PvP Overhaul loaded for Minecraft 26.3");
    }
}
