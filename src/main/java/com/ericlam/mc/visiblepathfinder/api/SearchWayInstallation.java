package com.ericlam.mc.visiblepathfinder.api;

public interface SearchWayInstallation {

    void installSearch(String searcher, Class<? extends GraphSearchAlgorithm> algorithm);

    void installDynamicSearch(String searcher, Class<? extends DynamicGraphSearchAlgorithm> algorithm);

}
