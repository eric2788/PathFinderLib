package com.ericlam.mc.pathfinderlib.command;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.WrappedParticle;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.eld.services.ScheduleService;
import com.ericlam.mc.pathfinderlib.PathFinderLib;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.text.MessageFormat;

@Commander(
        name = "test",
        description = "異步測試封包指令",
        playerOnly = true
)
public class GPSTestCommand implements CommandNode {


    @Inject
    private ScheduleService scheduleService;
    @Inject
    private PathFinderLib plugin;


    @Override
    public void execute(CommandSender sender) {
        var player = (Player) sender;
        player.sendMessage("正在測試異步發送封包....");
        tryRun("異步發送方塊封包", player, () -> player.sendBlockChange(player.getLocation(), Material.BEACON.createBlockData()));
        tryRun("異步發送粒子封包", player, () -> {
            WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();
            packet.setParticleType(WrappedParticle.create(Particle.FLAME, null));
            packet.setX(player.getLocation().getX());
            packet.setY(player.getLocation().getY() + 1);
            packet.setZ(player.getLocation().getZ());
            packet.setNumberOfParticles(8);
            packet.sendPacket(player);
        });
        tryRun("異步發送ActionBar", player, () -> player.sendActionBar(TextComponent.fromLegacyText("§aHello World!")));
    }


    private void tryRun(String title, Player player, Runnable runnable) {
        player.sendMessage("正在測試: " + title);
        scheduleService
                .runAsync(plugin, runnable)
                .thenRunSync(v -> player.sendMessage(MessageFormat.format("{0} 測試完成，成功在異步運行。", title)))
                .joinWithCatch(ex -> player.sendMessage(MessageFormat.format("測試 {0} 時出現錯誤: {1}", title, ex.getMessage())));
    }
}
