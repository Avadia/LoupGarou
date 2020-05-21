package fr.leomelki.loupgarou.events;

import fr.leomelki.loupgarou.classes.LGGame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("NullableProblems")
@RequiredArgsConstructor
public class LGEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    final LGGame game;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return LGEvent.getHandlerList();
    }
}
