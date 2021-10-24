package com.ericlam.mc.visiblepathfinder;

import com.ericlam.mc.visiblepathfinder.config.VPFConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import javax.inject.Inject;
import java.util.List;

public final class MCMechanism {

    private final List<Material> BLOCKS_CAN_CLIMB;
    private final List<Material> DAMAGEABLE;

    @Inject
    public MCMechanism(VPFConfig config) {
        BLOCKS_CAN_CLIMB = config.can_climb;
        DAMAGEABLE = config.damageable;
    }

    public boolean isPassable(Vector vector, World world) {
        var block = vector.toLocation(world).getBlock();
        if (BLOCKS_CAN_CLIMB.contains(block.getType())) { // 可以爬行的方塊，則視為可以通過
            return true;
        }
        if (isWoodDoor(block.getType())){ // 木門可以通過
            return true;
        }
        if (DAMAGEABLE.contains(block.getType())){ // 有傷害，不能通過
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
        if (block.getType() == Material.WATER){
            return true;
        }
        // 底部方塊為固定 或 底部方塊為可爬行
        return block.isSolid() || BLOCKS_CAN_CLIMB.contains(block.getType());
    }


    private boolean isWoodDoor(Material type){
        return type != Material.IRON_DOOR && type != Material.IRON_TRAPDOOR && type.toString().endsWith("DOOR");
    }

    private boolean isStair(Material type){
        return type.toString().endsWith("STAIRS");
    }
}