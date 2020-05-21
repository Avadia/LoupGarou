package fr.leomelki.loupgarou.events;

import fr.leomelki.loupgarou.classes.LGGame;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.events.LGPlayerKilledEvent.Reason;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public class LGNightPlayerPreKilledEvent extends LGEvent implements Cancellable {
    @Getter
    private final LGPlayer killed;
    @Getter
    @Setter
    boolean cancelled;
    @Getter
    @Setter
    private Reason reason;

    public LGNightPlayerPreKilledEvent(LGGame game, LGPlayer killed, Reason reason) {
        super(game);
        this.killed = killed;
        this.reason = reason;
    }
}
