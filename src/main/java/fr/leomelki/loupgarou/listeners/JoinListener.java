package fr.leomelki.loupgarou.listeners;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import fr.leomelki.com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import fr.leomelki.loupgarou.MainLg;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.events.JoinEvent;
import fr.leomelki.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.leomelki.loupgarou.events.QuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;

public class JoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getPluginManager().callEvent(new JoinEvent(e.getPlayer(), "is connected"));
        e.setJoinMessage("");
    }

    @EventHandler
    public void onJoin(PlayerQuitEvent e) {
        Bukkit.getPluginManager().callEvent(new QuitEvent(e.getPlayer(), "is disconnected"));
        e.setQuitMessage("");
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onJoin(JoinEvent e) {
        Player p = e.getPlayer();

        WrapperPlayServerScoreboardTeam myTeam = new WrapperPlayServerScoreboardTeam();
        myTeam.setName(p.getName());
        myTeam.setPrefix(WrappedChatComponent.fromText(""));
        myTeam.setPlayers(Collections.singletonList(p.getName()));
        myTeam.setMode(2);
        boolean noSpec = p.getGameMode() != GameMode.SPECTATOR;
        for (Player player : Bukkit.getOnlinePlayers())
            if (player != p) {
                if (player.getGameMode() != GameMode.SPECTATOR)
                    player.hidePlayer(p);
                WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
                team.setName(player.getName());
                team.setPrefix(WrappedChatComponent.fromText(""));
                team.setPlayers(Collections.singletonList(player.getName()));
                team.setMode(2);

                team.sendPacket(p);
                myTeam.sendPacket(player);
            }
        p.setSaturation(10000);
        p.setFoodLevel(10000);
        LGPlayer lgp = LGPlayer.thePlayer(e.getPlayer());
        lgp.showView();
        lgp.join(MainLg.getInstance().getCurrentGame());
        if (noSpec)
            p.setGameMode(GameMode.ADVENTURE);
        e.setJoinMessage("");
        p.removePotionEffect(PotionEffectType.JUMP);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @EventHandler
    public void onLeave(QuitEvent e) {
        Player p = e.getPlayer();
        LGPlayer lgp = LGPlayer.thePlayer(p);
        if (lgp.getGame() != null) {
            lgp.leaveChat();
            if (lgp.getRole() != null && !lgp.isDead())
                lgp.getGame().kill(lgp, Reason.DISCONNECTED, true);
            lgp.getGame().getInGame().remove(lgp);
            lgp.getGame().checkLeave();
        }
        LGPlayer.removePlayer(p);
        lgp.remove();
        e.setQuitMessage("");
    }

}
