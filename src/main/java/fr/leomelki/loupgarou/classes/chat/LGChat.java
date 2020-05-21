package fr.leomelki.loupgarou.classes.chat;

import fr.leomelki.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map.Entry;

@RequiredArgsConstructor
public class LGChat {
    @Getter
    private final HashMap<LGPlayer, LGChatCallback> viewers = new HashMap<>();
    @Getter
    private final LGChatCallback defaultCallback;

    public void sendMessage(LGPlayer sender, String message) {
        String sendMessage = getViewers().get(sender).send(sender, message);
        for (Entry<LGPlayer, LGChatCallback> entry : viewers.entrySet())
            entry.getKey().sendMessage(sendMessage != null ? sendMessage : entry.getValue().receive(sender, message));
    }

    public void join(LGPlayer player, LGChatCallback callback) {
        if (getViewers().containsKey(player))
            getViewers().replace(player, callback);
        else
            getViewers().put(player, callback);
    }

    public void leave(LGPlayer player) {
        getViewers().remove(player);
    }

    public interface LGChatCallback {
        String receive(LGPlayer sender, String message);

        default String send(LGPlayer sender, String message) {
            return null;
        }
    }
}
