package com.ericlam.mc.visiblepathfinder;

import com.ericlam.mc.visiblepathfinder.api.DistanceScorer;
import com.ericlam.mc.visiblepathfinder.config.VPFConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.List;

public final class MCMechanism {

    private final List<Material> BLOCKS_CAN_CLIMB;
    private final List<Material> DAMAGEABLE;

    @Inject
    private Debugger debugger;

    @Inject
    public MCMechanism(VPFConfig config) {
        BLOCKS_CAN_CLIMB = config.can_climb;
        DAMAGEABLE = config.damageable;
    }

    public boolean isWalkable(Vector vector, World world){
        var topV = vector.clone().add(new Vector(0, 1, 0));
        // 確保有兩格空間能通過
        return isPassable(topV, world) && isPassable(vector, world);
    }

    public boolean isPassable(Vector vector, World world) {
        var block = vector.toLocation(world).getBlock();
        if (BLOCKS_CAN_CLIMB.contains(block.getType())) { // 可以爬行的方塊，則視為可以通過
            return true;
        }
        // 水是可爬行方塊，但需要做一些過濾
        if (block.getType() == Material.WATER) {
            // 檢查水是否流動
            var leveled = (Levelled) block.getState().getBlockData();
            // level > 0 為水流動方塊
            if (leveled.getLevel() > 0) {
                return true;
            } else { // 若果為固定水，則確保上方再無任何液體方塊
                var topBlock = vector.clone().add(new Vector(0, 1, 0)).toLocation(world).getBlock();
                return !topBlock.isLiquid();
            }
        }
        if (isWoodDoor(block.getType())) { // 木門可以通過
            return true;
        }
        if (DAMAGEABLE.contains(block.getType())) { // 有傷害，不能通過
            return false;
        }
        return block.isPassable();
    }

    public boolean isOnGround(Vector vector, World world) {
        var current = vector.clone().add(new Vector(0, -1, 0));
        var block = current.toLocation(world).getBlock();
        // 有傷害，不能通過
        if (DAMAGEABLE.contains(block.getType())) {
            return false;
        }
        // 水面上可以
        if (block.getType() == Material.WATER) {
            return true;
        }
        // 底部方塊為固定 或 底部方塊為可爬行
        return (block.isSolid() && !block.isPassable()) || BLOCKS_CAN_CLIMB.contains(block.getType());
    }


    private boolean isWoodDoor(Material type) {
        return type != Material.IRON_DOOR && type != Material.IRON_TRAPDOOR && type.toString().endsWith("DOOR");
    }

    public void sendProgress(Player player, double totalCost, double currentCost) {
        if (player == null) return;
        var h = Math.max(0, (totalCost - currentCost) / totalCost) * 100;
        player.sendActionBar(Component.text(MessageFormat.format("§e進度: {0}%", Math.round(h))));
    }

    private boolean isStair(Material type) {
        return type.toString().endsWith("STAIRS");
    }
}
