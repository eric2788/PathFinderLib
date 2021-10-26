package com.ericlam.mc.visiblepathfinder.searcher;

import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class BiAStarSearcher extends AStarSearcher {

    @Inject
    private Injector injector;

    private AStarSearcher asAsc;
    private AStarSearcher asDsc;

    @Override
    public List<Vector> search(Vector from, Vector to, World world, @Nullable Player player, DistanceScorer scorer, int weight) {
        asAsc = injector.getInstance(AStarSearcher.class);
        asDsc = injector.getInstance(AStarSearcher.class);

        var maxCost = asAsc.initialize(from, to, weight, scorer);
        asDsc.initialize(to, from, weight, scorer);

        while (!asAsc.openSet.isEmpty() && !asDsc.openSet.isEmpty()){
            var next1 = asAsc.openSet.poll();
            var next2 = asDsc.openSet.poll();

            if (next1 != null && next2 != null){

                if (next1.current.equals(next2.current)){
                    return backTraces(next1, next2);
                }

                // 發送進度 (允許異步)
                mcMechanism.sendProgress(player, maxCost, next1.estimatedScore + next2.estimatedScore);

                var neighbours = asAsc.findNeighbours(next1.current, world, player);
                asAsc.findSuccessors(neighbours, next1, next2.current, scorer, weight);

                neighbours = asDsc.findNeighbours(next2.current, world, player);
                asDsc.findSuccessors(neighbours, next2, next1.current, scorer, weight);

            }

        }

        debugger.log("找不到路徑。");
        return List.of();
    }



    private List<Vector> backTraces(RouteNode ascNode, RouteNode dscNode){
        var asPath = asAsc.retracePath(ascNode);
        var dsPath = asDsc.retracePath(dscNode);
        dsPath.remove(dsPath.size()-1);
        Collections.reverse(dsPath);
        asPath.addAll(dsPath);
        return asPath;
    }

    @Override
    public void terminate() {
        super.terminate();
        asAsc.terminate();
        asDsc.terminate();
    }
}
