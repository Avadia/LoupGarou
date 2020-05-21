package fr.leomelki.loupgarou.events;

import fr.leomelki.loupgarou.classes.LGGame;
import fr.leomelki.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public class LGPyromaneGasoilEvent extends LGEvent implements Cancellable {
    @Getter
    @Setter
    private boolean cancelled;
    @Getter
    @Setter
    private LGPlayer player;

    public LGPyromaneGasoilEvent(LGGame game, LGPlayer player) {
        super(game);
        this.player = player;
    }
}