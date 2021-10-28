package com.ericlam.mc.pathfinderlib.command;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.pathfinderlib.config.PathLibConfig;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;

@Commander(
        name = "debug",
        description = "切換 debug 模式 (不影響 config)"
)
public class GPSDebugCommand implements CommandNode {

    @Inject
    private PathLibConfig config;

    @Override
    public void execute(CommandSender sender) {
        config.debug = !config.debug;
        sender.sendMessage("§a已切換debug模式為: " + config.debug);
    }
}
