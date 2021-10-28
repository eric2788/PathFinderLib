package com.ericlam.mc.pathfinderlib.api.searcher;

import com.ericlam.mc.eld.services.ScheduleService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.util.List;

public interface DynamicPathSearcher {

    ScheduleService.BukkitPromise<Void> findPathAsync(Entity from, Entity to, Player player);

    ScheduleService.BukkitPromise<Void> findPathAsync(Entity from, Location to, Player player);

    ScheduleService.BukkitPromise<Void> findPathAsync(Entity from, Entity to, World world);

    ScheduleService.BukkitPromise<Void> findPathAsync(Entity from, Location to, World world);

    void findPath(Entity from, Entity to, Player player);

    void findPath(Entity from, Location to, World world);

    void findPath(Entity from, Location to, Player player);

    void findPath(Entity from, Entity to, World world);

    void setOnUpdated(Consumer<List<Vector>> route);

    void setOnReachedTarget(Consumer<Entity> onReachedTarget);

}
