package com.ericlam.mc.visiblepathfinder.manager;

import com.ericlam.mc.visiblepathfinder.pathfinder.DynamicPathFinder;
import com.ericlam.mc.visiblepathfinder.VisiblePathFinder;
import com.ericlam.mc.visiblepathfinder.api.*;
import com.ericlam.mc.visiblepathfinder.config.VPFConfig;

import javax.inject.Inject;
import java.util.Map;

public final class DynamicPathSearcherManager extends PathSearcherBase<DynamicPathSearcher, DynamicPathSearcherService.DynamicPathSearcherBuilder> implements DynamicPathSearcherService {

    private final Map<String, Class<? extends DynamicGraphSearchAlgorithm>> algorithmMap;
    private final VPFConfig.DynamicSettings dynamicSettings;

    @Inject
    public DynamicPathSearcherManager(VPFConfig config,
                                      Map<String, DistanceScorer> distanceScorerMap,
                                      VisiblePathFinder.SearchWayInstallationImpl searchWays
    ) {
        super(config, distanceScorerMap, searchWays);
        this.algorithmMap = searchWays.getDynamicAlgorithmMap();
        this.dynamicSettings = config.dynamic_settings;
    }


    @Override
    public boolean hasPathSearcher(String searcher) {
        return algorithmMap.containsKey(searcher);
    }

    @Override
    public DynamicPathSearcher getPathSearcher(String searcher) {
        if (!hasPathSearcher(searcher)) {
            LOGGER.warn("無效的動態路徑演算法名稱: {}, 採用回默認動態路徑演算法: {}", searcher, dynamicSettings.algorithm);
            searcher = dynamicSettings.algorithm;
        }
        var searcherType = algorithmMap.get(searcher);
        return new DynamicPathFinder(
                searchRecordManager,
                scheduleService,
                plugin,
                () -> injector.getInstance(searcherType),
                defaultWeight,
                defaultScorer,
                dynamicSettings.max_accepted_radius,
                dynamicSettings.ticks_per_check
        );
    }

    @Override
    public DynamicPathSearcherBuilder buildPathSearcher(String searcher) {
        return new DynamicPathFinderBuilder(searcher);
    }


    public class DynamicPathFinderBuilder implements DynamicPathSearcherBuilder {

        private String searcher;
        private String distance = "";
        private int weight = defaultWeight;
        private int checkingRadius = dynamicSettings.max_accepted_radius;
        private long intervalCheck = dynamicSettings.ticks_per_check;

        public DynamicPathFinderBuilder(String searcher) {
            this.searcher = searcher;
        }

        @Override
        public DynamicPathSearcherBuilder setSearcherAlgorithm(String searcher) {
            this.searcher = searcher;
            return this;
        }

        @Override
        public DynamicPathSearcherBuilder setDistanceAlgorithm(String distanceAlgorithm) {
            this.distance = distanceAlgorithm;
            return this;
        }

        @Override
        public DynamicPathSearcherBuilder setWeight(int weight) {
            this.weight = weight;
            return this;
        }

        @Override
        public DynamicPathSearcherBuilder checkingRadius(int radius) {
            this.checkingRadius = radius;
            return this;
        }

        @Override
        public DynamicPathSearcherBuilder intervalPerCheck(long ticks) {
            this.intervalCheck = ticks;
            return this;
        }

        @Override
        public DynamicPathSearcher build() {
            if (!algorithmMap.containsKey(searcher)) {
                LOGGER.warn("Unknown Searcher: {}, use back default searcher", searcher);
                searcher = dynamicSettings.algorithm;
            }
            final var searcherType = algorithmMap.get(searcher);
            var scorer = distanceScorerMap.get(distance);
            if (scorer == null) {
                LOGGER.warn("Unknown DistanceScorer: {}, use back default distanceScorer", distance);
                scorer = defaultScorer;
            }
            return new DynamicPathFinder(
                    searchRecordManager,
                    scheduleService,
                    plugin,
                    () -> injector.getInstance(searcherType),
                    weight,
                    scorer,
                    checkingRadius,
                    intervalCheck
            );
        }
    }
}
