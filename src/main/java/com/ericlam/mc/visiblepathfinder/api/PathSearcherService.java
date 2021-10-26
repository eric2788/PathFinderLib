package com.ericlam.mc.visiblepathfinder.api;

public interface PathSearcherService extends PathSearchingAPI<PathSearcher, PathSearcherService.PathSearcherBuilder> {

    interface PathSearcherBuilder extends SearcherBuilder<PathSearcher> {

        PathSearcherBuilder setSearcherAlgorithm(String searcher);

        PathSearcherBuilder setDistanceAlgorithm(String distanceAlgorithm);

        PathSearcherBuilder setWeight(int weight);

    }


}
