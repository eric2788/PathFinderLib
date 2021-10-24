package com.ericlam.mc.visiblepathfinder.config;

import com.ericlam.mc.eld.annotations.Resource;
import com.ericlam.mc.eld.components.Configuration;
import org.bukkit.Material;

import java.util.List;

@Resource(locate = "config.yml")
public class VPFConfig extends Configuration {

    public boolean debug = false;

    public DefaultSettings default_settings;

    public List<Material> can_climb;

    public List<Material> damageable;

    public static class DefaultSettings {
        public String algorithm = "astar";
        public String distance_calculator = "manhattan";
        public int weight = 15;

    }

}
