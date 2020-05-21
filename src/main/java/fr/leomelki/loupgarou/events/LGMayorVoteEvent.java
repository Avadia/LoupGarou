package fr.leomelki.loupgarou.events;

import fr.leomelki.loupgarou.classes.LGGame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public class LGMayorVoteEvent extends LGEvent implements Cancellable {
    @Getter
    @Setter
    private boolean cancelled;

    public LGMayorVoteEvent(LGGame game) {
        super(game);
    }
}
