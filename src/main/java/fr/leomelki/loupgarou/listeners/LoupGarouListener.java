package fr.leomelki.loupgarou.listeners;

import fr.leomelki.com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import fr.leomelki.loupgarou.events.LGGameJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LoupGarouListener implements Listener {
    @EventHandler
    public void onGameJoin(LGGameJoinEvent e) {
        // Tous les loups-garous
        WrapperPlayServerScoreboardTeam teamDelete = new WrapperPlayServerScoreboardTeam();
        teamDelete.setMode(1);
        teamDelete.setName("loup_garou_list");
        teamDelete.sendPacket(e.getPlayer().getPlayer());
    }
}
