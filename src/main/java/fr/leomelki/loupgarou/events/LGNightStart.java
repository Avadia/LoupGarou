package fr.leomelki.loupgarou.events;

import fr.leomelki.loupgarou.classes.LGGame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public class LGNightStart extends LGEvent implements Cancellable {
    @Getter
    @Setter
    boolean cancelled;

    public LGNightStart(LGGame game) {
        super(game);
    }
}
