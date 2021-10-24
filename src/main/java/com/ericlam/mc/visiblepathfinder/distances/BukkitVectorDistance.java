package com.ericlam.mc.visiblepathfinder.distances;

import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import org.bukkit.util.Vector;

public final class BukkitVectorDistance implements DistanceScorer {

    @Override
    public double computeCost(Vector from, Vector to) {
        return from.distance(to);
    }
}
