package com.ericlam.mc.pathfinderlib.searcher;

import com.google.inject.Inject;
import com.google.inject.Injector;

public final class BiDStarSearcher extends DStarSearcher {

    @Inject
    public BiDStarSearcher(Injector injector) {
        super(injector);
        this.aStarSearcher = injector.getInstance(BiAStarSearcher.class);
    }

}
