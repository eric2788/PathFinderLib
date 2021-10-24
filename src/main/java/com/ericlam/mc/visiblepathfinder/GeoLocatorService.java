package com.ericlam.mc.visiblepathfinder;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class GeoLocatorService {

    public static class GPS {

        public Location start;

        public Location destination;

    }

    private final Map<Player, GPS> locatorCache = new ConcurrentHashMap<>();

    public void setStart(Player player, Location location) {
        locatorCache.putIfAbsent(player, new GPS());
        locatorCache.get(player).start = location;
    }

    public void setDestination(Player player, Location location) {
        locatorCache.putIfAbsent(player, new GPS());
        locatorCache.get(player).destination = location;
    }

    public boolean hasStart(Player player){
        return getLocatorOptional(player).map(gps -> gps.start != null).orElse(false);
    }

    public boolean hasDestination(Player player){
        return getLocatorOptional(player).map(gps -> gps.destination != null).orElse(false);
    }

    public Optional<GPS> getLocatorOptional(Player player) {
        return Optional.ofNullable(getLocator(player));
    }

    public GPS getLocator(Player player){
        return locatorCache.get(player);
    }

    public void clearLocator(Player player) {
        locatorCache.remove(player);
    }
}
