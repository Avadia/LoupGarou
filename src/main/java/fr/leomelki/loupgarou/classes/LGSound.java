package fr.leomelki.loupgarou.classes;

import lombok.Getter;
import org.bukkit.Sound;

public enum LGSound {
    KILL(Sound.ENTITY_LIGHTNING_IMPACT),
    START_NIGHT(Sound.ENTITY_BAT_TAKEOFF),
    START_DAY(Sound.ENTITY_VILLAGER_YES);

    @Getter
    Sound sound;

    LGSound(Sound sound) {
        this.sound = sound;
    }
}
