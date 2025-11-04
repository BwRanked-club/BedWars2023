package com.tomkeuper.bedwars.configuration;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.configuration.ConfigManager;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class ArenaConfig extends ConfigManager {

    public ArenaConfig(Plugin plugin, String name, String dir) {
        super(plugin, name, dir);

        YamlConfiguration yml = getYml();
        yml.options().header(plugin.getDescription().getName() + " arena configuration file.\n" +
                "Documentation here: https://wiki.tomkeuper.com/docs/BedWars2023/configuration/Arena-Configuration\n");
        yml.addDefault("group", "Default");
        yml.addDefault(ConfigPath.ARENA_DISPLAY_NAME, "");
        yml.addDefault("minPlayers", 4);
        yml.addDefault("maxInTeam", 1);
        yml.addDefault("allowSpectate", true);
        yml.addDefault(ConfigPath.ARENA_SPAWN_PROTECTION, 5);
        yml.addDefault(ConfigPath.ARENA_SHOP_PROTECTION, 1);
        yml.addDefault(ConfigPath.ARENA_UPGRADES_PROTECTION, 1);
        yml.addDefault(ConfigPath.ARENA_GENERATOR_PROTECTION, 1);
        yml.addDefault(ConfigPath.ARENA_GENERATOR_SPLIT_RANGE, 2.0);
        yml.addDefault(ConfigPath.ARENA_ISLAND_RADIUS, 17);
        yml.addDefault("worldBorder", 500);
        yml.addDefault(ConfigPath.ARENA_Y_LEVEL_KILL, 0);
        yml.addDefault(ConfigPath.ARENA_CONFIGURATION_MAX_BUILD_Y, 110);
        yml.addDefault(ConfigPath.ARENA_CONFIGURATION_MIN_BUILD_Y, 60);
        yml.addDefault(ConfigPath.ARENA_DISABLE_GENERATOR_FOR_EMPTY_TEAMS, false);
        yml.addDefault(ConfigPath.ARENA_DISABLE_NPCS_FOR_EMPTY_TEAMS, false);
        yml.addDefault(ConfigPath.ARENA_NORMAL_DEATH_DROPS, false);
        yml.addDefault(ConfigPath.ARENA_USE_BED_HOLO, false);
        yml.addDefault(ConfigPath.ARENA_ALLOW_MAP_BREAK, false);
        yml.addDefault(ConfigPath.ARENA_ALLOW_DRAGON_DESTROY_WHEN_PROTECTED, true);
        yml.addDefault(ConfigPath.ARENA_MAGIC_MILK_TIME, 30);
        ArrayList<String> rules = new ArrayList<>();
        rules.add("doDaylightCycle:false");
        rules.add("announceAdvancements:false");
        rules.add("doInsomnia:false");
        rules.add("doImmediateRespawn:true");
        rules.add("doWeatherCycle:false");
        rules.add("doFireTick:false");
        if (BedWars.nms.getVersion() >= 14) {
            rules.add("locatorBar:false"); // Only apply for 1.21.6 and above
        }
        yml.addDefault(ConfigPath.ARENA_GAME_RULES, rules);
        yml.options().copyDefaults(true);
        save();
    }
}
