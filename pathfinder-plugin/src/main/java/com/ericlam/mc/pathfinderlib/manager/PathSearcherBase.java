package com.ericlam.mc.pathfinderlib.manager;

import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.pathfinderlib.PathFinderLib;
import com.ericlam.mc.pathfinderlib.api.DistanceScorer;
import com.ericlam.mc.pathfinderlib.api.PathSearchingAPI;
import com.ericlam.mc.pathfinderlib.config.PathLibConfig;
import com.google.inject.Injector;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

public abstract class PathSearcherBase<T, B extends PathSearchingAPI.SearcherBuilder<T>> implements PathSearchingAPI<T, B> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PathSearchingAPI.class);
    protected final String defaultSearcher;
    protected final int defaultWeight;
    protected final DistanceScorer defaultScorer;
    protected final Map<String, DistanceScorer> distanceScorerMap;
    @Inject
    protected ScheduleService scheduleService;
    @Inject
    protected PathFinderLib plugin;
    @Inject
    protected SearchRecordManager searchRecordManager;
    @Inject
    protected FakeParticleManager particleManager;
    @Inject
    protected FakeBlockManager blockManager;
    @Inject
    protected Injector injector;

    @Inject
    public PathSearcherBase(PathLibConfig config,
                            Map<String, DistanceScorer> distanceScorerMap,
                            PathFinderLib.SearchWayInstallationImpl searchWays
    ) {
        this.distanceScorerMap = distanceScorerMap;
        this.defaultSearcher = config.default_settings.algorithm;
        this.defaultScorer = distanceScorerMap.get(config.default_settings.distance_calculator);
        this.defaultWeight = config.default_settings.weight;
        if (this.defaultScorer == null) {
            throw new IllegalArgumentException("Unknown distance calculator: " + config.default_settings.distance_calculator);
        }
    }

    @Override
    public boolean hasDistanceAlgorithm(String distance) {
        return distanceScorerMap.containsKey(distance);
    }

    @Override
    public boolean terminateSearch(Player player) {
        var re = searchRecordManager.removeLastSearch(player) || searchRecordManager.removeLastRoute(player);
        if (re) {
            particleManager.clearParticle(player);
            blockManager.clearAllFakeBlock(player);
        }
        return re;
    }


}
