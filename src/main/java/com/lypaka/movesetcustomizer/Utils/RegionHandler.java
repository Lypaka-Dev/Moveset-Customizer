package com.lypaka.movesetcustomizer.Utils;

import com.google.common.reflect.TypeToken;
import com.lypaka.movesetcustomizer.Config.ConfigManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.Map;

public class RegionHandler {

    private static ConfigurationNode config = ConfigManager.getConfigFile(1);
    private static ConfigurationLoader<CommentedConfigurationNode> loader = ConfigManager.getConfigLoad(1);


    public static boolean isInRegion (Player player) throws ObjectMappingException {
        Map<String, Map<String, String>> map = config.getNode("Regions").getValue(new TypeToken<Map<String, Map<String, String>>>() {});
        Object[] list =  map.keySet().toArray();
        for (int i = 0; i <= map.size() - 1; i++) {
            if (player.getLocation().getBlockX() <= Integer.parseInt(map.get(list[i]).get("Max X")) &&
                    player.getLocation().getBlockY() <= Integer.parseInt(map.get(list[i]).get("Max Y")) &&
                    player.getLocation().getBlockZ() <= Integer.parseInt(map.get(list[i]).get("Max Z")) &&
                    player.getLocation().getBlockX() >= Integer.parseInt(map.get(list[i]).get("Min X")) &&
                    player.getLocation().getBlockY() >= Integer.parseInt(map.get(list[i]).get("Min Y")) &&
                    player.getLocation().getBlockZ() >= Integer.parseInt(map.get(list[i]).get("Min Z")) &&
                    player.getWorld().getName().equals(map.get(list[i].toString()).get("World"))) {
                return true;
            }
        }
        return false;
    }

    public static String getRegionName (Player player) throws ObjectMappingException {
        Map<String, Map<String, String>> map = config.getNode("Regions").getValue(new TypeToken<Map<String, Map<String, String>>>() {});
        Object[] list = map.keySet().toArray();
        for (int i = 0; i <= map.size() - 1; i++) {
            if (player.getLocation().getBlockX() <= Integer.parseInt(map.get(list[i]).get("Max X")) &&
                    player.getLocation().getBlockY() <= Integer.parseInt(map.get(list[i]).get("Max Y")) &&
                    player.getLocation().getBlockZ() <= Integer.parseInt(map.get(list[i]).get("Max Z")) &&
                    player.getLocation().getBlockX() >= Integer.parseInt(map.get(list[i]).get("Min X")) &&
                    player.getLocation().getBlockY() >= Integer.parseInt(map.get(list[i]).get("Min Y")) &&
                    player.getLocation().getBlockZ() >= Integer.parseInt(map.get(list[i]).get("Min Z")) &&
                    player.getWorld().getName().equals(map.get(list[i].toString()).get("World"))) {
                return list[i].toString();
            }
        }
        return "none";
    }

    public static boolean doesRegionExist (String name) throws ObjectMappingException {
        Map<String, List<String>> map = config.getNode("Regions").getValue(new TypeToken<Map<String, List<String>>>() {});
        Object[] list = map.keySet().toArray();
        for (int i = 0; i <= list.length - 1; i++) {
            if (name.equals(list[i].toString())) {
                return true;
            }
        }
        return false;
    }
}
