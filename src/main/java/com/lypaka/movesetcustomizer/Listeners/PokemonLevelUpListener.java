package com.lypaka.movesetcustomizer.Listeners;

import com.google.common.reflect.TypeToken;
import com.lypaka.movesetcustomizer.Config.ConfigManager;
import com.lypaka.movesetcustomizer.Utils.RegionHandler;
import com.pixelmongenerations.api.events.LevelUpEvent;
import com.pixelmongenerations.api.events.pokemon.LevelUpMovesEvent;
import com.pixelmongenerations.api.pokemon.PokemonSpec;
import com.pixelmongenerations.common.battle.attacks.Attack;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.Pixelmon;
import com.pixelmongenerations.core.network.EnumUpdateType;
import com.pixelmongenerations.core.network.packetHandlers.OpenReplaceMoveScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

public class PokemonLevelUpListener {

    private static ConfigurationNode movesetsConfig = ConfigManager.getConfigFile(0);
    private static List<String> moveQueue = new ArrayList<>();
    private static Player pokemonOwner;
    private static EntityPixelmon pokemonLearningMove;

    @SubscribeEvent
    public void onLevelUp (LevelUpEvent event) throws ObjectMappingException {

        /**
         *
         * Get and check if player is in a region
         * Get name of region
         * Get Pokemon and check move sets config for that Pokemon in that region
         * Get level and see if the Pokemon learns a move on this level
         * Teach move to Pokemon
         *
         */

        Player player = (Player) event.getPlayer();
        if (RegionHandler.isInRegion(player)) {
            EntityPixelmon pokemon = event.getPokemon().getEntity();
            setPokemon(pokemon);
            setPlayer(player);
            if (!movesetsConfig.getNode("Movesets", RegionHandler.getRegionName(player), pokemon.getPokemonName()).isVirtual()) {
                if (learnsMove(pokemon.getPokemonName(), RegionHandler.getRegionName(player), event.getLevel())) {
                    levelUpHandling(player, pokemon, RegionHandler.getRegionName(player), event.getLevel());
                }
            }
        }
    }

    // Cancels the event of normal level up moves (we don't need those anymore, right?)
    @SubscribeEvent
    public void onLevelUpMove (LevelUpMovesEvent event) throws ObjectMappingException {
        Player player = (Player) event.getPokemon().getOwner();
        if (RegionHandler.isInRegion(player)) {
            EntityPixelmon pokemon = PokemonSpec.from(event.getPokemon().getPokemonName()).create((World) player.getWorld());
            if (!movesetsConfig.getNode("Movesets", RegionHandler.getRegionName(player), pokemon.getPokemonName()).isVirtual()) {
                event.getAttacks().clear();
            }
        }
    }

    // Handles the event of learning more than one move on level up
    @SubscribeEvent
    public void onGUIClose (PlayerContainerEvent.Close event) {
        Player player = (Player) event.getEntityPlayer();
        try {
            if (PokemonLevelUpListener.getPlayer().getName().equals(player.getName())) {
                if (event.getContainer().toString().contains("net.minecraft.inventory.ContainerPlayer")) {
                    if (PokemonLevelUpListener.moveQueue.size() > 1) {
                        for (int i = 1; i <= PokemonLevelUpListener.moveQueue.size() - 1; i++) {
                            if (PokemonLevelUpListener.moveQueue.get(i) != null) {
                                teachMove(player, PokemonLevelUpListener.getPokemon(), new Attack(PokemonLevelUpListener.moveQueue.get(i)));
                            }
                        }
                        PokemonLevelUpListener.moveQueue.clear();
                    }
                }
            }
        } catch (NullPointerException er) {
            // This is just here so the plugin doesn't shit itself when closing other GUIs from other things and stuff
        }
    }


    private boolean learnsMove (String pokemon, String region, int level) throws ObjectMappingException {
        List<String> moveList = movesetsConfig.getNode("Movesets", region, pokemon, String.valueOf(level)).getList(TypeToken.of(String.class));
        return !moveList.isEmpty();
    }


    private void levelUpHandling(Player player, EntityPixelmon pokemon, String region, int level) throws ObjectMappingException {
        List<String> moveList = movesetsConfig.getNode("Movesets", region, pokemon.getPokemonName(), String.valueOf(level)).getList(TypeToken.of(String.class));
        if (moveList.size() > 1) {
            PokemonLevelUpListener.moveQueue.addAll(moveList);
            teachMove(player, pokemon, new Attack(moveQueue.get(0)));
        } else {
            teachMove(player, pokemon, new Attack(moveList.get(0)));
        }
    }


    private void teachMove(Player player, EntityPixelmon pokemon, Attack attack) {
        if (!pokemon.getMoveset().hasAttack(attack.baseAttack.getUnlocalizedName())) {
            if (pokemon.getMoveset().size() < 4) {
                pokemon.getMoveset().add(attack);
                pokemon.update(EnumUpdateType.Moveset);
                player.sendMessage(Text.of(TextColors.GREEN, pokemon.getPokemonName() + " just learned " + attack.baseAttack.getUnlocalizedName() + "!"));
            } else {
                Pixelmon.NETWORK.sendTo(new OpenReplaceMoveScreen(player.getUniqueId(), pokemon.getPokemonId(), attack.baseAttack.attackIndex, 0, pokemon.getLvl().getLevel()), (EntityPlayerMP) player);
            }
        }
    }


    private static void setPokemon (EntityPixelmon pokemon) {
        pokemonLearningMove = pokemon;
    }


    private static EntityPixelmon getPokemon() {
        return PokemonLevelUpListener.pokemonLearningMove;
    }


    private static void setPlayer (Player player) {
        pokemonOwner = player;
    }


    private static Player getPlayer() {
        return PokemonLevelUpListener.pokemonOwner;
    }

}
