package com.ericlam.mc.visiblepathfinder;

import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import com.ericlam.mc.visiblepathfinder.api.GraphSearchAlgorithm;
import com.ericlam.mc.visiblepathfinder.api.PathSearcher;
import com.ericlam.mc.visiblepathfinder.api.PathSearcherService;
import com.ericlam.mc.visiblepathfinder.config.VPFConfig;
import com.google.common.graph.Graph;
import com.google.inject.Injector;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

public class PathSearcherManager implements PathSearcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathSearcherService.class);
    ;

    @Inject
    private ScheduleService scheduleService;
    @Inject
    private VisiblePathFinder plugin;
    @Inject
    private SearchRecordManager searchRecordManager;
    @Inject
    private FakeParticleManager particleManager;
    @Inject
    private FakeBlockManager blockManager;
    @Inject
    private Injector injector;

    private final String defaultSearcher;
    private final int defaultRange;
    private final DistanceScorer defaultScorer;
    private final Map<String, DistanceScorer> distanceScorerMap;
    private final Map<String, Class<? extends GraphSearchAlgorithm>> algorithmMap;

    @Inject
    public PathSearcherManager(VPFConfig config, Map<String, DistanceScorer> distanceScorerMap, VisiblePathFinder.SearchWayInstallationImpl searchWays) {
        this.algorithmMap = searchWays.getAlgorithmMap();
        this.distanceScorerMap = distanceScorerMap;
        this.defaultSearcher = config.default_settings.algorithm;
        this.defaultScorer = distanceScorerMap.get(config.default_settings.distance_calculator);
        this.defaultRange = config.default_settings.weight;
        if (this.defaultScorer == null) {
            throw new IllegalArgumentException("Unknown distance calculator: " + config.default_settings.distance_calculator);
        }
    }


    @Override
    public boolean hasPathSearcher(String searcher) {
        return algorithmMap.containsKey(searcher);
    }

    @Override
    public boolean hasDistanceAlgorithm(String distance) {
        return distanceScorerMap.containsKey(distance);
    }

    @Override
    public PathSearcher getPathSearcher(String searcher) {
        if (!hasPathSearcher(searcher)) {
            LOGGER.warn("無效的路徑演算法名稱: {}, 採用回默認路徑演算法: {}", searcher, defaultSearcher);
            searcher = defaultSearcher;
        }
        final var searcherCls = algorithmMap.get(searcher);
        return new PathFinder(
                searchRecordManager,
                scheduleService,
                plugin,
                () -> injector.getInstance(searcherCls),
                defaultRange,
                defaultScorer
        );
    }

    @Override
    public PathSearcherBuilder buildPathSearcher(String searcher) {
        return new PathFinderBuilder(searcher);
    }

    @Override
    public boolean terminateSearch(Player player) {
        var re = searchRecordManager.removeLastSearch(player) || searchRecordManager.removeLastRoute(player);
        if (re){
            particleManager.clearParticle(player);
            blockManager.clearAllFakeBlock(player);
        }
        return re;
    }


    public class PathFinderBuilder implements PathSearcherBuilder {

        private String searcher;
        private String distance = "";
        private int weight = defaultRange;

        public PathFinderBuilder(String searcher) {
            this.searcher = searcher;
        }

        @Override
        public PathSearcherBuilder setSearcherAlgorithm(String searcher) {
            this.searcher = searcher;
            return this;
        }

        @Override
        public PathSearcherBuilder setDistanceAlgorithm(String distanceAlgorithm) {
            this.distance = distanceAlgorithm;
            return this;
        }

        @Override
        public PathSearcherBuilder setWeight(int weight) {
            this.weight = weight;
            return this;
        }

        @Override
        public PathSearcher build() {
            if (!algorithmMap.containsKey(searcher)) {
                LOGGER.warn("Unknown Searcher: {}, use back default searcher", searcher);
                searcher = defaultSearcher;
            }
            final var searcherType = algorithmMap.get(searcher);
            var scorer = distanceScorerMap.get(distance);
            if (scorer == null) {
                LOGGER.warn("Unknown DistanceScorer: {}, use back default distanceScorer", distance);
                scorer = defaultScorer;
            }
            return new PathFinder(
                    searchRecordManager,
                    scheduleService,
                    plugin,
                    () -> injector.getInstance(searcherType),
                    weight,
                    scorer
            );
        }
    }
}
