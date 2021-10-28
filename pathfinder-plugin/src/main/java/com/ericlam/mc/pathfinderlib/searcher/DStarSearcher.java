package com.ericlam.mc.pathfinderlib.searcher;

import com.ericlam.mc.pathfinderlib.Debugger;
import com.ericlam.mc.pathfinderlib.MCMechanism;
import com.ericlam.mc.pathfinderlib.PathFinderLib;
import com.ericlam.mc.pathfinderlib.api.DistanceScorer;
import com.ericlam.mc.pathfinderlib.api.algorithm.DynamicGraphSearchAlgorithm;
import com.ericlam.mc.pathfinderlib.manager.FakeParticleManager;
import com.google.inject.Injector;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DStarSearcher implements DynamicGraphSearchAlgorithm {

    protected AStarSearcher aStarSearcher;
    private Supplier<Vector> moverVector;
    private Supplier<Vector> targetVector;
    private Consumer<List<Vector>> onUpdated = o -> {
    };
    private Consumer<Entity> onReachedTarget = o -> {
    };
    private Entity targetEntity, movingEntity;
    @Inject
    private PathFinderLib plugin;
    @Inject
    private FakeParticleManager particleManager;
    @Inject
    private Debugger debugger;
    @Inject
    private MCMechanism mcMechanism;

    @Inject
    public DStarSearcher(Injector injector) {
        this.aStarSearcher = injector.getInstance(AStarSearcher.class);
    }

    @Override
    public void startRouting(Entity from, Entity to, World world, @Nullable Player observer, DistanceScorer scorer, int weight, int maxRange, long tickCheck) {

        // clear old data while reuse
        moverVector = () -> from.getLocation().toBlockLocation().toVector();
        targetVector = () -> to.getLocation().toBlockLocation().toVector();
        targetEntity = to;
        movingEntity = from;

        this.recalculation(world, observer, scorer, weight, maxRange, tickCheck);
    }

    @Override
    public void startRouting(Entity from, Vector to, World world, @Nullable Player observer, DistanceScorer scorer, int weight, int maxRange, long tickCheck) {

        // clear old data while reuse
        moverVector = () -> from.getLocation().toBlockLocation().toVector();
        targetVector = () -> to;
        targetEntity = from;
        movingEntity = from;

        this.recalculation(world, observer, scorer, weight, maxRange, tickCheck);
    }


    private void recalculation(World world, @Nullable Player player, DistanceScorer scorer, int weight, int maxRange, long tickCheck) {

        if (aStarSearcher.terminate) {
            debugger.log("搜索已被強制終止");
            return;
        }

        // reuse A* searching
        var result = aStarSearcher.search(moverVector.get(), targetVector.get(), world, player, scorer, weight);
        Bukkit.getScheduler().runTask(plugin, () -> this.onUpdated.accept(result)); // first routed path, run in sync
        if (result.isEmpty()) { // no path found, search terminated
            return;
        }
        TraceRunnable traceRunnable = new TraceRunnable(world, player, scorer, weight, result, maxRange, tickCheck);
        traceRunnable.runTaskTimerAsynchronously(plugin, 0L, tickCheck);
    }

    @Override
    public void setOnUpdatedPath(Consumer<List<Vector>> onUpdated) {
        this.onUpdated = onUpdated;
    }

    @Override
    public void setOnReachedTarget(Consumer<Entity> onReachedTarget) {
        this.onReachedTarget = onReachedTarget;
    }

    @Override
    public void terminate() {
        aStarSearcher.terminate();
    }

    private class TraceRunnable extends BukkitRunnable {

        private final World world;
        private final Player player;
        private final DistanceScorer scorer;
        private final int weight;
        private final int maxRange;
        private final List<Vector> calculatedPath;
        private final long intervalCheck;
        private boolean needRecalculate = false;

        private TraceRunnable(World world,
                              Player player,
                              DistanceScorer scorer,
                              int weight,
                              List<Vector> calculatedPath,
                              int maxRange,
                              long intervalCheck) {
            this.world = world;
            this.player = player;
            this.scorer = scorer;
            this.weight = weight;
            this.maxRange = maxRange;
            this.intervalCheck = intervalCheck;
            this.calculatedPath = new ArrayList<>(calculatedPath);
        }

        @Override
        public void run() {

            if (aStarSearcher.terminate) {
                debugger.log("搜索已被強制終止");
                cancel();
                return;
            }

            if (targetEntity.isDead() || movingEntity.isDead()) {
                debugger.log("搜索者或目標已死亡，中止搜索。");
                cancel();
                return;
            }
            if (targetEntity instanceof Player && !((Player) targetEntity).isOnline()) {
                debugger.log("搜索者沒有上線，已終止搜索");
                cancel();
                return;
            }
            if (movingEntity instanceof Player && !((Player) movingEntity).isOnline()) {
                debugger.log("目標沒有上線，已終止搜索");
                cancel();
                return;
            }


            var currentLoc = moverVector.get();
            var targetLoc = targetVector.get();

            if (aStarSearcher.terminate) {
                debugger.log("檢查誤差值被強制終止");
                cancel();
                return;
            }

            // 目標抵達終點，停止計算
            if (currentLoc.equals(targetLoc)) {
                debugger.log("目標已經抵達終點，停止計算");
                cancel();
                return;
            }

            // 目標位置
            var last = calculatedPath.get(calculatedPath.size() - 1);

            // 目標位置大於偏離值，則重新計算
            if (!inMaxRange(targetLoc, last)) {
                debugger.log("目標位置大於偏離值，需要重新計算");
                ensureWalkableBeforeRun();
                return;
            }

            // 尋找預測路徑中最緊鄰的位置
            // 首先計算與終點的距離(最長)
            double minimum = scorer.computeCost(currentLoc, last);
            Vector nextNode = null;
            Vector nearby = null;
            for (int i = 0; i < calculatedPath.size(); i++) {
                var path = calculatedPath.get(i);
                var cost = scorer.computeCost(currentLoc, path); // 計算目前與迭代節點的距離
                // 距離在偏離值內以及比上次記錄的小
                if (cost <= maxRange && cost < minimum) {
                    minimum = cost;
                    nearby = path;
                    // 如有，獲取下次即將迭代的節點
                    if (i < calculatedPath.size() - 1) {
                        nextNode = calculatedPath.get(i + 1);
                    } else {
                        nextNode = null;
                    }
                }
            }

            // 找不到鄰近的預測節點，判定為超出偏離值
            if (nearby == null) {
                debugger.log("目前位置大於預測路徑偏離值，需要重新計算");
                ensureWalkableBeforeRun();
                return;
            }

            // 若找不到下一節點
            if (nextNode == null) {
                debugger.log("找不到下一個節點，已略過");
                return;
            }

            // 下一個預定位置無法通過，則重新計算
            if (!mcMechanism.isWalkable(nextNode, world)) {
                debugger.log("下一個預定位置無法通過，需要重新計算");
                ensureWalkableBeforeRun();
            }
        }


        private void ensureWalkableBeforeRun() {
            var currentLoc = moverVector.get();
            var targetLoc = targetVector.get();
            // 只有位置有效時才能被重新計算
            if (mcMechanism.isWalkable(currentLoc, world) && mcMechanism.isWalkable(targetLoc, world)) {
                needRecalculate = true;
                cancel();
            }
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            if (needRecalculate) {
                debugger.log("重新計算中...");
                recalculation(world, player, scorer, weight, maxRange, intervalCheck);
            } else {
                debugger.log("已中止搜索");
                if (player != null) {
                    player.sendMessage("§a已抵達目的地");
                    particleManager.clearParticle(player); // clear particles for trace
                }
                Bukkit.getScheduler().runTask(plugin, () -> onReachedTarget.accept(targetEntity)); // run sync
            }
        }

        private boolean inMaxRange(Vector current, Vector predicted) {
            return scorer.computeCost(current, predicted) <= maxRange;
        }
    }
}
