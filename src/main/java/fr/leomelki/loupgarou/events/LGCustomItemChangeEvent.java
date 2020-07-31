package fr.leomelki.loupgarou.events;

import fr.leomelki.loupgarou.classes.LGCustomItems;
import fr.leomelki.loupgarou.classes.LGGame;
import fr.leomelki.loupgarou.classes.LGPlayer;
import lombok.Getter;

import java.util.List;

public class LGCustomItemChangeEvent extends LGEvent {
    @Getter
    private final LGPlayer player;
    @Getter
    private final List<LGCustomItems.LGCustomItemsConstraints> constraints;

    public LGCustomItemChangeEvent(LGGame game, LGPlayer player, List<LGCustomItems.LGCustomItemsConstraints> constraints) {
        super(game);
        this.player = player;
        this.constraints = constraints;
    }
}
