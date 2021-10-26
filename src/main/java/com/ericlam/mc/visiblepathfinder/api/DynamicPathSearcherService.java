package com.ericlam.mc.visiblepathfinder.api;

public interface DynamicPathSearcherService extends PathSearchingAPI<DynamicPathSearcher, DynamicPathSearcherService.DynamicPathSearcherBuilder> {

    interface DynamicPathSearcherBuilder extends SearcherBuilder<DynamicPathSearcher> {

        DynamicPathSearcherBuilder setSearcherAlgorithm(String searcher);

        DynamicPathSearcherBuilder setDistanceAlgorithm(String distanceAlgorithm);

        DynamicPathSearcherBuilder setWeight(int weight);

        DynamicPathSearcherBuilder checkingRadius(int radius);

        DynamicPathSearcherBuilder intervalPerCheck(long ticks);

    }
}
