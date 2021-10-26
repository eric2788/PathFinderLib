package com.ericlam.mc.visiblepathfinder.pathfinder;

import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.visiblepathfinder.VisiblePathFinder;
import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import com.ericlam.mc.visiblepathfinder.api.DynamicGraphSearchAlgorithm;
import com.ericlam.mc.visiblepathfinder.api.DynamicPathSearcher;
import com.ericlam.mc.visiblepathfinder.manager.SearchRecordManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import javax.inject.Provider;
import java.util.List;

public final class DynamicPathFinder extends PathFinderBase<DynamicGraphSearchAlgorithm> implements DynamicPathSearcher {

    private Consumer<List<Vector>> onUpdated = o -> {
    };
    private Consumer<Entity> onReachedTarget = o -> {
    };

    private final int checkingRadius;
    private final long intervalCheck;

    public DynamicPathFinder(SearchRecordManager recordManager,
                             ScheduleService scheduleService,
                             VisiblePathFinder plugin,
                             Provider<DynamicGraphSearchAlgorithm> searchAlgorithmProvider,
                             int weight,
                             DistanceScorer scorer,
                             int checkingRadius,
                             long intervalCheck
    ) {
        super(recordManager, scheduleService, plugin, searchAlgorithmProvider, weight, scorer);
        this.checkingRadius = checkingRadius;
        this.intervalCheck = intervalCheck;
    }

    @Override
    public ScheduleService.BukkitPromise<Void> findPathAsync(Entity from, Entity to, Player player) {
        return scheduleService.runAsync(plugin, () -> this.findPath(from, to, player));
    }

    @Override
    public ScheduleService.BukkitPromise<Void> findPathAsync(Entity from, Location to, Player player) {
        return scheduleService.runAsync(plugin, () -> this.findPath(from, to, player));
    }

    @Override
    public ScheduleService.BukkitPromise<Void> findPathAsync(Entity from, Entity to, World world) {
        return scheduleService.runAsync(plugin, () -> this.findPath(from, to, world));
    }

    @Override
    public ScheduleService.BukkitPromise<Void> findPathAsync(Entity from, Location to, World world) {
        return scheduleService.runAsync(plugin, () -> this.findPath(from, to, world));
    }

    @Override
    public void findPath(Entity from, Entity to, Player player) {
        recordManager.removeLastRoute(player);
        recordManager.removeLastSearch(player);
        var searcher = searchAlgorithmProvider.get();
        searcher.setOnUpdatedPath(onUpdated);
        searcher.setOnReachedTarget(onReachedTarget);
        recordManager.setLastSearch(player, searcher);
        // no need set route because it is dynamic
        searcher.startRouting(from, to, player.getWorld(), player, scorer, weight, checkingRadius, intervalCheck);
    }

    @Override
    public void findPath(Entity from, Location to, World world) {
        var toVector = to.toBlockLocation().toVector();
        var searcher = searchAlgorithmProvider.get();
        searcher.setOnUpdatedPath(onUpdated);
        searcher.setOnReachedTarget(onReachedTarget);
        searcher.startRouting(from, toVector, world, null, scorer, weight, checkingRadius, intervalCheck);
    }

    @Override
    public void findPath(Entity from, Location to, Player player) {
        var toVector = to.toBlockLocation().toVector();
        recordManager.removeLastRoute(player);
        recordManager.removeLastSearch(player);
        var searcher = searchAlgorithmProvider.get();
        searcher.setOnUpdatedPath(onUpdated);
        searcher.setOnReachedTarget(onReachedTarget);
        recordManager.setLastSearch(player, searcher);
        // no need set route because it is dynamic
        searcher.startRouting(from, toVector, player.getWorld(), player, scorer, weight, checkingRadius, intervalCheck);
    }

    @Override
    public void findPath(Entity from, Entity to, World world) {
        var searcher = searchAlgorithmProvider.get();
        searcher.setOnUpdatedPath(onUpdated);
        searcher.setOnReachedTarget(onReachedTarget);
        searcher.startRouting(from, to, world, null, scorer, weight, checkingRadius, intervalCheck);
    }

    @Override
    public void setOnUpdated(Consumer<List<Vector>> route) {
        this.onUpdated = route;
    }

    @Override
    public void setOnReachedTarget(Consumer<Entity> onReachedTarget) {
        this.onReachedTarget = onReachedTarget;
    }
}
