package fr.leomelki.loupgarou.scoreboard;

public class CustomScoreboardEntry {
    private final int amount;
    private final CustomScoreboard scoreboard;
    private final String name;

    public CustomScoreboardEntry(CustomScoreboard scoreboard, String rawName, int amount) {
        this.amount = amount;
        this.scoreboard = scoreboard;
        this.name = /*this.generateDisplayableName(*/rawName/*)*/;
        this.show();
    }

    public void show() {
        scoreboard.getVObjective().getScore(name).setScore(amount);
        scoreboard.getVObjective().updateScore(name);
    }

    public String generateDisplayableName(String rawName) {
        if (rawName.length() <= 16) {
            return rawName;
        }

        int limit = 16;

        if (rawName.charAt(15) == '§') {
            limit = 15;
        } else if (rawName.charAt(14) == '§' && rawName.charAt(13) != '§') {
            limit = 14;
        }

        final String sringifiedPrefix = rawName.substring(0, limit);
        String suffix;

        if (limit != 16) {
            suffix = rawName.substring(limit);
        } else {
            char colorCode = 'f';
            boolean storeColorCode = false;
            for (char c : sringifiedPrefix.toCharArray()) {
                if (storeColorCode) {
                    storeColorCode = false;
                    colorCode = c;
                } else if (c == '§') {
                    storeColorCode = true;
                }
            }
            suffix = "§" + colorCode + rawName.substring(limit);
        }

        return suffix;
    }

    public void hide() {
        if (scoreboard.isShown()) {
            scoreboard.getVObjective().removeScore(name);
        }
    }
}
