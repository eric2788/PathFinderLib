package com.ericlam.mc.visiblepathfinder.api;

import org.bukkit.entity.Player;

public interface PathSearchingAPI<T, B extends PathSearchingAPI.SearcherBuilder<T>> {

    boolean hasPathSearcher(String searcher);

    boolean hasDistanceAlgorithm(String distance);

    T getPathSearcher(String searcher);

    boolean terminateSearch(Player player);

    B buildPathSearcher(String searcher);

    interface SearcherBuilder<T> {

        T build();

    }

}
