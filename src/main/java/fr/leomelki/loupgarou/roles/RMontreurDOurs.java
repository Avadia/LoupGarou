package fr.leomelki.loupgarou.roles;

import fr.leomelki.loupgarou.MainLg;
import fr.leomelki.loupgarou.classes.LGGame;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.events.LGDayStartEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;
import java.util.Objects;

public class RMontreurDOurs extends Role {
    private int lastNight = -1;

    public RMontreurDOurs(LGGame game) {
        super(game);
    }

    @Override
    public String getRawName() {
        return "MontreurDOurs";
    }

    @Override
    public RoleType getType() {
        return RoleType.VILLAGER;
    }

    @Override
    public RoleWinType getWinType() {
        return RoleWinType.VILLAGE;
    }

    @Override
    public String getName(int amount) {
        final String baseline = this.getName();

        return (amount > 1) ? baseline.replace("ontreur", "ontreurs") : baseline;
    }

    @Override
    public String getName() {
        return "§a§lMontreur d'Ours";
    }

    @Override
    public String getFriendlyName() {
        return "du " + getName();
    }

    @Override
    public String getShortDescription() {
        return "Tu gagnes avec le §a§lVillage";
    }

    @Override
    public String getDescription() {
        return "Tu gagnes avec le §a§lVillage§f. Chaque matin, ton Ours va renifler tes voisins et grognera si l'un d'eux est hostile aux Villageois.";
    }

    @Override
    public String getTask() {
        return "";
    }

    @Override
    public String getBroadcastedTask() {
        return "";
    }

    @Override
    public int getTimeout() {
        return -1;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDay(LGDayStartEvent e) {
        if (e.getGame() == getGame() && !getPlayers().isEmpty()) {
            if (lastNight == getGame().getNight())
                return;
            lastNight = getGame().getNight();
            List<?> original = MainLg.getInstance().getConfig().getList("spawns");
            for (LGPlayer target : getPlayers()) {
                if (!target.isRoleActive())
                    continue;
                int size = Objects.requireNonNull(original).size();
                int killedPlace = target.getPlace();

                for (int i = killedPlace + 1; ; i++) {
                    if (i == size)
                        i = 0;
                    LGPlayer lgp = getGame().getPlacements().get(i);
                    if (lgp != null && !lgp.isDead()) {
                        if (lgp.getRoleWinType() == RoleWinType.VILLAGE || lgp.getRoleWinType() == RoleWinType.NONE)
                            break;
                        else {
                            getGame().broadcastMessage("§6La bête du " + getName() + "§6 grogne...");
                            return;
                        }
                    }
                    if (lgp == target)// Fait un tour complet
                        break;
                }
                for (int i = killedPlace - 1; ; i--) {
                    if (i == -1)
                        i = size - 1;
                    LGPlayer lgp = getGame().getPlacements().get(i);
                    if (lgp != null && !lgp.isDead()) {
                        if (lgp.getRoleWinType() == RoleWinType.VILLAGE || lgp.getRoleWinType() == RoleWinType.NONE)
                            break;
                        else {
                            getGame().broadcastMessage("§6La bête du " + getName() + "§6 grogne...");
                            return;
                        }
                    }
                    if (lgp == target)// Fait un tour complet
                        break;
                }
            }
        }
    }
}
