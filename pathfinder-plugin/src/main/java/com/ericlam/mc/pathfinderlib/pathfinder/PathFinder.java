package com.ericlam.mc.pathfinderlib.pathfinder;

import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.pathfinderlib.PathFinderLib;
import com.ericlam.mc.pathfinderlib.api.DistanceScorer;
import com.ericlam.mc.pathfinderlib.api.algorithm.GraphSearchAlgorithm;
import com.ericlam.mc.pathfinderlib.api.searcher.PathSearcher;
import com.ericlam.mc.pathfinderlib.manager.SearchRecordManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.inject.Provider;
import java.util.List;

public final class PathFinder extends PathFinderBase<GraphSearchAlgorithm> implements PathSearcher {

    public PathFinder(SearchRecordManager recordManager,
                      ScheduleService scheduleService,
                      PathFinderLib plugin,
                      Provider<GraphSearchAlgorithm> searchAlgorithmProvider,
                      int weight,
                      DistanceScorer scorer) {
        super(recordManager, scheduleService, plugin, searchAlgorithmProvider, weight, scorer);
    }

    @Override
    public ScheduleService.BukkitPromise<List<Vector>> findPathAsync(Location from, Location to, Player player) {
        return scheduleService.callAsync(plugin, () -> this.findPath(from, to, player));
    }

    @Override
    public ScheduleService.BukkitPromise<List<Vector>> findPathAsync(Location from, Location to, World world) {
        return scheduleService.callAsync(plugin, () -> this.findPath(from, to, world));
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
        return searcher.search(fromVector, toVector, player.getWorld(), player, scorer, weight);
    }

    @Override
    public List<Vector> findPath(Location from, Location to, World world) {
        var fromVector = from.toBlockLocation().toVector();
        var toVector = to.toBlockLocation().toVector();
        var searcher = searchAlgorithmProvider.get();
        return searcher.search(fromVector, toVector, world, null, scorer, weight);
    }


}
