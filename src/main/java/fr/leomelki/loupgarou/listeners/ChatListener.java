package fr.leomelki.loupgarou.listeners;

import fr.leomelki.loupgarou.classes.LGPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!e.isCancelled()) {
            LGPlayer player = LGPlayer.thePlayer(e.getPlayer());
            player.onChat(e.getMessage());
            e.setCancelled(true);
        }
    }
}
