package com.ericlam.mc.visiblepathfinder.config;

import com.ericlam.mc.eld.annotations.Resource;
import com.ericlam.mc.eld.components.Configuration;
import org.bukkit.Material;

import java.util.List;

@Resource(locate = "config.yml")
public class VPFConfig extends Configuration {

    public boolean debug = false;

    public DefaultSettings default_settings;
    public DynamicSettings dynamic_settings;

    public ParticleSettings particle_settings;

    public List<Material> can_climb;

    public List<Material> damageable;

    public static class DefaultSettings {
        public String algorithm = "astar";
        public String distance_calculator = "manhattan";
        public int weight = 15;

    }

    public static class ParticleSettings {

        public int loop_per_tick = 5;
        public long interval_per_loop = 2L;
        public long interval_per_trace = 20L;

    }

    public static class DynamicSettings {
        public String algorithm = "dstar";
        public long ticks_per_check = 20L;
        public int max_accepted_radius = 2;
    }

}
