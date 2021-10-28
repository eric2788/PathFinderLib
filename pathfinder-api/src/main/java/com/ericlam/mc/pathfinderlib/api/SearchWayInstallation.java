package com.ericlam.mc.pathfinderlib.api;

import com.ericlam.mc.pathfinderlib.api.algorithm.DynamicGraphSearchAlgorithm;
import com.ericlam.mc.pathfinderlib.api.algorithm.GraphSearchAlgorithm;

public interface SearchWayInstallation {

    void installSearch(String searcher, Class<? extends GraphSearchAlgorithm> algorithm);

    void installDynamicSearch(String searcher, Class<? extends DynamicGraphSearchAlgorithm> algorithm);

}
