package kjurczyn.pl3xmap.openpac;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.ServerLoadedEvent;
import net.pl3x.map.core.markers.layer.CustomLayer;
import org.jetbrains.annotations.NotNull;


public class Pl3xmapListener implements EventListener{
    public static final String KEY = "openpac";

    @EventHandler
    public void onServerLoaded(@NotNull ServerLoadedEvent event)
    {
        Pl3xMap.api().getWorldRegistry().forEach(world-> {
            world.getLayerRegistry().register(new CustomLayer(KEY, world, () -> "Claims"));
        });
    }

//    @EventHandler
//    public void onWorldLoaded(@NotNull WorldLoadedEvent event)
//    {
//
//    }

}
