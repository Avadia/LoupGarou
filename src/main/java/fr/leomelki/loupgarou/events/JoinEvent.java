package fr.leomelki.loupgarou.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class JoinEvent extends CustomEvent {
    @Getter
    @Setter
    String joinMessage;

    public JoinEvent(Player player, String joinMessage) {
        super(player);
        this.joinMessage = joinMessage;
    }
}
