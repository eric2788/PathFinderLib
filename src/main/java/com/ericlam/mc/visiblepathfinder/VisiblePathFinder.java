package com.ericlam.mc.visiblepathfinder;

import com.ericlam.mc.eld.ELDBukkitPlugin;
import com.ericlam.mc.eld.ManagerProvider;
import com.ericlam.mc.eld.ServiceCollection;
import com.ericlam.mc.eld.annotations.ELDPlugin;
import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import com.ericlam.mc.visiblepathfinder.api.PathFinderService;
import com.ericlam.mc.visiblepathfinder.api.PathSearcher;
import com.ericlam.mc.visiblepathfinder.config.VPFConfig;
import com.ericlam.mc.visiblepathfinder.distances.BukkitVectorDistance;
import com.ericlam.mc.visiblepathfinder.distances.EuclideanDistance;
import com.ericlam.mc.visiblepathfinder.distances.ManhattanDistance;
import com.ericlam.mc.visiblepathfinder.searcher.AStarSearcher;

import java.util.Map;

@ELDPlugin(
        registry = VisiblePathFinderRegistry.class,
        lifeCycle = VisiblePathFinderLifeCycle.class
)
public final class VisiblePathFinder extends ELDBukkitPlugin {

    @Override
    protected void manageProvider(ManagerProvider provider) {
    }

    @Override
    protected void bindServices(ServiceCollection collection) {
        collection.addServices(PathSearcher.class, Map.of(
                "astar", AStarSearcher.class
        ));

        collection.addServices(DistanceScorer.class, Map.of(
                "bukkit", BukkitVectorDistance.class,
                "euclidean", EuclideanDistance.class,
                "manhattan", ManhattanDistance.class
        ));

        collection.addConfiguration(VPFConfig.class);

        collection.bindService(PathFinderService.class, PathFinderManager.class);
        collection.addSingleton(GeoLocatorService.class);
        collection.addSingleton(MCMechanism.class);
        collection.addSingleton(Debugger.class);
        collection.addSingleton(FakeParticleManager.class);

    }
}
