package com.ericlam.mc.pathfinderlib.command;

import com.ericlam.mc.eld.annotations.CommandArg;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.pathfinderlib.GeoLocatorService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Locale;


@Commander(
        name = "locate",
        description = "設置導航地點",
        playerOnly = true
)
public class GPSLocateCommand implements CommandNode {

    @Inject
    private GeoLocatorService geoLocatorService;

    @CommandArg(order = 1, labels = {"start / end"})
    private String pos;

    @Override
    public void execute(CommandSender sender) {
        var player = (Player) sender;

        if (pos.toLowerCase(Locale.ROOT).equals("start")) {
            geoLocatorService.setStart(player, player.getLocation());
        } else if (pos.toLowerCase(Locale.ROOT).equals("end")) {
            geoLocatorService.setDestination(player, player.getLocation());
        } else {
            sender.sendMessage(MessageFormat.format("§c未知位置 {0}", pos));
            return;
        }

        sender.sendMessage(MessageFormat.format("§a設置 {0} 成功。", pos));
    }
}
