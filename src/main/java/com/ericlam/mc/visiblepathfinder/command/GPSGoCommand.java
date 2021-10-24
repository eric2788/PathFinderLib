package com.ericlam.mc.visiblepathfinder.command;

import com.comphenix.protocol.wrappers.WrappedParticle;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.visiblepathfinder.Debugger;
import com.ericlam.mc.visiblepathfinder.GeoLocatorService;
import com.ericlam.mc.visiblepathfinder.FakeParticleManager;
import com.ericlam.mc.visiblepathfinder.api.PathFinderService;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;

@Commander(
        name = "go",
        description = "開始導航",
        playerOnly = true
)
public class GPSGoCommand implements CommandNode {

    @Inject
    private GeoLocatorService locatorService;
    @Inject
    private PathFinderService pathFinderService;

    @Inject
    private FakeParticleManager particleManager;

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

        pathFinderService.findPathAsync(gps.start, gps.destination, player)
                .thenRunSync(paths -> {
                    if (paths.isEmpty()){
                        player.sendMessage("§c找不到路徑");
                        return;
                    }
                    particleManager.clearParticle(player);
                    for (var v : paths) {
                        var loc = v.toLocation(world);
                        //debugger.showFakeBlock(loc, player, Material.TORCH);
                        particleManager.spawnFixedParticles(player, loc, WrappedParticle.create(Particle.CLOUD, null), 8);
                    }
                    locatorService.clearLocator(player);
                    player.sendMessage("§a導航完成, 並已清空先前的地點設置記錄。");
                })
                .joinWithCatch(ex -> {
                    player.sendMessage("§c搜索路徑時出現錯誤: "+ex.getMessage());
                    ex.printStackTrace();
                });

    }
}
