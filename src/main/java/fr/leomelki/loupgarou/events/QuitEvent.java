package fr.leomelki.loupgarou.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class QuitEvent extends CustomEvent {
    @Getter
    @Setter
    String quitMessage;

    public QuitEvent(Player player, String quitMessage) {
        super(player);
        this.quitMessage = quitMessage;
    }
}
