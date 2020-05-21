package fr.leomelki.loupgarou.events;

import fr.leomelki.loupgarou.classes.LGGame;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.classes.LGWinType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

import java.util.List;

public class LGGameEndEvent extends LGEvent implements Cancellable {
    @Getter
    private final LGWinType winType;
    @Getter
    private final List<LGPlayer> winners;
    @Getter
    @Setter
    private boolean cancelled;

    public LGGameEndEvent(LGGame game, LGWinType winType, List<LGPlayer> winners) {
        super(game);
        this.winType = winType;
        this.winners = winners;
    }
}