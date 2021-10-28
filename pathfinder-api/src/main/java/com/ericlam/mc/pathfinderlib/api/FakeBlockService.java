package com.ericlam.mc.pathfinderlib.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface FakeBlockService {

    void showFakeBlock(Location location, Player player, Material material);

    void clearAllFakeBlock(Player player);
}
