package com.ericlam.mc.visiblepathfinder.api;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;

public interface PathSearcher {

    List<Vector> search(Vector from, Vector to, World world, @Nullable Player player);

}