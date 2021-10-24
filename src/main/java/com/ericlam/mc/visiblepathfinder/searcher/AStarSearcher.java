package com.ericlam.mc.visiblepathfinder.searcher;

import com.ericlam.mc.visiblepathfinder.Debugger;
import com.ericlam.mc.visiblepathfinder.MCMechanism;
import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import com.ericlam.mc.visiblepathfinder.api.GraphSearchAlgorithm;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

public class AStarSearcher implements GraphSearchAlgorithm {


    @Inject
    protected Debugger debugger;
    @Inject
    protected MCMechanism mcMechanism;

    private transient boolean terminate = false;

    @Override
    public List<Vector> search(Vector from, Vector to, World world, @Nullable Player player, DistanceScorer scorer, int weight) {
        Queue<RouteNode> openSet = new PriorityQueue<>();
        Map<Vector, RouteNode> allNodes = new HashMap<>();
        var cost = scorer.computeCost(from, to);
        RouteNode start = new RouteNode(from, null, 0d, cost);
        debugger.log("開始節點 {} 與 目標 {} 的距離為 {}", from, to, cost);
        openSet.add(start);
        allNodes.put(from, start);
        while (!openSet.isEmpty()) {

            RouteNode next = openSet.poll();
            if (next.current.equals(to)) {
                debugger.log("節點 {} 已是目的地", next.current);
                List<Vector> route = new ArrayList<>();
                RouteNode current = next;
                do {
                    debugger.log("正在添加節點 {} 作為路徑", current.current);
                    route.add(0, current.current);
                    current = allNodes.get(current.previous);
                } while (current != null);
                return route;
            }

            debugger.log("正在尋找節點 {} 的鄰近節點..", next.current);
            var neighbours = this.findNeighbours(next.current, world, player);
            this.findSuccessors(neighbours, next, to, allNodes, openSet, scorer, weight);

            if (player != null && !player.isOnline()){
                debugger.log("玩家已離線，搜索中止。");
                return List.of();
            }

            if (terminate){
                debugger.log("搜索被強行中止。");
                return List.of();
            }

        }

        debugger.log("找不到路徑，已返回空路徑。");
        // 找不到路徑
        return List.of();
    }


    @Override
    public void terminate() {
        terminate = true;
    }

    protected void findSuccessors(Set<Vector> neighbours,
                                  RouteNode next,
                                  Vector to,
                                  Map<Vector, RouteNode> allNodes,
                                  Queue<RouteNode> openSet,
                                  DistanceScorer scorer,
                                  int weight){
        debugger.log("節點 {} 有 {} 個鄰近節點。", next.current, neighbours.size());
        for (Vector connection : neighbours) {
            RouteNode nextNode = allNodes.getOrDefault(connection, new RouteNode(connection));
            allNodes.put(connection, nextNode);
            double newScore = next.routeScore + scorer.computeCost(next.current, connection);
            debugger.log("節點 {} 的新分數: {}, 與前分數相比: {} < {} = {}",
                    connection, newScore, newScore, nextNode.routeScore, newScore < nextNode.routeScore);
            if (newScore < nextNode.routeScore) {
                nextNode.previous = next.current;
                nextNode.routeScore = newScore;
                var hScore = weight * scorer.computeCost(connection, to);
                nextNode.estimatedScore = newScore + hScore;
                debugger.log("節點 {} g = {}, h = {}, f = {}", newScore, hScore, nextNode.estimatedScore);
                openSet.add(nextNode);
            }
        }
    }


    protected Set<Vector> findNeighbours(Vector current, World world, @Nullable Player player) {
        var neighbours = new HashSet<Vector>();
        // 必須克隆，否則會直接修改 current 本身
        var leftTopCorn = current.clone().add(new Vector(1, 1, 1));
        var rightTopBottom = current.clone().add(new Vector(-1, -1, -1));

        var dx = Math.abs(leftTopCorn.getBlockX() - rightTopBottom.getBlockX());
        var dy = Math.abs(leftTopCorn.getBlockY() - rightTopBottom.getBlockY());
        var dz = Math.abs(leftTopCorn.getBlockZ() - rightTopBottom.getBlockZ());

        // 只尋找節點鄰近所有節點
        for (int x = 0; x <= dx; x++) {
            for (int y = 0; y <= dy; y++) {
                for (int z = 0; z <= dz; z++) {
                    Vector vector = rightTopBottom.clone().add(new Vector(x, y, z));
                    var passable = mcMechanism.isPassable(vector, world);
                    var onGround = mcMechanism.isOnGround(vector, world);
                    debugger.log("節點 {} 的鄰近節點 {}, 方塊是否可通過: {}, 方塊是否在地: {}", current, vector, passable, onGround);
                    if (passable && onGround) { // 如果方塊無法通過或在地，則不添加到鄰近節點
                        if (player != null) {
                            debugger.debugBlock(vector.toLocation(world), player);
                            //debugger.debugParticle(vector.toLocation(world), player);
                        }
                        neighbours.add(vector);
                    }
                }
            }
        }

        return neighbours;
    }

    static class RouteNode implements Comparable<RouteNode> {

        public final Vector current;
        public Vector previous;
        public double routeScore;
        public double estimatedScore;

        RouteNode(Vector current) {
            this(current, null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        RouteNode(Vector current, Vector previous, double routeScore, double estimatedScore) {
            this.current = current;
            this.previous = previous;
            this.routeScore = routeScore;
            this.estimatedScore = estimatedScore;
        }


        @Override
        public int compareTo(@NotNull RouteNode other) {
            return Double.compare(this.estimatedScore, other.estimatedScore);
        }


    }
}
