package com.ericlam.mc.visiblepathfinder.command;

import com.comphenix.protocol.wrappers.WrappedParticle;
import com.ericlam.mc.eld.annotations.CommandArg;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.visiblepathfinder.manager.FakeBlockManager;
import com.ericlam.mc.visiblepathfinder.GeoLocatorService;
import com.ericlam.mc.visiblepathfinder.manager.FakeParticleManager;
import com.ericlam.mc.visiblepathfinder.api.PathSearcherService;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.text.MessageFormat;

@Commander(
        name = "go",
        description = "開始導航",
        playerOnly = true
)
public class GPSGoCommand implements CommandNode {

    @Inject
    private GeoLocatorService locatorService;
    @Inject
    private PathSearcherService pathFinderService;

    @Inject
    private FakeParticleManager particleManager;
    @Inject
    private FakeBlockManager blockManager;

    @CommandArg(order = 1, labels = "搜索演算法", optional = true)
    private String searchAlgorithm = "astar";
    @CommandArg(order = 2, labels = "距離演算法", optional = true)
    private String distanceAlgorithm = "chebyshev";
    @CommandArg(order = 3, labels = "權重", optional = true)
    private int weight = 15;

    @Override
    public void execute(CommandSender sender) {

        var player = (Player) sender;

        if (!locatorService.hasStart(player)){
            player.sendMessage("§c缺少開始地");
            return;
        }

        if (!locatorService.hasDestination(player)){
            player.sendMessage("§c缺少目的地");
            return;
        }

        GeoLocatorService.GPS gps = locatorService.getLocator(player);

        if (gps.start.getWorld() != gps.destination.getWorld()){
            player.sendMessage("§c兩者世界不一樣");
            return;
        }

        if (gps.start.getWorld() != player.getWorld()){
            player.sendMessage("§c紀錄地點並非目前的世界");
            return;
        }

        World world = player.getWorld();

        player.sendMessage("§a開始導航....");

        if (!pathFinderService.hasPathSearcher(searchAlgorithm)){
            player.sendMessage("§c未知演算法: "+searchAlgorithm);
            return;
        }

        var pathSearcher = pathFinderService
                .buildPathSearcher(searchAlgorithm)
                .setDistanceAlgorithm(distanceAlgorithm)
                .setWeight(weight)
                .build();

        long prev = System.currentTimeMillis();
        pathSearcher.findPathAsync(gps.start, gps.destination, player)
                .thenRunSync(paths -> {

                    blockManager.clearAllFakeBlock(player);

                    if (paths.isEmpty()){
                        player.sendMessage("§c找不到路徑");
                        return;
                    }
                    long now = System.currentTimeMillis();
                    particleManager.clearParticle(player);
                    for (var v : paths) {
                        var loc = v.toLocation(world);
                        //blockManager.showFakeBlock(loc, player, Material.TORCH);
                        particleManager.spawnFixedParticles(player, loc, WrappedParticle.create(Particle.CLOUD, null), 8);
                    }
                    player.sendMessage("§a導航完成。");
                    player.sendMessage(MessageFormat.format(
                            "§e搜索演算法: {0}\n" +
                                    "§b距離演算法: {1}\n" +
                                    "§c權重: {2}\n" +
                                    "§a導航花費時間: {3} 毫秒",
                            searchAlgorithm, distanceAlgorithm, weight, now - prev));
                })
                .joinWithCatch(ex -> {
                    player.sendMessage("§c搜索路徑時出現錯誤: "+ex.getMessage());
                    ex.printStackTrace();
                });

    }
}
