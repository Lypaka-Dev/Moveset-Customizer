package com.lypaka.movesetcustomizer.Commands;

import com.google.common.reflect.TypeToken;
import com.lypaka.movesetcustomizer.Config.ConfigManager;
import com.lypaka.movesetcustomizer.Utils.RegionHandler;
import com.pixelmongenerations.api.def.MoveContainer;
import com.pixelmongenerations.api.pokemon.PokemonSpec;
import com.pixelmongenerations.common.battle.attacks.Attack;
import com.pixelmongenerations.common.battle.attacks.AttackBase;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.data.pokemon.PokemonRegistry;
import net.minecraft.world.World;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class MovesetCommands {

    private static ConfigurationNode config = ConfigManager.getConfigFile(0);
    private static ConfigurationLoader<CommentedConfigurationNode> loader = ConfigManager.getConfigLoad(0);
    private static AttackBase attack;
    private static EntityPixelmon pokemon;
    private static List<String> list;


    /**
     *
     * Creates commands for adding and removing moves from the config
     *
     * @return main CommandSpec of moveset commands
     */

    public static CommandSpec getMovesetCommands() {

        CommandSpec add = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("region")),
                        GenericArguments.string(Text.of("pokemon")),
                        GenericArguments.integer(Text.of("level")),
                        GenericArguments.remainingJoinedStrings(Text.of("move"))
                )
                .executor((sender, context) -> {

                    String region = context.getOne("region").get().toString();
                    Player player = (Player) sender;
                    int level = (int) context.getOne("level").get();
                    String move = context.getOne("move").get().toString().replace("[", "").replace("]", "");


                    // Checks Pokemon name and Attack name for valid entries
                    try {
                        MovesetCommands.pokemon = PokemonSpec.from(context.getOne("pokemon").get().toString()).create((World) player.getWorld());
                    } catch (NullPointerException ex) {
                        player.sendMessage(Text.of(TextColors.RED, "No Pokemon with that name exists!"));
                    }
                    try {
                        MovesetCommands.attack = Attack.getAttackBase(move).get();
                    } catch (NoSuchElementException ex) {
                        player.sendMessage(Text.of(TextColors.RED, "No such Attack exists!"));
                    }


                    // Checks if the Pokemon can learn the move (prevents Hydro Pump Bulbasaur, and the errors that would obviously cause)
                    MoveContainer moves = PokemonRegistry.getBaseFor(pokemon.getPokemonName()).moves;
                    if (!moves.tutorMoves.toString().contains(move) && !moves.tmTRMoves.toString().contains(move) && !moves.levelMoves.toString().contains(move) && !moves.evolutionMoves.toString().contains(move) && !moves.eggMoves.toString().contains(move)) {
                        player.sendMessage(Text.of(TextColors.RED, "This Pokemon cannot learn this move!"));


                    // Pokemon name, Attack name, and the Pokemon's ability to learn the move are all good to go
                    } else {
                        try {
                            if (RegionHandler.doesRegionExist(region)) {
                                if (config.getNode("Movesets", region, MovesetCommands.pokemon.getPokemonName(), String.valueOf(level)).isVirtual()) {
                                    list = new ArrayList<>();
                                    list.add(MovesetCommands.attack.getUnlocalizedName());
                                    config.getNode("Movesets", region, MovesetCommands.pokemon.getPokemonName(), String.valueOf(level)).setValue(list);
                                    loader.save(config);
                                    player.sendMessage(Text.of(TextColors.GREEN, "Successfully created a new move set and added " + MovesetCommands.pokemon.getPokemonName() + ", " + level + ", " + MovesetCommands.attack.getUnlocalizedName() + "!"));
                                } else {
                                    list = config.getNode("Movesets", region, MovesetCommands.pokemon.getPokemonName(), String.valueOf(level)).getList(TypeToken.of(String.class));
                                    list.add(MovesetCommands.attack.getUnlocalizedName());
                                    config.getNode("Movesets", region, MovesetCommands.pokemon.getPokemonName(), String.valueOf(level)).setValue(list);
                                    loader.save(config);
                                    player.sendMessage(Text.of(TextColors.GREEN, "Successfully added " + MovesetCommands.pokemon.getPokemonName() + ", " + level + ", " + MovesetCommands.attack.getUnlocalizedName() + "!"));
                                }

                            } else {
                                player.sendMessage(Text.of(TextColors.RED, "No region with this name exists!"));
                            }

                        } catch (ObjectMappingException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                    return CommandResult.success();
                })
                .build();

        CommandSpec deleteRegion = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("region"))
                )
                .executor((sender, context) -> {

                    Player player = (Player) sender;
                    String region = context.getOne("region").get().toString();

                    try {
                        if (RegionHandler.doesRegionExist(region)) {
                            config.getNode("Movesets", region).setValue(null);
                            loader.save(config);
                            player.sendMessage(Text.of(TextColors.GREEN, "Successfully deleted the entire " + region + " region from the move sets config!"));

                        } else {
                            player.sendMessage(Text.of(TextColors.RED, "No region with that name exists!"));
                        }

                    } catch (ObjectMappingException | IOException e) {
                        e.printStackTrace();
                    }


                    return CommandResult.success();

                })
                .build();

        CommandSpec deletePokemon = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("pokemon")),
                        GenericArguments.string(Text.of("region")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("level")))
                )
                .executor((sender, context) -> {

                    Player player = (Player) sender;
                    String region = context.getOne("region").get().toString();


                    try {
                        pokemon = PokemonSpec.from(context.getOne("pokemon").get().toString()).create((World) player.getWorld());
                    } catch (NullPointerException ex) {
                        player.sendMessage(Text.of(TextColors.RED, "There's no Pokemon with that name! Check your spelling!"));
                    }


                    try {
                        if (RegionHandler.doesRegionExist(region)) {
                            if (context.getOne("level").isPresent()) {
                                String lvl = context.getOne("level").get().toString();
                                config.getNode("Movesets", region, MovesetCommands.pokemon.getPokemonName(), lvl).setValue(null);
                                loader.save(config);
                            }

                        } else {
                            player.sendMessage(Text.of(TextColors.RED, "No region with that name exists!"));
                        }
                    } catch (ObjectMappingException | IOException e) {
                        e.printStackTrace();
                    }

                    return CommandResult.success();

                })
                .build();

        CommandSpec deleteMain = CommandSpec.builder()
                .child(deletePokemon, "pokemon")
                .child(deleteRegion, "region")
                .executor((sender, context) -> {

                    Player player = (Player) sender;
                    return CommandResult.success();

                })
                .build();

        CommandSpec main = CommandSpec.builder()
                .child(add, "add")
                .child(deleteMain, "delete")
                .executor((sender, context) -> {

                    Player player = (Player) sender;
                    return CommandResult.success();

                })
                .build();


        return main;
    }

}
