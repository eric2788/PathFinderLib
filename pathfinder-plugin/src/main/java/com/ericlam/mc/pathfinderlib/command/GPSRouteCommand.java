package com.ericlam.mc.pathfinderlib.command;

import com.comphenix.protocol.wrappers.WrappedParticle;
import com.ericlam.mc.eld.annotations.CommandArg;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.pathfinderlib.GeoLocatorService;
import com.ericlam.mc.pathfinderlib.api.DynamicPathSearcherService;
import com.ericlam.mc.pathfinderlib.manager.FakeBlockManager;
import com.ericlam.mc.pathfinderlib.manager.FakeParticleManager;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.text.MessageFormat;

@Commander(
        name = "route",
        description = "啟動動態導航"
)
public class GPSRouteCommand implements CommandNode {

    @Inject
    private DynamicPathSearcherService searcherService;
    @Inject
    private GeoLocatorService locatorService;
    @Inject
    private FakeParticleManager particleManager;
    @Inject
    private FakeBlockManager blockManager;

    @CommandArg(order = 1, labels = "搜索演算法", optional = true)
    private final String searchAlgorithm = "dstar";
    @CommandArg(order = 2, labels = "距離演算法", optional = true)
    private final String distanceAlgorithm = "chebyshev";
    @CommandArg(order = 3, labels = "權重", optional = true)
    private final int weight = 15;
    @CommandArg(order = 4, labels = "誤差半徑", optional = true)
    private final int range = 3;
    @CommandArg(order = 5, labels = "檢測間隔(ticks)", optional = true)
    private final long perCheck = 20L;

    @Override
    public void execute(CommandSender sender) {
        var player = (Player) sender;


        if (!locatorService.hasDestination(player)) {
            player.sendMessage("§c缺少目的地");
            return;
        }

        var gps = locatorService.getLocator(player);

        if (gps.destination.getWorld() != player.getWorld()) {
            player.sendMessage("§c紀錄地點並非目前的世界");
            return;
        }

        // 因為目前沒有動態目標，因此以靜態目標作為測試
        var destination = gps.destination;

        World world = player.getWorld();

        player.sendMessage("§a開始導航....");

        var pathSearcher = searcherService
                .buildPathSearcher(searchAlgorithm)
                .setDistanceAlgorithm(distanceAlgorithm)
                .setWeight(weight)
                .checkingRadius(range)
                .intervalPerCheck(perCheck)
                .build();


        pathSearcher.setOnUpdated(paths -> {

            blockManager.clearAllFakeBlock(player);

            if (paths.isEmpty()) {
                player.sendMessage("§c找不到路徑");
                return;
            }

            particleManager.clearParticle(player);

            for (var v : paths) {
                var loc = v.toLocation(world);
                //blockManager.showFakeBlock(loc, player, Material.TORCH);
                particleManager.spawnFixedParticles(player, loc, WrappedParticle.create(Particle.CLOUD, null), 8);
            }

            player.sendMessage("§a導航已更新。");

        });

        pathSearcher.setOnReachedTarget(en -> {

            player.sendMessage("§a導航已完成。");
            player.sendMessage(MessageFormat.format(
                    "§e搜索演算法: {0}\n" +
                            "§b距離演算法: {1}\n" +
                            "§c權重: {2}\n",
                    searchAlgorithm, distanceAlgorithm, weight));

        });

        pathSearcher.findPathAsync(player, destination, player)
                .joinWithCatch(ex -> {
                    player.sendMessage("§c搜索路徑時出現錯誤: " + ex.getMessage());
                    ex.printStackTrace();
                });
    }
}
