package com.ericlam.mc.visiblepathfinder;

import com.comphenix.protocol.wrappers.WrappedParticle;
import com.ericlam.mc.visiblepathfinder.api.GraphSearchAlgorithm;
import com.ericlam.mc.visiblepathfinder.config.VPFConfig;
import com.ericlam.mc.visiblepathfinder.manager.FakeBlockManager;
import com.ericlam.mc.visiblepathfinder.manager.FakeParticleManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public final class Debugger {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphSearchAlgorithm.class);

    @Inject
    private FakeParticleManager particleManager;
    @Inject
    private FakeBlockManager blockManager;
    @Inject
    private VPFConfig config;

    public void debugBlock(Location location, Player player) {
        if (!config.debug) return;
        blockManager.showFakeBlock(location, player, Material.REDSTONE_TORCH);
    }

    public void debugParticle(Location location, Player player) {
        if (!config.debug) return;
        particleManager.spawnFixedParticles(player, location, WrappedParticle.create(Particle.FLAME, null), 10);
    }

    public void log(String message, Object... paras) {
        if (!config.debug) return;
        LOGGER.info(message, paras);
    }
}
