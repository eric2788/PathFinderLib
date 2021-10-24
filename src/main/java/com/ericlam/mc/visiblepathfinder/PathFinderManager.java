package com.ericlam.mc.visiblepathfinder;

import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.visiblepathfinder.api.PathFinderService;
import com.ericlam.mc.visiblepathfinder.api.PathSearcher;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PathFinderManager implements PathFinderService {

    @Inject
    private ScheduleService scheduleService;
    @Inject
    private VisiblePathFinder plugin;
    @Inject
    private FakeParticleManager particleManager;

    private final Map<Player, RoutingRunnable> runnableMap = new ConcurrentHashMap<>();

    @Inject
    @Named("astar")
    private PathSearcher searcher;

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
        var runnable = runnableMap.remove(player);
        if (runnable != null) runnable.cancel();
        runnable = new RoutingRunnable(player, to);
        runnableMap.put(player, runnable);
        runnable.runTaskTimer(plugin, 0L, 10L);
        return searcher.search(fromVector, toVector, player.getWorld(), player);
    }

    @Override
    public List<Vector> findPath(Location from, Location to, World world) {
        var fromVector = from.toBlockLocation().toVector();
        var toVector = to.toBlockLocation().toVector();
        return searcher.search(fromVector, toVector, world, null);
    }

    private class RoutingRunnable extends BukkitRunnable {

        private final Player player;
        private final Location dest;

        private RoutingRunnable(Player player, Location dest) {
            this.player = player;
            this.dest = dest;
        }

        @Override
        public void run() {
            if (player.getLocation().distance(dest) < 1 || !player.isOnline()){
                player.sendMessage("§a已抵達目的地");
                this.cancel();
            }
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            particleManager.clearParticle(player);
            super.cancel();
        }
    }


}
