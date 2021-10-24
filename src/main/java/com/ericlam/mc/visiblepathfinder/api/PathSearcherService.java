package com.ericlam.mc.visiblepathfinder.api;

import org.bukkit.entity.Player;

public interface PathSearcherService {

    boolean hasPathSearcher(String searcher);

    boolean hasDistanceAlgorithm(String distance);

    PathSearcher getPathSearcher(String searcher);

    PathSearcherBuilder buildPathSearcher(String searcher);

    boolean terminateSearch(Player player);

    interface PathSearcherBuilder {

        PathSearcherBuilder setSearcherAlgorithm(String searcher);

        PathSearcherBuilder setDistanceAlgorithm(String distanceAlgorithm);

        PathSearcherBuilder setWeight(int weight);

        PathSearcher build();
    }

}
