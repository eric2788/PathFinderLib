package com.ericlam.mc.visiblepathfinder.distances;

import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import org.bukkit.util.Vector;

public final class EuclideanDistance implements DistanceScorer {

    @Override
    public double computeCost(Vector from, Vector to) {
        return Math.sqrt(
                Math.pow(from.getX() - to.getX(), 2) +
                Math.pow(from.getY() - to.getY(), 2) +
                Math.pow(from.getZ() - to.getZ(), 2)
        );
    }
}
