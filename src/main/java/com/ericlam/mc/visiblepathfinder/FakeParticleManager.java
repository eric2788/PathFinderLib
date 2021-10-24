package com.ericlam.mc.visiblepathfinder;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.WrappedParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FakeParticleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeParticleManager.class);

    @Inject
    private VisiblePathFinder plugin;

    private final Map<Player, List<WrapperPlayServerWorldParticles>> particlePacketMap = new ConcurrentHashMap<>();
    private final Map<Player, FakeParticleRunnable> fakeParticleRunnableMap = new HashMap<>();

    public void spawnFixedParticles(Player player, Location location, WrappedParticle<?> particle, int numbers){
        WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();
        packet.setLongDistance(true);
        packet.setParticleType(particle);
        packet.setX(location.getX());
        packet.setY(location.getY()+1);
        packet.setZ(location.getZ());
        packet.setNumberOfParticles(numbers);
        particlePacketMap.putIfAbsent(player, new ArrayList<>());
        particlePacketMap.get(player).add(packet);
        if (!fakeParticleRunnableMap.containsKey(player)){
            var runnable = new FakeParticleRunnable(player);
            runnable.runTaskTimer(plugin, 0L, 1L);
            fakeParticleRunnableMap.put(player, runnable);
        }
    }

    public void clearParticle(Player player){
        var runnable = fakeParticleRunnableMap.remove(player);
        if (runnable != null){
            runnable.cancel();
        }
        particlePacketMap.remove(player);
    }


    private class FakeParticleRunnable extends BukkitRunnable {

        private final Player player;

        public FakeParticleRunnable(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            if (!player.isOnline()) {
                clearParticle(player);
                return;
            }
            var particlePackets = particlePacketMap.get(player);
            if (particlePackets == null) return;
            particlePackets.forEach(packet -> {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet.getHandle());
                } catch (InvocationTargetException e) {
                    LOGGER.warn("Error while sending packet to {}", player.getName(), e);
                }
            });
        }
    }
}
