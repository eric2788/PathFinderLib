package com.ericlam.mc.visiblepathfinder.api;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;

public interface DynamicGraphSearchAlgorithm extends SearchAlgorithm {

    void startRouting(Entity from,
                      Entity to,
                      World world,
                      @Nullable Player observer,
                      DistanceScorer scorer,
                      int weight,
                      int maxRange,
                      long tickCheck
    );

    void startRouting(Entity from,
                      Vector to,
                      World world,
                      @Nullable Player observer,
                      DistanceScorer scorer,
                      int weight,
                      int maxRange,
                      long tickCheck
    );

    void setOnUpdatedPath(Consumer<List<Vector>> onUpdated);

    void setOnReachedTarget(Consumer<Entity> onReachedTarget);

}
