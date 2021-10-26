package com.ericlam.mc.visiblepathfinder.manager;

import com.ericlam.mc.visiblepathfinder.pathfinder.PathFinder;
import com.ericlam.mc.visiblepathfinder.VisiblePathFinder;
import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import com.ericlam.mc.visiblepathfinder.api.GraphSearchAlgorithm;
import com.ericlam.mc.visiblepathfinder.api.PathSearcher;
import com.ericlam.mc.visiblepathfinder.api.PathSearcherService;
import com.ericlam.mc.visiblepathfinder.config.VPFConfig;

import javax.inject.Inject;
import java.util.Map;

public final class PathSearcherManager extends PathSearcherBase<PathSearcher, PathSearcherService.PathSearcherBuilder> implements PathSearcherService {

    private final Map<String, Class<? extends GraphSearchAlgorithm>> algorithmMap;

    @Inject
    public PathSearcherManager(VPFConfig config,
                               Map<String, DistanceScorer> distanceScorerMap,
                               VisiblePathFinder.SearchWayInstallationImpl searchWays
    ) {
        super(config, distanceScorerMap, searchWays);
        this.algorithmMap = searchWays.getAlgorithmMap();
    }

    @Override
    public boolean hasPathSearcher(String searcher) {
        return algorithmMap.containsKey(searcher);
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
                defaultWeight,
                defaultScorer
        );
    }

    @Override
    public PathSearcherBuilder buildPathSearcher(String searcher) {
        return new PathFinderBuilder(searcher);
    }


    public class PathFinderBuilder implements PathSearcherBuilder {

        private String searcher;
        private String distance = "";
        private int weight = defaultWeight;

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
