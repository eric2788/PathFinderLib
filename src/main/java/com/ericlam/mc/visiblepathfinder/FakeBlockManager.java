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

    /* no need queue
    private static class ShowBlock {

        final Location location;
        final Material showMaterial;

        private ShowBlock(Location location, Material showMaterial) {
            this.location = location;
            this.showMaterial = showMaterial;
        }
    }

     */

    private final Map<Player, Queue<Location>> fakePlacedQueue = new ConcurrentHashMap<>();

    /* no need runnable and queue map due to can send async
     private final Map<Player, Queue<ShowBlock>> playerQueueMap = new ConcurrentHashMap<>();
     private final Map<Player, FakeBlockRunnable> runnableMap = new HashMap<>();
     */

    @Inject
    private VisiblePathFinder plugin;


    public void showFakeBlock(Location location, Player player, Material material) {
        fakePlacedQueue.putIfAbsent(player, new ConcurrentLinkedDeque<>());
        player.sendBlockChange(location, material.createBlockData());
        fakePlacedQueue.get(player).add(location);
        /* no need runnable due to fake block can send async
        if (!runnableMap.containsKey(player)) {
            playerQueueMap.putIfAbsent(player, new ConcurrentLinkedDeque<>());
            fakePlacedQueue.putIfAbsent(player, new ConcurrentLinkedDeque<>());
            var runnable = new FakeBlockRunnable(player);
            runnable.runTaskTimer(plugin, 0L, 5L);
            runnableMap.put(player, runnable);
        }
        playerQueueMap.get(player).add(new ShowBlock(location, material));

         */
    }

    public void clearAllFakeBlock(Player player) {
        if (!fakePlacedQueue.containsKey(player)) return;
        var queue = fakePlacedQueue.get(player);
        while (!queue.isEmpty()) {
            var loc = queue.poll();
            player.sendBlockChange(loc, loc.getBlock().getBlockData());
            loc.getBlock().getState().update(true);
        }
    }


    /* fake block can send async, no need runnable
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
            var placedQueue = fakePlacedQueue.get(player);
            while (!queue.isEmpty()) {
                var show = queue.poll();
                player.sendBlockChange(show.location, show.showMaterial.createBlockData());
                placedQueue.add(show.location);

            }
            running = false;
        }
    }

     */

}
