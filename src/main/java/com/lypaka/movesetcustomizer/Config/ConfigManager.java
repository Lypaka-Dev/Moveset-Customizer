package com.lypaka.movesetcustomizer.Config;

import com.lypaka.movesetcustomizer.MovesetCustomizer;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Loads and stores all the configuration settings.
 * It loads from file on server start up. or when a player reloads the plugin.
 *
 * @uathor landonjw
 * @since 9/25/2019 - Version 1.0.0
 */
public class ConfigManager {

    /** Name of the file to grab configuration settings from. */
    private static final String[] FILE_NAMES = {"movesets.conf", "regions.conf"};
    /** Paths needed to locate the configuration file. */
    private static Path dir;
    private static Path[] config = new Path[FILE_NAMES.length];
    /** Loader for the configuration file. */
    private static ArrayList<ConfigurationLoader<CommentedConfigurationNode>> configLoad = new ArrayList<ConfigurationLoader<CommentedConfigurationNode>>(FILE_NAMES.length);
    /** Storage for all the configuration settings. */
    private static CommentedConfigurationNode[] configNode = new CommentedConfigurationNode[FILE_NAMES.length];

    /**
     * Locates the configuration file and loads it.
     * @param folder Folder where the configuration file is located.
     */
    public static void setup(Path folder){
        dir = folder;

        for (int i = 0; i < FILE_NAMES.length; i++) {
            config[i] = dir.resolve(FILE_NAMES[i]);
        }
        load();
    }

    /**
     * Loads the configuration settings into storage.
     */
    public static void load(){
        //Create directory if it doesn't exist.
        try{
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }

            for (int i = 0; i < FILE_NAMES.length; i++) {
                //Create or locate file and load configuration file into storage.
                MovesetCustomizer.getContainer().getAsset(FILE_NAMES[i]).get().copyToFile(config[i], false, true);

                ConfigurationLoader<CommentedConfigurationNode> tempConfigLoad = HoconConfigurationLoader.builder().setPath(config[i]).build();

                configLoad.add(i, tempConfigLoad);
                configNode[i] = tempConfigLoad.load();
            }

        } catch (IOException e){
            MovesetCustomizer.getLogger().error("MovesetCustomizer configuration could not load.");
            e.printStackTrace();
        }
    }



    /**
     * Saves the configuration settings to configuration file.
     */
    public static void save(){
        Task.builder().execute(() -> {
            for (int i = 0; i < FILE_NAMES.length; i++) {
                try{
                    configLoad.get(i).save(configNode[i]);
                }
                catch(IOException e){
                    MovesetCustomizer.getLogger().error("MovesetCustomizer could not save configuration.");
                    e.printStackTrace();
                }
            }
        }).async().submit(MovesetCustomizer.instance);
    }


    /**
     * Gets the configuration loader ArrayList.
     * @return The configuration loader ArrayList.
     */
    public static ArrayList<ConfigurationLoader<CommentedConfigurationNode>> getConfigLoad(){
        return configLoad;
    }

    /**
     * Gets the configuration loader at specific index.
     * @param index Index of the configuration loader.
     * @return The configuration loader.
     */
    public static ConfigurationLoader<CommentedConfigurationNode> getConfigLoad(int index){
        return configLoad.get(index);
    }


    /**
     * Gets a node from the configuration node, where all configuration settings are stored.
     * @param index Index of the config.
     * @param node A node within the configuration node.
     * @return A node within the configuration node.
     */
    public static CommentedConfigurationNode getConfigNode(int index, Object... node){
        return configNode[index].getNode(node);
    }


    public static CommentedConfigurationNode getConfigFile (int index) {
        return configNode[index];
    }



}