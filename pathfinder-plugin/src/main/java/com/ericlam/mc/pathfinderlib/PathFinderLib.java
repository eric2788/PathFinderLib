package com.ericlam.mc.pathfinderlib;

import com.ericlam.mc.eld.AddonManager;
import com.ericlam.mc.eld.ELDBukkitAddon;
import com.ericlam.mc.eld.ManagerProvider;
import com.ericlam.mc.eld.ServiceCollection;
import com.ericlam.mc.eld.annotations.ELDPlugin;
import com.ericlam.mc.pathfinderlib.api.DistanceScorer;
import com.ericlam.mc.pathfinderlib.api.DynamicPathSearcherService;
import com.ericlam.mc.pathfinderlib.api.PathSearcherService;
import com.ericlam.mc.pathfinderlib.api.SearchWayInstallation;
import com.ericlam.mc.pathfinderlib.api.algorithm.DynamicGraphSearchAlgorithm;
import com.ericlam.mc.pathfinderlib.api.algorithm.GraphSearchAlgorithm;
import com.ericlam.mc.pathfinderlib.config.PathLibConfig;
import com.ericlam.mc.pathfinderlib.distances.BukkitVectorDistance;
import com.ericlam.mc.pathfinderlib.distances.ChebyshevDistance;
import com.ericlam.mc.pathfinderlib.distances.EuclideanDistance;
import com.ericlam.mc.pathfinderlib.distances.ManhattanDistance;
import com.ericlam.mc.pathfinderlib.manager.DynamicPathSearcherManager;
import com.ericlam.mc.pathfinderlib.manager.FakeParticleManager;
import com.ericlam.mc.pathfinderlib.manager.PathSearcherManager;
import com.ericlam.mc.pathfinderlib.searcher.*;
import com.google.inject.AbstractModule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ELDPlugin(
        registry = PathFinderLibRegistry.class,
        lifeCycle = PathFinderLibLifeCycle.class
)
public final class PathFinderLib extends ELDBukkitAddon {

    @Override
    protected void bindServices(ServiceCollection collection) {

        collection.addServices(DistanceScorer.class, Map.of(
                "bukkit", BukkitVectorDistance.class,
                "euclidean", EuclideanDistance.class,
                "manhattan", ManhattanDistance.class,
                "chebyshev", ChebyshevDistance.class
        ));

        collection.addConfiguration(PathLibConfig.class);

        collection.bindService(PathSearcherService.class, PathSearcherManager.class);
        collection.bindService(DynamicPathSearcherService.class, DynamicPathSearcherManager.class);
        collection.addSingleton(GeoLocatorService.class);
        collection.addSingleton(MCMechanism.class);
        collection.addSingleton(Debugger.class);
        collection.addSingleton(FakeParticleManager.class);
    }

    @Override
    protected void preAddonInstall(ManagerProvider provider, AddonManager installer) {
        var install = new SearchWayInstallationImpl();
        installer.customInstallation(SearchWayInstallation.class, install);

        install.installSearch("astar", AStarSearcher.class);
        install.installSearch("trace", TraceSearcher.class);
        install.installSearch("biastar", BiAStarSearcher.class);

        install.installDynamicSearch("dstar", DStarSearcher.class);
        install.installDynamicSearch("bidstar", BiDStarSearcher.class);

        installer.installModule(new SearchModule(install));
    }

    public static final class SearchWayInstallationImpl implements SearchWayInstallation {

        private final Map<String, Class<? extends GraphSearchAlgorithm>> algorithmMap = new ConcurrentHashMap<>();
        private final Map<String, Class<? extends DynamicGraphSearchAlgorithm>> dynamicAlgorithmMap = new ConcurrentHashMap<>();

        @Override
        public void installSearch(String searcher, Class<? extends GraphSearchAlgorithm> algorithm) {
            this.algorithmMap.put(searcher, algorithm);
        }

        @Override
        public void installDynamicSearch(String searcher, Class<? extends DynamicGraphSearchAlgorithm> algorithm) {
            this.dynamicAlgorithmMap.put(searcher, algorithm);
        }

        public Map<String, Class<? extends GraphSearchAlgorithm>> getAlgorithmMap() {
            return algorithmMap;
        }

        public Map<String, Class<? extends DynamicGraphSearchAlgorithm>> getDynamicAlgorithmMap() {
            return dynamicAlgorithmMap;
        }
    }

    public static class SearchModule extends AbstractModule {

        private final SearchWayInstallationImpl searchWayInstallation;

        private SearchModule(SearchWayInstallationImpl searchWayInstallation) {
            this.searchWayInstallation = searchWayInstallation;
        }

        @Override
        protected void configure() {
            bind(SearchWayInstallationImpl.class).toInstance(searchWayInstallation);
        }
    }
}
