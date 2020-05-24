package fr.leomelki.loupgarou.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("NullableProblems")
@RequiredArgsConstructor
public class CustomEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    final Player player;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return CustomEvent.getHandlerList();
    }
}
