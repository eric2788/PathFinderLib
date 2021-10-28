package com.ericlam.mc.pathfinderlib.distances;

import com.ericlam.mc.pathfinderlib.api.DistanceScorer;
import org.bukkit.util.Vector;

public final class BukkitVectorDistance implements DistanceScorer {

    @Override
    public double computeCost(Vector from, Vector to) {
        return from.distance(to);
    }
}
