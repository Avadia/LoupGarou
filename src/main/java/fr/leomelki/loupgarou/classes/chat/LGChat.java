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
    //TODO If discord rate limit inscresed
//    @Getter
//    @Setter
//    private long discordID = -1L;

    public void sendMessage(LGPlayer sender, String message) {
        String sendMessage = getViewers().get(sender).send(sender, message);
        for (Entry<LGPlayer, LGChatCallback> entry : viewers.entrySet())
            entry.getKey().sendMessage(sendMessage != null ? sendMessage : entry.getValue().receive(sender, message));
    }

    public void join(LGPlayer player, LGChatCallback callback) {
        //TODO If discord rate limit inscresed
//        Bukkit.getScheduler().runTaskAsynchronously(SamaGamesAPI.get().getPlugin(), () -> {
//            if (discordID == -1L && SamaGamesAPI.get().getGameManager().getGame().hasDiscordChannel())
//                discordID = DiscordAPI.createChannel(SamaGamesAPI.get().getServerName());
//            if (discordID != -1L && player.isDiscord()) {
//                List<UUID> playersToMove = new ArrayList<>();
//                playersToMove.add(player.getPlayer().getUniqueId());
//                DiscordAPI.movePlayers(playersToMove, discordID);
//            }
//        });
        if (getViewers().containsKey(player))
            getViewers().replace(player, callback);
        else
            getViewers().put(player, callback);
    }

    public void leave(LGPlayer player) {
        getViewers().remove(player);
        //TODO If discord rate limit inscresed
//        Bukkit.getScheduler().runTaskLaterAsynchronously(SamaGamesAPI.get().getPlugin(), () -> {
//            if (getViewers().isEmpty() && discordID != -1L) {
//                DiscordAPI.deleteChannel(discordID);
//                discordID = -1L;
//            }
//        }, 50L);
    }

    public interface LGChatCallback {
        String receive(LGPlayer sender, String message);

        default String send(LGPlayer sender, String message) {
            return null;
        }
    }
}
