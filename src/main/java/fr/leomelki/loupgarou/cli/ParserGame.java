package fr.leomelki.loupgarou.cli;

import fr.leomelki.loupgarou.classes.LGGame;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.classes.LGWinType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.Objects;

class ParserGame extends ParserAbstract {
    protected ParserGame(CommandInterpreter other) {
        super(other);
    }

    /* ========================================================================== */
    /*                              MAKE PLAYERS JOIN                             */
    /* ========================================================================== */

    protected void processJoinAll(CommandSender sender) {
        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        sender.sendMessage("\n§7" + onlinePlayers.size() + " joueurs ont été détectés en ligne et vont rejoindre la partie");

        for (Player p : onlinePlayers) {
            Bukkit.getPluginManager().callEvent(new PlayerQuitEvent(p, "joinall"));
        }

        for (Player p : onlinePlayers) {
            Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(p, "joinall"));
        }
    }

    /* ========================================================================== */
    /*                               START THE GAME                               */
    /* ========================================================================== */

    protected void processStartGame(CommandSender sender, String[] args) {
        final boolean isTargetingPlayer = (args.length == 2);
        final Player player = (isTargetingPlayer) ? Bukkit.getPlayer(args[1]) : (Player) sender;

        if (isTargetingPlayer && player == null) {
            sender.sendMessage(PLAYER_ERROR_PREFIX + args[1] + "§c n'existe pas !");
            return;
        }

        final LGPlayer lgp = LGPlayer.thePlayer(player);

        if (lgp.getGame() == null) {
            sender.sendMessage(PLAYER_ERROR_PREFIX + lgp.getName() + "§c n'est pas dans une partie.");
            return;
        }

        if (Objects.requireNonNull(this.instanceMainLg.getConfig().getList("spawns")).size() < lgp.getGame().getMaxPlayers()) {
            sender.sendMessage("\n§4Erreur : §cIl n'y a pas assez de points de spawn !");
            sender.sendMessage("§8§oPour les définir, merci de faire §7/lg addSpawn");
            return;
        }

        sender.sendMessage("\n§aVous avez bien démarré une nouvelle partie !");
        lgp.getGame().updateStart();
    }

    /* ========================================================================== */
    /*                                END THE GAME                                */
    /* ========================================================================== */

    protected void processEndGame(CommandSender sender, String[] args) {
        final boolean isTargetingPlayer = (args.length == 2);
        final Player selected = (isTargetingPlayer) ? Bukkit.getPlayer(args[1]) : (Player) sender;

        if (isTargetingPlayer && selected == null) {
            sender.sendMessage(PLAYER_ERROR_PREFIX + args[1] + "§c n'est pas connecté.");
            return;
        }

        final LGGame game = LGPlayer.thePlayer(selected).getGame();

        if (game == null) {
            sender.sendMessage(PLAYER_ERROR_PREFIX + selected.getName() + "§c n'est pas dans une partie.");
            return;
        }

        game.cancelWait();
        game.endGame(LGWinType.EQUAL);
        game.broadcastMessage("\n§cLa partie a été arrêtée de force !");
    }
}
