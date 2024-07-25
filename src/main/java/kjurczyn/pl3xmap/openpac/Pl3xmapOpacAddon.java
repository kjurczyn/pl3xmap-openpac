package kjurczyn.pl3xmap.openpac;

import net.fabricmc.api.DedicatedServerModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.pl3x.map.core.Pl3xMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.server.MinecraftServer;

public class Pl3xmapOpacAddon implements DedicatedServerModInitializer {


    public static final Logger LOGGER = LoggerFactory.getLogger("Pl3xmap-openpac");
    private MinecraftServer minecraftServer;
    @Override
    public void onInitializeServer() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            minecraftServer = server;
            Pl3xMap.api().getEventRegistry().register(new Pl3xmapListener(minecraftServer));
        });


        LOGGER.info("Pl3xmap-openpac initialized!");
    }
}
