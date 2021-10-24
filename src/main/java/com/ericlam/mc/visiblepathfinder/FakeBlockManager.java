package com.ericlam.mc.visiblepathfinder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class FakeBlockManager {

    private static class ShowBlock {

        final Location location;
        final Material showMaterial;

        private ShowBlock(Location location, Material showMaterial) {
            this.location = location;
            this.showMaterial = showMaterial;
        }
    }

    private final Map<Player, Queue<ShowBlock>> playerQueueMap = new ConcurrentHashMap<>();
    private final Map<Player, FakeBlockRunnable> runnableMap = new HashMap<>();

    @Inject
    private VisiblePathFinder plugin;


    public void showFakeBlock(Location location, Player player, Material material) {
        playerQueueMap.putIfAbsent(player, new ConcurrentLinkedDeque<>());
        playerQueueMap.get(player).add(new ShowBlock(location, material));
        if (!runnableMap.containsKey(player)) {
            var runnable = new FakeBlockRunnable(player);
            runnable.runTaskTimer(plugin, 0L, 5L);
            runnableMap.put(player, runnable);
        }
    }



    private class FakeBlockRunnable extends BukkitRunnable {

        private final Player player;
        private transient boolean running = false;

        private FakeBlockRunnable(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            if (running) return;
            running = true;
            var queue = playerQueueMap.get(player);
            if (queue != null) {
                while (!queue.isEmpty()) {
                    var show = queue.poll();
                    player.sendBlockChange(show.location, show.showMaterial.createBlockData());
                }
            }
            running = false;
        }
    }

}
