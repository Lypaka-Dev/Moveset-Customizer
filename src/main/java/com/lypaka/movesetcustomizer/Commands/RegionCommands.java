package com.lypaka.movesetcustomizer.Commands;

import com.lypaka.movesetcustomizer.Config.ConfigManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegionCommands {

    private static ConfigurationNode config = ConfigManager.getConfigFile(1);
    private static ConfigurationLoader<CommentedConfigurationNode> loader = ConfigManager.getConfigLoad(1);
    private static String world;
    private static Map<String, String> map = new HashMap<String, String>() {};

    public static CommandSpec getRegionCommands() {

        CommandSpec create = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("region")),
                        GenericArguments.integer(Text.of("maxX")),
                        GenericArguments.integer(Text.of("maxY")),
                        GenericArguments.integer(Text.of("maxZ")),
                        GenericArguments.integer(Text.of("minX")),
                        GenericArguments.integer(Text.of("minY")),
                        GenericArguments.integer(Text.of("minZ")),
                        GenericArguments.optional(GenericArguments.string(Text.of("world")))
                )
                .executor((sender, context) -> {

                    Player player = (Player) sender;
                    String region = context.getOne("region").get().toString();
                    int maxX = (int) context.getOne("maxX").get();
                    int maxY = (int) context.getOne("maxY").get();
                    int maxZ = (int) context.getOne("maxZ").get();
                    int minX = (int) context.getOne("minX").get();
                    int minY = (int) context.getOne("minY").get();
                    int minZ = (int) context.getOne("minZ").get();
                    RegionCommands.world = (String) context.getOne("world").orElse(Sponge.getServer().getPlayer(player.getName()).get().getWorld().getName());

                    if (config.getNode("Regions", region).isVirtual()) {
                        map.put("Max X", String.valueOf(maxX));
                        map.put("Max Y", String.valueOf(maxY));
                        map.put("Max Z", String.valueOf(maxZ));
                        map.put("Min X", String.valueOf(minX));
                        map.put("Min Y", String.valueOf(minY));
                        map.put("Min Z", String.valueOf(minZ));
                        map.put("World", RegionCommands.world);
                        config.getNode("Regions", region).setValue(map);
                        try {
                            loader.save(config);
                            player.sendMessage(Text.of("Successfully created the " + region + " region!"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        player.sendMessage(Text.of(TextColors.RED, "A region with that name is already defined in the config!"));
                    }

                    return CommandResult.success();

                })
                .build();

        CommandSpec delete = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("region"))
                )
                .executor((sender, context) -> {

                    Player player = (Player) sender;
                    String region = context.getOne("region").get().toString();

                    if (!config.getNode("Regions", region).isVirtual()) {
                        config.getNode("Regions", region).setValue(null);
                        try {
                            loader.save(config);
                            player.sendMessage(Text.of("Successfully deleted the " + region + " region!"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        player.sendMessage(Text.of(TextColors.RED, "A region with that name does not exist in the config!"));
                    }

                    return CommandResult.success();

                })
                .build();


        CommandSpec main = CommandSpec.builder()
                .child(create, "create")
                .child(delete, "delete")
                .permission("movesets.region.admin")
                .executor((sender, context) -> {

                    Player player = (Player) sender;
                    return CommandResult.success();

                })
                .build();

        return main;
    }


}
