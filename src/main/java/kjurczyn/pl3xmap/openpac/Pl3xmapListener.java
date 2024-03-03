package kjurczyn.pl3xmap.openpac;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.ServerLoadedEvent;
import net.pl3x.map.core.markers.layer.CustomLayer;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.markers.marker.Rectangle;
import net.pl3x.map.core.world.World;
import net.pl3x.map.core.markers.Point;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;


public class Pl3xmapListener implements EventListener{

    public static final Logger LOGGER = LoggerFactory.getLogger("Pl3xmap-openpac");

    public static final String KEY = "openpac";
    private final MinecraftServer currentServer;
    public Pl3xmapListener(@NotNull MinecraftServer myServer)
    {
        currentServer = myServer;
    }

    private void drawChunk(CustomLayer worldLayer, ChunkPos chunkPos, String chunkLabel, String playerName)
    {
        final String key = String.format("%s_%s_%d_%d_%d_%d", worldLayer.getWorld().getName(), playerName,
                chunkPos.getStartX(), chunkPos.getStartZ(), chunkPos.getEndX(), chunkPos.getEndZ() );
        worldLayer.addMarker(new Rectangle(key, Point.of(chunkPos.getStartX(), chunkPos.getStartZ()),
                Point.of(chunkPos.getEndX(), chunkPos.getEndZ())));
    }

    @EventHandler
    public void onServerLoaded(@NotNull ServerLoadedEvent event)
    {
        Pl3xMap.api().getWorldRegistry().forEach(world-> {
            world.getLayerRegistry().register(new CustomLayer(KEY, world, () -> "Claims"));
            OpenPACServerAPI.get(currentServer).getServerClaimsManager().getPlayerInfoStream().forEach(playerClaimInfo -> {
                final String name = playerClaimInfo.getPlayerUsername();
                playerClaimInfo.getStream().forEach(entry -> {
                    World claimWorld = Pl3xMap.api().getWorldRegistry()
                            .get(entry.getKey().toString());
                    if (claimWorld == null)
                        return;
                    final CustomLayer claimLayer = (CustomLayer) claimWorld.getLayerRegistry().get(KEY);
                    entry.getValue().getStream().forEach(chunkPosList -> {
                        chunkPosList.getStream().forEach(chunk -> drawChunk(claimLayer, chunk, name, name));
                    });
                });

            });
        });
    }

//    @EventHandler
//    public void onWorldLoaded(@NotNull WorldLoadedEvent event)
//    {
//
//    }

}
