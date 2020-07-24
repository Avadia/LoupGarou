package fr.leomelki.loupgarou.scoreboard;

import fr.leomelki.com.comphenix.packetwrapper.WrapperPlayServerScoreboardDisplayObjective;
import fr.leomelki.com.comphenix.packetwrapper.WrapperPlayServerScoreboardObjective;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.classes.RolePlayers;
import fr.leomelki.loupgarou.roles.Role;
import fr.leomelki.loupgarou.utils.RandomString;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class CustomScoreboard {
    private static final String DISPLAY_NAME = "§e§lAvadia §7⚡ §c§lLoup-Garou";
    @Getter
    private final String name = RandomString.generate(15);
    @Getter
    private final List<LGPlayer> inGamePlayers;
    private final List<CustomScoreboardEntry> entries = new ArrayList<>();
    private final boolean shouldShowScoreboard;
    @Getter
    private boolean shown;

    public CustomScoreboard(List<LGPlayer> inGamePlayers, boolean shouldShowScoreboard) {
        this.inGamePlayers = inGamePlayers;
        this.shouldShowScoreboard = shouldShowScoreboard;
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
        WrapperPlayServerScoreboardObjective objective = new WrapperPlayServerScoreboardObjective();
        objective.setMode(0);
        objective.setName(name);
        objective.setDisplayName(DISPLAY_NAME);

        WrapperPlayServerScoreboardDisplayObjective display = new WrapperPlayServerScoreboardDisplayObjective();
        display.setPosition(1);
        display.setScoreName(name);

        for (LGPlayer currentPlayer : inGamePlayers) {
            objective.sendPacket(currentPlayer.getPlayer());
            display.sendPacket(currentPlayer.getPlayer());
        }

        shown = true;
    }

    public void hide() {
        WrapperPlayServerScoreboardObjective remove = new WrapperPlayServerScoreboardObjective();
        remove.setMode(1);
        remove.setName(name);

        for (LGPlayer currentPlayer : inGamePlayers) {
            remove.sendPacket(currentPlayer.getPlayer());
        }

        this.removePreexistingEntries();
        shown = false;
    }
}
