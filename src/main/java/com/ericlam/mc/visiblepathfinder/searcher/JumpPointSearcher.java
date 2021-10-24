package com.ericlam.mc.visiblepathfinder.searcher;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// 太麻煩，暫時拋棄
@Deprecated
public final class JumpPointSearcher extends AStarSearcher {

    @Override
    public List<Vector> search(Vector from, Vector to, World world, @Nullable Player player) {
        return List.of();
    }

    public Queue<Vector> findPathSync(Vector start, Vector goal, World world, @Nullable Player player) {

        Map<Vector, Double> fMap = new HashMap<>(); // distance to start + estimate to end
        Map<Vector, Double> gMap = new HashMap<>(); // distance to start (parent's g + distance from parent)
        Map<Vector, Double> hMap = new HashMap<>(); // estimate to end

        Queue<Vector> open = new PriorityQueue<>(Comparator.comparingDouble(a -> fMap.getOrDefault(a, 0d)));

        Set<Vector> closed = new HashSet<>();
        Map<Vector, Vector> parentMap = new HashMap<>();

        Set<Vector> goals = findNeighbors(goal, parentMap, world, player);

        if (walkable(goal, world)) {
            goals.add(goal);
        }
        if (goals.isEmpty()) {
            return null;
        }

        debugger.log("開始節點: {}", start);
        // push the start node into the open list
        open.add(start);

        // while the open list is not empty
        while (!open.isEmpty()) {
            //System.out.println(open.size());
            // pop the position of node which has the minimum `f` value.
            Vector node = open.poll();
            // mark the current node as checked
            closed.add(node);

            if (goals.contains(node)) {
                return backtrace(node, parentMap);
            }
            // add all possible next steps from the current node
            identifySuccessors(node, goal, goals, open, closed, parentMap, fMap, gMap, hMap, world, player);
        }

        // failed to find a path
        return null;
    }

    boolean walkable(Vector vector, World world) {
        return mcMechanism.isOnGround(vector, world) && mcMechanism.isPassable(vector, world);
    }

    private Vector plus(Vector sample, double x, double y, double z) {
        return sample.clone().add(new Vector(x, y, z));
    }

    Set<Vector> findNeighbors(Vector node, Map<Vector, Vector> parentMap, World world, @Nullable Player player) {
        Set<Vector> neighbors = new HashSet<>();

        var parent = parentMap.get(node);

        // directed pruning: can ignore most neighbors, unless forced.
        if (parent != null) {
            final double x = node.getX();
            final double y = node.getY();
            final double z = node.getZ();
            // get normalized direction of travel
            final double dx = (x - parent.getX()) / Math.max(Math.abs(x - parent.getX()), 1);
            final double dy = (y - parent.getY()) / Math.max(Math.abs(y - parent.getY()), 1);
            final double dz = (z - parent.getZ()) / Math.max(Math.abs(z - parent.getZ()), 1);


            // search diagonally
            if (dx != 0 && dy != 0 && dz != 0) {
                // Y
                if (walkable(plus(node, 0, dy, 0), world)) {
                    neighbors.add(plus(node, 0, dy, 0));
                }
                // X
                if (walkable(plus(node, dx, 0, 0), world)) {
                    neighbors.add(plus(node, dx, 0, 0));
                }
                // Z
                if (walkable(plus(node, 0, 0, dz), world)) {
                    neighbors.add(plus(node, 0, 0, dz));
                }

                // X Y Z 對角
                if (walkable(plus(node, dx, dy, dz), world)) {
                    neighbors.add(plus(node, dx, dy, dz));
                }

                //
                if (!walkable(plus(node, -dx, 0, 0), world)) {
                    neighbors.add(plus(node, -dx, +dy, +dz));
                }
                if (!walkable(plus(node, 0, -dy, 0), world)) {
                    neighbors.add(plus(node, +dx, -dy, +dz));
                }
                if (!walkable(plus(node, 0, 0, -dz), world)) {
                    neighbors.add(plus(node, +dx, +dy, -dz));
                }


            } else {
                if (dx == 0) { // assume dX == 0
                    // dY
                    if (walkable(plus(node, 0, dy, 0), world)) {
                        neighbors.add(plus(node, 0, dy, 0));
                    }
                    // dZ
                    if (walkable(plus(node, 0, 0, dz), world)) {
                        neighbors.add(plus(node, 0, 0, dz));
                    }

                    // plus
                    if (!walkable(plus(node, +1, 0, 0), world)) {
                        neighbors.add(plus(node, +1, +dy, +dz));
                    }
                    // minus
                    if (!walkable(plus(node, -1, 0, 0), world)) {
                        neighbors.add(plus(node, -1, +dy, +dz));
                    }

                } else if (dy == 0) {
                    // dX
                    if (walkable(plus(node, dx, 0, 0), world)) {
                        neighbors.add(plus(node, dx, 0, 0));
                    }
                    // dZ
                    if (walkable(plus(node, 0, 0, dz), world)) {
                        neighbors.add(plus(node, 0, 0, dz));
                    }
                    // plus
                    if (!walkable(plus(node, 0, +1, 0), world)) {
                        neighbors.add(plus(node, +dx, +1, +dz));
                    }
                    // minus
                    if (!walkable(plus(node, 0, -1, 0), world)) {
                        neighbors.add(plus(node, +dx, -1, +dz));
                    }
                } else { // dz == 0
                    // dX
                    if (walkable(plus(node, dx, 0, 0), world)) {
                        neighbors.add(plus(node, dx, 0, 0));
                    }
                    // dY
                    if (walkable(plus(node, 0, dy, 0), world)) {
                        neighbors.add(plus(node, 0, dy, 0));
                    }
                    // plus
                    if (!walkable(plus(node, 0, 0, +1), world)) {
                        neighbors.add(plus(node, +dx, +dy, +1));
                    }
                    // minus
                    if (!walkable(plus(node, 0, 0, -1), world)) {
                        neighbors.add(plus(node, +dx, +dy, -1));
                    }
                }
            }
        } else {
            // no parent, return all neighbors
            neighbors.addAll(super.findNeighbours(node, world, player));
        }

        return neighbors;
    }


    /**
     * Identify successors for the given node. Runs a JPS in direction of each available neighbor, adding any open
     * nodes found to the open list.
     *
     * @return All the nodes we have found jumpable from the current node
     */
    private void identifySuccessors(Vector node, Vector goal,
                                    Set<Vector> goals, Queue<Vector> open,
                                    Set<Vector> closed, Map<Vector, Vector> parentMap,
                                    Map<Vector, Double> fMap,
                                    Map<Vector, Double> gMap,
                                    Map<Vector, Double> hMap,
                                    World world,
                                    @Nullable Player player
    ) {
        // get all neighbors to the current node
        Collection<Vector> neighbors = findNeighbors(node, parentMap, world, player);

        double d;
        double ng;
        for (Vector neighbor : neighbors) {
            // jump in the direction of our neighbor
            Vector jumpNode = jump(neighbor, node, goals, world);

            // don't add a node we have already gotten to quicker
            if (jumpNode == null || closed.contains(jumpNode)) continue;

            // determine the jumpNode's distance from the start along the current path
            d = scorer.computeCost(jumpNode, node);
            ng = gMap.getOrDefault(node, 0d) + d;

            // if the node has already been opened and this is a shorter path, update it
            // if it hasn't been opened, mark as open and update it
            if (!open.contains(jumpNode) || ng < gMap.getOrDefault(jumpNode, 0d)) {
                gMap.put(jumpNode, ng);
                hMap.put(jumpNode, scorer.computeCost(jumpNode, goal));
                fMap.put(jumpNode, gMap.getOrDefault(jumpNode, 0d) + hMap.getOrDefault(jumpNode, 0d));
                //System.out.println("jumpNode: " + jumpNode.x + "," + jumpNode.y + " f: " + fMap.get(jumpNode));
                parentMap.put(jumpNode, node);

                if (!open.contains(jumpNode)) {
                    open.offer(jumpNode);
                }
            }
        }
    }

    /**
     * Returns a path of the parent nodes from a given node.
     */
    private Queue<Vector> backtrace(Vector node, Map<Vector, Vector> parentMap) {
        LinkedList<Vector> path = new LinkedList<>();
        path.add(node);

        double previousX, previousY, previousZ, currentX, currentY, currentZ;
        double dx, dy, dz;
        double steps;
        Vector temp;
        while (parentMap.containsKey(node)) {
            var previous = parentMap.get(node);
            previousX = previous.getX();
            previousY = previous.getY();
            previousZ = previous.getZ();
            currentX = node.getX();
            currentY = node.getY();
            currentZ = node.getZ();
            steps = Double.max(Double.max(Math.abs(previousX - currentX), Math.abs(previousY - currentY)), Math.abs(previousZ - currentZ));
            dx = Double.compare(previousX, currentX);
            dy = Double.compare(previousY, currentY);
            dz = Double.compare(previousZ, currentZ);


            for (int i = 0; i < steps; i++) {
                temp = plus(node, +dx, +dy, +dz);
                path.addFirst(temp);
            }

            node = parentMap.get(previous);
        }
        return path;
    }

    private Vector jump(Vector neighbor, Vector current, Set<Vector> goals, World world) {
        /*
        if (neighbor == null || !walkable(neighbor, world)) return null;
        if (goals.contains(neighbor)) return neighbor;

        double dx = neighbor.getX() - current.getX();
        double dy = neighbor.getY() - current.getY();
        double dz = neighbor.getZ() - current.getZ();

        // check for forced neighbors
        // check along diagonal
        if (dx != 0 && dy != 0) {
            walkable(plus(neighbor, -dx, +dy, 0), world);
            walkable(plus(neighbor, -dx, 0, 0), world); //!
            walkable(plus(neighbor, -dx, 0, -1), world); //!
            // ||
            walkable(plus(neighbor, +dx, -dy, 0), world);
            walkable(plus(neighbor, 0, -dy, 0),world); //!
            walkable(plus(neighbor, 0, -dy, -1), world); //!
            // ||
            walkable(plus(neighbor, -dx, 0, +dz), world);
            walkable(plus(neighbor, -dx, 0, 0), world); // !
            walkable(plus(neighbor, -dx, -dy, 0), world); //!
            // ||
            walkable(plus(neighbor, -dx, -dy, +dz), world);
            walkable(plus(neighbor, -dx, -dy, 0), world);
            walkable(plus(neighbor, -dx, 0, 0), world);
            walkable(plus(neighbor, 0, -dy, 0), world);
            // ||
            walkable()
            if ((graph.isWalkable(neighbor.x - dx, neighbor.y + dy)
                    && !graph.isWalkable(neighbor.x - dx, neighbor.y))
                    || (graph.isWalkable(neighbor.x + dx, neighbor.y - dy)
                    && !graph.isWalkable(neighbor.x, neighbor.y - dy))) {
                return neighbor;
            }
            // when moving diagonally, must check for vertical/horizontal jump points
            if (jump(graph.getNode(neighbor.x + dx, neighbor.y), neighbor, goals) != null ||
                    jump(graph.getNode(neighbor.x, neighbor.y + dy), neighbor, goals) != null) {
                return neighbor;
            }
        } else { // check horizontally/vertically
            if (dx != 0) {
                if ((graph.isWalkable(neighbor.x + dx, neighbor.y + 1) && !graph.isWalkable(neighbor.x, neighbor.y + 1)) ||
                        (graph.isWalkable(neighbor.x + dx, neighbor.y - 1) && !graph.isWalkable(neighbor.x, neighbor.y - 1))) {
                    return neighbor;
                }
            } else {
                if ((graph.isWalkable(neighbor.x + 1, neighbor.y + dy) && !graph.isWalkable(neighbor.x + 1, neighbor.y)) ||
                        (graph.isWalkable(neighbor.x - 1, neighbor.y + dy) && !graph.isWalkable(neighbor.x - 1, neighbor.y))) {
                    return neighbor;
                }
            }
        }

        // jump diagonally towards our goal
        return jump(graph.getNode(neighbor.x + dx, neighbor.y + dy), neighbor, goals);

         */
        return null;
    }
}
