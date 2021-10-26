package com.ericlam.mc.visiblepathfinder.pathfinder;

import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.visiblepathfinder.VisiblePathFinder;
import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import com.ericlam.mc.visiblepathfinder.api.SearchAlgorithm;
import com.ericlam.mc.visiblepathfinder.manager.SearchRecordManager;

import javax.inject.Provider;

public abstract class PathFinderBase<T extends SearchAlgorithm> {

    protected final SearchRecordManager recordManager;
    protected final ScheduleService scheduleService;
    protected final VisiblePathFinder plugin;
    protected final Provider<T> searchAlgorithmProvider;
    protected final DistanceScorer scorer;
    protected final int weight;

    public PathFinderBase(SearchRecordManager recordManager,
                          ScheduleService scheduleService,
                          VisiblePathFinder plugin,
                          Provider<T> searchAlgorithmProvider,
                          int weight,
                          DistanceScorer scorer
    ) {
        this.recordManager = recordManager;
        this.scheduleService = scheduleService;
        this.plugin = plugin;
        this.searchAlgorithmProvider = searchAlgorithmProvider;
        this.weight = weight;
        this.scorer = scorer;
    }

}
