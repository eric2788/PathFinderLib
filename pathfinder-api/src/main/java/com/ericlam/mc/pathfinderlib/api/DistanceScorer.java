package com.ericlam.mc.pathfinderlib.api;

import org.bukkit.util.Vector;

public interface DistanceScorer {

    double computeCost(Vector from, Vector to);

}
