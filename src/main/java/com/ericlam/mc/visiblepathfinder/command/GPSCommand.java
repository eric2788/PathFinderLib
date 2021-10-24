package com.ericlam.mc.visiblepathfinder.command;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import org.bukkit.command.CommandSender;

@Commander(
        name = "gps",
        description = "GPS 導航指令"
)
public class GPSCommand implements CommandNode {
    @Override
    public void execute(CommandSender sender) {
    }
}
