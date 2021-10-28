package com.ericlam.mc.pathfinderlib.api.searcher;

import com.ericlam.mc.eld.services.ScheduleService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public interface PathSearcher {

    ScheduleService.BukkitPromise<List<Vector>> findPathAsync(Location from, Location to, Player player);

    ScheduleService.BukkitPromise<List<Vector>> findPathAsync(Location from, Location to, World world);

    List<Vector> findPath(Location from, Location to, Player player);

    List<Vector> findPath(Location from, Location to, World world);

}
