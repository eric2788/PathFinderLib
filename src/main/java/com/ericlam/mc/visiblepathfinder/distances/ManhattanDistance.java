package com.ericlam.mc.visiblepathfinder.distances;

import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import org.bukkit.util.Vector;

public final class ManhattanDistance implements DistanceScorer {

    @Override
    public double computeCost(Vector from, Vector to) {
        return Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY()) + Math.abs(from.getZ() - to.getZ());
    }
}
