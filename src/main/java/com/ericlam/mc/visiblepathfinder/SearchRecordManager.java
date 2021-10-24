package com.ericlam.mc.visiblepathfinder;

import com.ericlam.mc.visiblepathfinder.api.GraphSearchAlgorithm;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SearchRecordManager {

    @Inject
    private FakeParticleManager particleManager;
    @Inject
    private VisiblePathFinder plugin;

    private final Map<Player, RoutingRunnable> runnableMap = new ConcurrentHashMap<>();
    private final Map<Player, GraphSearchAlgorithm> lastSearch = new ConcurrentHashMap<>();

    public boolean removeLastSearch(Player player) {
        var last = lastSearch.remove(player);
        if (last != null) last.terminate();
        return last != null;
    }

    public void setLastSearch(Player player, GraphSearchAlgorithm algorithm) {
        lastSearch.put(player, algorithm);
    }

    public boolean removeLastRoute(Player player) {
        var runnable = runnableMap.remove(player);
        if (runnable != null) runnable.cancel();
        return runnable != null;
    }

    public void setLastRoute(Player player, Location to) {
        var runnable = new RoutingRunnable(player, to);
        runnableMap.put(player, runnable);
        runnable.runTaskTimer(plugin, 0L, 10L);
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
            if (player.getLocation().distance(dest) < 1 || !player.isOnline()) {
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
