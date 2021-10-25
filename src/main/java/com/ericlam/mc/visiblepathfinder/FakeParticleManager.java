package com.ericlam.mc.visiblepathfinder;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.WrappedParticle;
import com.ericlam.mc.visiblepathfinder.config.VPFConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class FakeParticleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeParticleManager.class);

    @Inject
    private VPFConfig config;
    @Inject
    private VisiblePathFinder plugin;

    private final Map<Player, List<WrapperPlayServerWorldParticles>> particlePacketMap = new ConcurrentHashMap<>();
    private final Map<Player, FakeParticleRunnable> fakeParticleRunnableMap = new HashMap<>();

    public void spawnFixedParticles(Player player, Location location, WrappedParticle<?> particle, int numbers) {
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();
        packet.setLongDistance(true);
        packet.setParticleType(particle);
        packet.setX(location.getX());
        packet.setY(location.getY() + 1);
        packet.setZ(location.getZ());
        packet.setNumberOfParticles(numbers);
        particlePacketMap.putIfAbsent(player, new ArrayList<>());
        particlePacketMap.get(player).add(packet);
        if (!fakeParticleRunnableMap.containsKey(player)) {
            var runnable = new FakeParticleRunnable(player, particlePacketMap.get(player));
            runnable.runTaskTimerAsynchronously(plugin, 0L, config.particle_settings.interval_per_trace);
            fakeParticleRunnableMap.put(player, runnable);
        }
    }

    public void clearParticle(Player player) {
        var runnable = fakeParticleRunnableMap.remove(player);
        if (runnable != null) {
            runnable.cancel();
        }
        var list = particlePacketMap.remove(player);
        if (list != null){
            list.clear();
        }
    }

    private class TracerRunnable extends BukkitRunnable {

        private final Player player;
        private final Iterator<WrapperPlayServerWorldParticles> iterator;

        private TracerRunnable(Player player, List<WrapperPlayServerWorldParticles> particles) {
            this.player = player;
            this.iterator = new LinkedList<>(particles).iterator();
        }

        @Override
        public void run() {

            if (!player.isOnline()) {
                cancel();
                return;
            }

            for (int i = 0; i < config.particle_settings.loop_per_tick; i++) {
                if (iterator.hasNext()) {
                    var packet = iterator.next();
                    packet.sendPacket(player);
                } else {
                    cancel();
                }
            }
        }
    }


    private class FakeParticleRunnable extends BukkitRunnable {

        private final Player player;
        private final List<WrapperPlayServerWorldParticles> particles;

        private FakeParticleRunnable(
                Player player,
                List<WrapperPlayServerWorldParticles> particles
        ) {
            this.player = player;
            this.particles = particles;
        }

        @Override
        public void run() {
            if (!player.isOnline() || particles == null || particles.isEmpty()) {
                clearParticle(player);
                return;
            }
            new TracerRunnable(player, particles)
                    .runTaskTimerAsynchronously(plugin, 0L, config.particle_settings.interval_per_loop);
        }

    }
}
