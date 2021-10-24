package com.ericlam.mc.visiblepathfinder.searcher;

import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import org.apache.commons.lang.Validate;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// A* improved algorithm
public final class TraceSearcher extends AStarSearcher {

    private World world;
    private Player player;

    @Override
    public List<Vector> search(Vector from, Vector to, World world, @Nullable Player player, DistanceScorer scorer, int weight) {
        this.world = world;
        this.player = player;
        return super.search(from, to, world, player, scorer, weight);
    }


    @Override
    protected void findSuccessors(Set<Vector> neighbours,
                                  RouteNode next,
                                  Vector to,
                                  Map<Vector, RouteNode> allNodes,
                                  Queue<RouteNode> openSet,
                                  DistanceScorer scorer,
                                  int weight
    ) {
        this.findSuccessorsTrace(neighbours, next, to, allNodes, openSet, scorer, weight);
    }

    private void findSuccessorsTrace(Set<Vector> neighbours,
                                     RouteNode next, Vector to,
                                     Map<Vector, RouteNode> allNodes,
                                     Queue<RouteNode> openSet,
                                     DistanceScorer scorer,
                                     int weight
    ) {
        Validate.notNull(world, "world is null");
        debugger.log("節點 {} 有 {} 個鄰近節點。", next.current, neighbours.size());
        for (Vector connection : neighbours) {

            RouteNode nextNode = allNodes.getOrDefault(connection, new RouteNode(connection));
            allNodes.put(connection, nextNode);
            double newScore = next.routeScore + scorer.computeCost(next.current, connection);
            debugger.log("節點 {} 的新分數: {}, 與前分數相比: {} < {} = {}",
                    connection, newScore, newScore, nextNode.routeScore, newScore < nextNode.routeScore);

            if (newScore < nextNode.routeScore) {
                nextNode.previous = next.current;
                // g score
                nextNode.routeScore = newScore;
                // weight
                double n = (double) this.findNeighbours(connection, world, player).size() / 36;
                // h score
                double h = scorer.computeCost(connection, to);
                // f = gscore + h score
                nextNode.estimatedScore = (newScore * n) + h;
                debugger.log("節點 {} g = {}, h = {}, f = {}", newScore, h, nextNode.estimatedScore);
                openSet.add(nextNode);
            }
        }
    }
}
