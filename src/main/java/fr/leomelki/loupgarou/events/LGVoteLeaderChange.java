package fr.leomelki.loupgarou.events;

import fr.leomelki.loupgarou.classes.LGGame;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.classes.LGVote;
import lombok.Getter;

import java.util.List;

public class LGVoteLeaderChange extends LGEvent {
    @Getter
    List<LGPlayer> latest;
    @Getter
    List<LGPlayer> now;
    @Getter
    LGVote vote;

    public LGVoteLeaderChange(LGGame game, LGVote vote, List<LGPlayer> latest, List<LGPlayer> now) {
        super(game);
        this.latest = latest;
        this.now = now;
        this.vote = vote;
    }
}
