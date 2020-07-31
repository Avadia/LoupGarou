package fr.leomelki.loupgarou.scoreboard;

import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.classes.RolePlayers;
import fr.leomelki.loupgarou.roles.Role;
import lombok.Getter;
import net.samagames.tools.scoreboards.VObjective;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class CustomScoreboard {
    @Getter
    private final List<LGPlayer> inGamePlayers;
    private final List<CustomScoreboardEntry> entries = new ArrayList<>();
    private final boolean shouldShowScoreboard;
    @Getter
    private boolean shown;
    @Getter
    private final VObjective vObjective;

    public CustomScoreboard(List<LGPlayer> inGamePlayers, boolean shouldShowScoreboard) {
        this.inGamePlayers = inGamePlayers;
        this.shouldShowScoreboard = shouldShowScoreboard;
        this.vObjective = new VObjective("loupgaroubar", ChatColor.RED + "" + ChatColor.BOLD + "LoupGarou" + ChatColor.WHITE + " | " + ChatColor.YELLOW + "00:00");
    }

    private void createEntry(String name, int amountOfPlayers) {
        this.entries.add(new CustomScoreboardEntry(this, name, amountOfPlayers));
    }

    private void removePreexistingEntries() {
        final List<CustomScoreboardEntry> preexistingEntries = new ArrayList<>(this.entries);

        for (CustomScoreboardEntry preexistingEntry : preexistingEntries) {
            preexistingEntry.hide();
            this.entries.remove(preexistingEntry);
        }
    }

    public void displayEntries(List<RolePlayers> activeRoles) {
        this.removePreexistingEntries();

        for (RolePlayers currentPlayers : activeRoles) {
            final int amountOfPlayers = currentPlayers.getAmountOfPlayers();

            if (amountOfPlayers > 0) {
                if (this.shouldShowScoreboard) {
                    final Role currentRole = currentPlayers.getRole();
                    final String sanitizedName = currentRole.getName(amountOfPlayers).replace("§l", "");

                    this.createEntry(sanitizedName, amountOfPlayers);
                }
            }
        }
    }

    public void announce(String message, int fakeDuration) {
        this.removePreexistingEntries();
        this.createEntry(message, fakeDuration);
    }

    public void show() {
        for (LGPlayer currentPlayer : inGamePlayers) {
            vObjective.addReceiver(currentPlayer.getPlayer());
        }

        shown = true;
    }

    public void hide() {
        for (LGPlayer currentPlayer : inGamePlayers) {
            vObjective.removeReceiver(currentPlayer.getPlayer());
        }

        this.removePreexistingEntries();
        shown = false;
    }
}
