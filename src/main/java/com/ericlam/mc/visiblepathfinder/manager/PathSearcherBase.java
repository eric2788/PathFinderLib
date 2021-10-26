package com.ericlam.mc.visiblepathfinder.manager;

import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.visiblepathfinder.VisiblePathFinder;
import com.ericlam.mc.visiblepathfinder.api.*;
import com.ericlam.mc.visiblepathfinder.config.VPFConfig;
import com.google.inject.Injector;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

public abstract class PathSearcherBase<T, B extends PathSearchingAPI.SearcherBuilder<T>> implements PathSearchingAPI<T, B>  {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PathSearchingAPI.class);

    @Inject
    protected ScheduleService scheduleService;
    @Inject
    protected VisiblePathFinder plugin;
    @Inject
    protected SearchRecordManager searchRecordManager;
    @Inject
    protected FakeParticleManager particleManager;
    @Inject
    protected FakeBlockManager blockManager;
    @Inject
    protected Injector injector;

    protected final String defaultSearcher;
    protected final int defaultWeight;
    protected final DistanceScorer defaultScorer;
    protected final Map<String, DistanceScorer> distanceScorerMap;

    @Inject
    public PathSearcherBase(VPFConfig config,
                               Map<String, DistanceScorer> distanceScorerMap,
                               VisiblePathFinder.SearchWayInstallationImpl searchWays
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
