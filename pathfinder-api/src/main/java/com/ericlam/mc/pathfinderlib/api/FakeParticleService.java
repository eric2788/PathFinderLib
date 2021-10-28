package com.ericlam.mc.pathfinderlib.api;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface FakeParticleService {

    void spawnFixedParticles(Player player, Location location, Particle particle, @Nullable Object data, int numbers);

    void spawnFixedParticles(Player player, Location location, Particle particle, int numbers);

    void clearParticle(Player player);


}
