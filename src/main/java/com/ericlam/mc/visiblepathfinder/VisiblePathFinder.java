package com.ericlam.mc.visiblepathfinder;

import com.ericlam.mc.eld.AddonManager;
import com.ericlam.mc.eld.ELDBukkitAddon;
import com.ericlam.mc.eld.ManagerProvider;
import com.ericlam.mc.eld.ServiceCollection;
import com.ericlam.mc.eld.annotations.ELDPlugin;
import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import com.ericlam.mc.visiblepathfinder.api.GraphSearchAlgorithm;
import com.ericlam.mc.visiblepathfinder.api.PathSearcherService;
import com.ericlam.mc.visiblepathfinder.api.SearchWayInstallation;
import com.ericlam.mc.visiblepathfinder.config.VPFConfig;
import com.ericlam.mc.visiblepathfinder.distances.BukkitVectorDistance;
import com.ericlam.mc.visiblepathfinder.distances.ChebyshevDistance;
import com.ericlam.mc.visiblepathfinder.distances.EuclideanDistance;
import com.ericlam.mc.visiblepathfinder.distances.ManhattanDistance;
import com.ericlam.mc.visiblepathfinder.searcher.AStarSearcher;
import com.ericlam.mc.visiblepathfinder.searcher.TraceSearcher;
import com.google.inject.AbstractModule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ELDPlugin(
        registry = VisiblePathFinderRegistry.class,
        lifeCycle = VisiblePathFinderLifeCycle.class
)
public final class VisiblePathFinder extends ELDBukkitAddon {

    @Override
    protected void bindServices(ServiceCollection collection) {

        collection.addServices(DistanceScorer.class, Map.of(
                "bukkit", BukkitVectorDistance.class,
                "euclidean", EuclideanDistance.class,
                "manhattan", ManhattanDistance.class,
                "chebyshev", ChebyshevDistance.class
        ));

        collection.addConfiguration(VPFConfig.class);

        collection.bindService(PathSearcherService.class, PathSearcherManager.class);
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

        installer.installModule(new SearchModule(install));
    }

    public static final class SearchWayInstallationImpl implements SearchWayInstallation {

        private final Map<String, Class<? extends GraphSearchAlgorithm>> algorithmMap = new ConcurrentHashMap<>();

        @Override
        public void installSearch(String searcher, Class<? extends GraphSearchAlgorithm> algorithmCls) {
            this.algorithmMap.put(searcher, algorithmCls);
        }

        public Map<String, Class<? extends GraphSearchAlgorithm>> getAlgorithmMap() {
            return algorithmMap;
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
