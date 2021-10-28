package com.ericlam.mc.pathfinderlib.distances;

import com.ericlam.mc.pathfinderlib.api.DistanceScorer;
import org.bukkit.util.Vector;

public final class ChebyshevDistance implements DistanceScorer {

    @Override
    public double computeCost(Vector from, Vector to) {
        var dx = Math.abs(from.getBlockX() - to.getBlockX());
        var dy = Math.abs(from.getBlockY() - to.getBlockY());
        var dz = Math.abs(from.getBlockZ() - to.getBlockZ());
        return Math.max(Math.max(dx, dy), dz);
    }
}
