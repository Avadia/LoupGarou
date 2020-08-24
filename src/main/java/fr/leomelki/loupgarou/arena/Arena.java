package fr.leomelki.loupgarou.arena;

import fr.leomelki.loupgarou.MainLg;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.classes.LGWinType;
import fr.leomelki.loupgarou.events.JoinEvent;
import fr.leomelki.loupgarou.events.QuitEvent;
import fr.leomelki.loupgarou.roles.Role;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Game;
import net.samagames.api.games.GamePlayer;
import net.samagames.api.games.themachine.messages.templates.WinMessageTemplate;
import net.samagames.tools.InventoryUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 * This file is part of HangoverGames.
 *
 * HangoverGames is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HangoverGames is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HangoverGames.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Arena extends Game<GamePlayer> implements Listener {
    private final MainLg plugin;

    private final Location spawn;

    public Arena(MainLg plugin, Location spawn) {
        super("werewolf", "LoupGarou", "Attention à ne pas vous faire manger !", GamePlayer.class);

        this.plugin = plugin;
        this.spawn = spawn;
    }

    public void startManual() {
        MainLg.getInstance().getConfig().set("roleDistribution", "fixed");
        MainLg.getInstance().saveConfig();
        MainLg.getInstance().loadConfig();

        startCommon();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lg joinAll");
        for (Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lg start " + player.getName());
            break;
        }
    }

    public void startAuto() {
        MainLg.getInstance().getConfig().set("roleDistribution", "random");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lg random players " + Bukkit.getOnlinePlayers().size());

        startCommon();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lg joinAll");
        for (Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lg start " + player.getName());
            break;
        }
    }

    public void startCommon() {
        super.startGame();

//        for (GamePlayer gamePlayer : this.getInGamePlayers().values()) {
//            Player player = gamePlayer.getPlayerIfOnline();
//
//            //TODO: SamaGamesAPI.get().getStatsManager().getPlayerStats(player.getUniqueId()).getWerewolfStatistics().incrByPlayedGames(1);
//        }
    }

    @Override
    public void startGame() {
        startAuto();
    }

    @Override
    public void handlePostRegistration() {
        super.handlePostRegistration();

        this.coherenceMachine.setStartCountdownCatchPhrase("Préparez-vous à dormir !");
        this.coherenceMachine.setNameShortcut("LG");
    }

    @Override
    public void handleLogin(Player player) {
        super.handleLogin(player);

        InventoryUtils.cleanPlayer(player);

        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(this.spawn);
        player.getInventory().setItem(8, this.coherenceMachine.getLeaveItem());
        Bukkit.getPluginManager().callEvent(new JoinEvent(player, "is connected"));

        this.gameManager.refreshArena();
    }

    @Override
    public void handleLogout(Player player) {
        super.handleLogout(player);

        Bukkit.getPluginManager().callEvent(new QuitEvent(player, "is disconnected"));
    }

    @Override
    public Pair<Boolean, String> canJoinGame(UUID player, boolean reconnect) {
        if (this.getConnectedPlayers() + 1 > plugin.getConfig().getInt("distributionRandom.amountOfPlayers"))
            return Pair.of(false, "Partie pleine !");
        return super.canJoinGame(player, reconnect);
    }

    @Override
    public Pair<Boolean, String> canPartyJoinGame(List<UUID> partyMembers) {
        if (this.getConnectedPlayers() + partyMembers.size() > plugin.getConfig().getInt("distributionRandom.amountOfPlayers"))
            return Pair.of(false, "Partie pleine !");
        return super.canPartyJoinGame(partyMembers);
    }

    public void win(List<LGPlayer> winners, LGWinType winType, List<Role> roles) {
        //TODO: SamaGamesAPI.get().getStatsManager().getPlayerStats(player.getUniqueId()).getWerewolfStatistics().incrByWins(1);

        final boolean shouldDisplayWinners = (!winners.isEmpty());

        for (LGPlayer lgPlayer : winners) {
            if (lgPlayer.getPlayer() == null) continue;
            this.addCoins(lgPlayer.getPlayer(), 50, "Victoire !");
            this.effectsOnWinner(lgPlayer.getPlayer());
        }

        WinMessageTemplate template = SamaGamesAPI.get().getGameManager().getCoherenceMachine().getTemplateManager().getWinMessageTemplate();
        List<String> content = new ArrayList<>();
        content.add(winType.getMessage());
        if (shouldDisplayWinners) {
            final List<String> winnerNames = winners.stream().map(LGPlayer::getFullName)
                    .filter(fullName -> fullName != null && !fullName.equals("null")).collect(Collectors.toList());
            final String winnersFriendlyName = (winners.size() > 1) ? "aux vainqueurs" : "au vainqueur";
            content.add("§6§l§oFélicitations " + winnersFriendlyName + ": §7§l" + String.join(", §7", winnerNames));
        }

        content.add("\n");
        content.add("§e§lLa composition du village à cette partie était la suivante");

        // We unregister every role listener because they are unused after the game's
        // end !
        if (roles != null) {
            for (Role role : roles) {
                final List<String> playerNames = role.getPlayersThisRound().stream().map(LGPlayer::getFullName)
                        .filter(fullName -> fullName != null && !fullName.equals("null")).collect(Collectors.toList());
                content.add("§e - " + role.getName() + " §e: §7§l" + String.join(", §7", playerNames));
                HandlerList.unregisterAll(role);
            }
        }
        template.execute(content);

        this.handleGameEnd();
    }
}
