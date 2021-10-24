package com.ericlam.mc.visiblepathfinder;

import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import com.ericlam.mc.visiblepathfinder.api.GraphSearchAlgorithm;
import com.ericlam.mc.visiblepathfinder.api.PathSearcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.inject.Provider;
import java.util.List;

public final class PathFinder implements PathSearcher {



    private final SearchRecordManager recordManager;
    private final ScheduleService scheduleService;
    private final VisiblePathFinder plugin;
    private final Provider<GraphSearchAlgorithm> searchAlgorithmProvider;
    private final DistanceScorer scorer;
    private final int range;

    public PathFinder(SearchRecordManager recordManager,
                      ScheduleService scheduleService,
                      VisiblePathFinder plugin,
                      Provider<GraphSearchAlgorithm> searchAlgorithmProvider,
                      int weight,
                      DistanceScorer scorer
    ) {
        this.recordManager = recordManager;
        this.scheduleService = scheduleService;
        this.plugin = plugin;
        this.searchAlgorithmProvider = searchAlgorithmProvider;
        this.range = weight;
        this.scorer = scorer;
    }

    @Override
    public ScheduleService.BukkitPromise<List<Vector>> findPathAsync(Location from, Location to, Player player) {
        return scheduleService.callAsync(plugin, () -> this.findPath(from, to, player));
    }

    @Override
    public ScheduleService.BukkitPromise<List<Vector>> findPathAsync(Location from, Location to, World world) {
        return scheduleService.callAsync(plugin, () ->  this.findPath(from, to, world));
    }

    @Override
    public List<Vector> findPath(Location from, Location to, Player player) {
        var fromVector = from.toBlockLocation().toVector();
        var toVector = to.toBlockLocation().toVector();
        recordManager.removeLastRoute(player);
        recordManager.removeLastSearch(player);
        var searcher = searchAlgorithmProvider.get();
        recordManager.setLastRoute(player, to);
        recordManager.setLastSearch(player, searcher);
        return searcher.search(fromVector, toVector, player.getWorld(), player, scorer, range);
    }

    @Override
    public List<Vector> findPath(Location from, Location to, World world) {
        var fromVector = from.toBlockLocation().toVector();
        var toVector = to.toBlockLocation().toVector();
        var searcher = searchAlgorithmProvider.get();
        return searcher.search(fromVector, toVector, world, null, scorer, range);
    }




}
