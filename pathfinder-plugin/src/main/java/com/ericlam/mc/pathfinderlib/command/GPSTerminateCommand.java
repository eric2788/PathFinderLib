package com.ericlam.mc.pathfinderlib.command;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.pathfinderlib.api.PathSearcherService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;

@Commander(
        name = "terminate",
        description = "中止現有導航",
        playerOnly = true
)
public class GPSTerminateCommand implements CommandNode {

    @Inject
    private PathSearcherService searcherService;

    @Override
    public void execute(CommandSender sender) {
        var player = (Player) sender;
        boolean re = searcherService.terminateSearch(player);
        if (re) {
            player.sendMessage("§a已終止現有導航。");
        } else {
            player.sendMessage("§c沒有導航正在運行。");
        }
    }
}
