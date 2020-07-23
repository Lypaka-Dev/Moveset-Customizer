package com.lypaka.movesetcustomizer;

import com.google.inject.Inject;
import com.lypaka.movesetcustomizer.Commands.MovesetCommands;
import com.lypaka.movesetcustomizer.Commands.RegionCommands;
import com.lypaka.movesetcustomizer.Config.ConfigManager;
import com.lypaka.movesetcustomizer.Listeners.PokemonLevelUpListener;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;

@Plugin(
        id = "movesetcustomizer",
        name = "Moveset-Customizer",
        version = "1.0.0"
)
public class MovesetCustomizer {

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path configDir;

    @Inject
    private PluginContainer container;

    @Inject
    public Logger logger;

    public static MovesetCustomizer instance;


    @Listener
    public void onPreInit (GamePreInitializationEvent event) {
        instance = this;
        container = Sponge.getPluginManager().getPlugin("movesetcustomizer").get();
        ConfigManager.setup(configDir);
        loadPluginCommands();
        MinecraftForge.EVENT_BUS.register(new PokemonLevelUpListener());
        logger.info("Loaded custom movesets!");
    }


    private void loadPluginCommands() {
        CommandSpec main = CommandSpec.builder()
                .child(RegionCommands.getRegionCommands(), "region")
                .child(MovesetCommands.getMovesetCommands(), "movesets")
                .permission("movesetcustomizer.command.admin")
                .executor((sender, context) -> {

                    Player player = (Player) sender;
                    return CommandResult.success();

                })
                .build();

        Sponge.getCommandManager().register(instance, main, "mc", "movesetcustomizer");
    }

    public static Logger getLogger() {
        return instance.logger;
    }
    public static PluginContainer getContainer() {
        return instance.container;
    }
}
