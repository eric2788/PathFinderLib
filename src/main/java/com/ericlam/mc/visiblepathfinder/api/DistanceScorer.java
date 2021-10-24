package com.ericlam.mc.visiblepathfinder.api;

import org.bukkit.util.Vector;

public interface DistanceScorer {

    double computeCost(Vector from, Vector to);

}
