package com.ericlam.mc.visiblepathfinder.api;

public interface SearchWayInstallation {

    void installSearch(String searcher, Class<? extends GraphSearchAlgorithm> algorithmCls);

}
