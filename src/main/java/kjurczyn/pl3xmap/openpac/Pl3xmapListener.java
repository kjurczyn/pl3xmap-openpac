package kjurczyn.pl3xmap.openpac;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Pair;
import net.minecraft.util.math.ChunkPos;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.ServerLoadedEvent;
import net.pl3x.map.core.markers.layer.CustomLayer;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.markers.marker.Polygon;
import net.pl3x.map.core.markers.marker.Polyline;
import net.pl3x.map.core.markers.marker.Rectangle;
import net.pl3x.map.core.world.World;
import net.pl3x.map.core.markers.Point;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;

import java.util.*;

public class Pl3xmapListener implements EventListener{

    public static final Logger LOGGER = LoggerFactory.getLogger("Pl3xmap-openpac");

    public static final String KEY = "openpac";
    private final MinecraftServer currentServer;
    public Pl3xmapListener(@NotNull MinecraftServer myServer)
    {
        currentServer = myServer;
    }

    private boolean isTouching(ChunkPos ch1, ChunkPos ch2)
    {
        return (ch1.getStartX() == ch2.getStartX() && ch1.getStartZ() == ch2.getStartZ() - 1) || // touches north
                (ch1.getStartX() == ch2.getStartX() && ch1.getStartZ() == ch2.getStartZ() + 1) || // touches south
                (ch1.getStartX() == ch2.getStartX() - 1 && ch1.getStartZ() == ch2.getStartZ()) || // touches west
                (ch1.getStartX() == ch2.getStartX() + 1 && ch1.getStartZ() == ch2.getStartZ());// touches east
    }

    private boolean isTouching(ChunkPos ch, Polyline pl)
    {

    }

    private Polyline mergeGroup(List<ChunkPos> chunkGroup, String polylineName)
    {
        ArrayList<ChunkPos> added = new ArrayList<>();
        ArrayList<Pair<Point, Point>> lines = new ArrayList<>();

        Polyline groupPolyline = new Polyline(polylineName);
        groupPolyline.addPoint(Point.of(chunkGroup.get(0).getStartX(), chunkGroup.get(0).getStartZ()));
        groupPolyline.addPoint(Point.of(chunkGroup.get(0).getStartX(), chunkGroup.get(0).getEndZ()));
        groupPolyline.addPoint(Point.of(chunkGroup.get(0).getEndX(), chunkGroup.get(0).getStartZ()));
        groupPolyline.addPoint(Point.of(chunkGroup.get(0).getEndX(), chunkGroup.get(0).getEndZ()));

        added.add(chunkGroup.remove(0));
        // TODO Polygon class with edges

        while (!chunkGroup.isEmpty()) {
            List<Point> points = groupPolyline.getPoints();
            ChunkPos curChunk = chunkGroup.get(0);
            List<ChunkPos> touchingList = new ArrayList<>();
            for (ChunkPos addedChunk : added) {
                if (isTouching(addedChunk, curChunk)) {
                    touchingList.add(addedChunk);
                }
            }

            if (touchingList.isEmpty()) {
                Collections.rotate(chunkGroup, -1);
            } else {
                for (ChunkPos touchingChunk : touchingList) {

                }
            }


        }

        return groupPolyline;
    }

//    private boolean isInside(Polyline polyline, Point point)
//    {
//        polyline.getPoints(
//        // Ray casting
//        polyline.getPoints().forEach(polylinePoint -> {
//
//        });
//    }
    private void drawChunk(CustomLayer worldLayer, ChunkPos chunkPos, String chunkLabel, UUID playerID)
    {
        final String key = String.format("%s_%s_%d_%d_%d_%d", worldLayer.getWorld().getName(), playerID.toString(),
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
                final UUID id = playerClaimInfo.getPlayerId();
                playerClaimInfo.getStream().forEach(entry -> {
                    World claimWorld = Pl3xMap.api().getWorldRegistry()
                            .get(entry.getKey().toString());
                    if (claimWorld == null)
                        return;
                    final CustomLayer claimLayer = (CustomLayer) claimWorld.getLayerRegistry().get(KEY);
                    assert claimLayer != null;
                    List<List<ChunkPos>> chunkPosGroupList = new ArrayList<>();
                    entry.getValue().getStream().forEach(chunkPosList -> {
                        chunkPosList.getStream().forEach(chunk -> {
                            ArrayList<List<ChunkPos>> touchingList = new ArrayList<>();
                            chunkPosGroupList.forEach(group -> {
                                for (ChunkPos addedChunk : group) {
                                    if (isTouching(addedChunk, chunk)) {
                                        touchingList.add(group);
                                    }
                                    break;
                                }

                                if (touchingList.isEmpty()) {
                                    // chunk is not touching any group, create a new one
                                  chunkPosGroupList.add(new ArrayList<>());
                                  chunkPosGroupList.get(chunkPosGroupList.size()-1).add(chunk);
                                } else if (touchingList.size() == 1) {
                                    // Touching one group, add chunk to group
                                   touchingList.get(0).add(chunk);
                                } else {
                                    // touching 2 or more chunk groups, merge other groups into the
                                    // first one on the list then remove them from the list of groups.
                                    // Add the chunk at the end.
                                    for (int i = 1; i < touchingList.size(); i++) {
                                        touchingList.get(0).addAll(touchingList.get(i));
                                        chunkPosGroupList.remove(touchingList.get(i));
                                    }
                                    touchingList.get(0).add(chunk);
                                }
                            });
//                            drawChunk(claimLayer, chunk, name, id);
                        });
                    });
                    for (List<ChunkPos> chunkPosGroup : chunkPosGroupList) {
                        // merge the groups into polylines

                    }
                });


            });
        });
    }

}
