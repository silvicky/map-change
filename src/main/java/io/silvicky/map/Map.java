package io.silvicky.map;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Map implements ModInitializer {
    /**
     * Runs the mod initializer.
     */
    public static final String MOD_ID = "MapChange";
    public static final Logger LOGGER = LoggerFactory.getLogger("map-change");
    @Override
    public void onInitialize() {
        LOGGER.info("Loading MapChange...");
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> MapChange.register(dispatcher));

    }
}
