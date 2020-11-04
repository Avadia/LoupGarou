package fr.leomelki.loupgarou.classes;

import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import fr.leomelki.com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import fr.leomelki.com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import fr.leomelki.loupgarou.MainLg;
import fr.leomelki.loupgarou.classes.chat.LGChat;
import fr.leomelki.loupgarou.classes.chat.LGChat.LGChatCallback;
import fr.leomelki.loupgarou.classes.chat.LGNoChat;
import fr.leomelki.loupgarou.roles.*;
import fr.leomelki.loupgarou.utils.VariableCache;
import fr.leomelki.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.EnumDifficulty;
import net.minecraft.server.v1_12_R1.EnumGamemode;
import net.minecraft.server.v1_12_R1.PacketPlayOutRespawn;
import net.minecraft.server.v1_12_R1.WorldType;
import net.samagames.tools.Titles;
import net.samagames.tools.chat.ActionBarAPI;
import net.samagames.tools.discord.DiscordAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LGPlayer {
    private static final HashMap<Player, LGPlayer> cachedPlayers = new HashMap<>();
    @Getter
    private final VariableCache cache = new VariableCache();
    @Getter
    boolean muted;
    long lastChoose;
    @Getter
    @Setter
    private int place;
    @Getter
    private Player player;
    @Getter
    @Setter
    private boolean dead;
    @Setter
    @Getter
    private Role role;
    private LGChooseCallback chooseCallback;
    private List<LGPlayer> blacklistedChoice = new ArrayList<>(0);
    @Getter
    @Setter
    private LGGame game;
    @Getter
    @Setter
    private String latestObjective;
    @Getter
    @Setter
    private String nick;
    private boolean canSelectDead;
    private String name;
    @Getter
    private LGChat chat;
    @Getter
    @Setter
    private boolean discord = false;

    public LGPlayer(Player player) {
        this.player = player;
    }

    public LGPlayer(String name) {
        this.name = name;
    }

    public static LGPlayer thePlayer(Player player) {
        return cachedPlayers.computeIfAbsent(player, LGPlayer::new);
    }

    public static void removePlayer(Player player) {
        cachedPlayers.remove(player);
    }

    public void sendActionBarMessage(String msg) {
        if (this.player != null)
            ActionBarAPI.sendMessage(player, msg);
    }

    public void sendMessage(String msg) {
        if (this.player != null)
            getPlayer().sendMessage(MainLg.getPrefix() + msg);
    }

    public void sendTitle(String title, String subTitle, int stay) {
        if (this.player != null) {
            Titles.sendTitle(player, 10, stay, 10, title, subTitle);
        }
    }

    public void remove() {
        this.player = null;
        if (isDiscord()) {
            List<UUID> playerList = new ArrayList<>();
            playerList.add(getPlayer().getUniqueId());
            Bukkit.getScheduler().runTaskAsynchronously(MainLg.getInstance(), () -> {
                DiscordAPI.unmutePlayers(playerList);
                DiscordAPI.kickPlayers(playerList);
            });
        }
    }

    public String getFullName() {
        final String playerName = (player != null) ? getPlayer().getName() : name;
        final String currentNick = (this.nick != null) ? "§8 => §b" + this.nick : "";

        return playerName + currentNick;
    }

    public String getName() {
        final String baselineName = player != null ? getPlayer().getName() : name;

        return (this.nick != null) ? this.nick : baselineName;
    }

    public String getName(boolean real) { // si true, alors on renvoie le vrai pseudo minecraft et pas le nick
        final String baselineName = player != null ? getPlayer().getName() : name;

        if (real) {
            return baselineName;
        }

        return (this.nick != null) ? this.nick : baselineName;
    }

    public boolean join(LGGame game) {
        if (getPlayer().getGameMode() == GameMode.SPECTATOR) {
            sendMessage("§cÉtant en mode spectateur, vous ne rejoignez pas la partie !");
            return false;
        }
        if (game.tryToJoin(this)) {
            // To update the skin
            updateOwnSkin();
            getPlayer().setWalkSpeed(0.2f);

            return true;
        }
        return false;
    }

    public void choose(LGChooseCallback callback, LGPlayer... blacklisted) {
        this.blacklistedChoice = blacklisted == null ? new ArrayList<>(0) : Arrays.asList(blacklisted);
        this.chooseCallback = callback;
    }

    public void stopChoosing() {
        this.blacklistedChoice = null;
        this.chooseCallback = null;
    }

    @SuppressWarnings("deprecation")
    public void showView() {
        if (getGame() != null && player != null)
            for (LGPlayer lgp : getGame().getAlive())
                if (!lgp.isDead()) {
                    if (lgp != this && lgp.getPlayer() != null)
                        getPlayer().showPlayer(lgp.getPlayer());
                    else {
                        WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
                        team.setMode(2);
                        team.setName(lgp.getName(true));
                        team.setPrefix("");
                        team.setPlayers(Collections.singletonList(lgp.getName(true)));
                        team.sendPacket(getPlayer());

                        WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
                        ArrayList<PlayerInfoData> infos = new ArrayList<>();
                        info.setAction(PlayerInfoAction.ADD_PLAYER);
                        infos.add(new PlayerInfoData(new WrappedGameProfile(getPlayer().getUniqueId(), getName(true)), 0,
                                NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(getName(true))));
                        info.setData(infos);
                        info.sendPacket(getPlayer());
                    }
                }

        getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 2, false, false));
    }

    // TODO Update prefix for only one guy
    public void updatePrefix() {
        if (getGame() != null && !isDead() && player != null) {
            List<String> meList = Collections.singletonList(getName(true));
            for (LGPlayer lgp : getGame().getInGame()) {
                WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
                ArrayList<PlayerInfoData> infos = new ArrayList<>();
                info.setAction(PlayerInfoAction.ADD_PLAYER);
                infos.add(new PlayerInfoData(new WrappedGameProfile(getPlayer().getUniqueId(), getName(true)), 0,
                        NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(getName(true))));
                info.setData(infos);
                info.sendPacket(lgp.getPlayer());

                WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
                team.setMode(2);
                team.setName(getName(true));
                team.setPrefix("DANG YO");
                team.setPlayers(meList);
                team.sendPacket(lgp.getPlayer());
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void hideView() {
        if (getGame() != null && player != null) {
            WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
            ArrayList<PlayerInfoData> infos = new ArrayList<>();
            info.setAction(PlayerInfoAction.ADD_PLAYER);
            for (LGPlayer lgp : getGame().getAlive())
                if (lgp != this && lgp.getPlayer() != null) {
                    if (!lgp.isDead())
                        infos.add(new PlayerInfoData(new WrappedGameProfile(lgp.getPlayer().getUniqueId(), lgp.getName(true)), 0,
                                NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(lgp.getName(true))));
                    getPlayer().hidePlayer(lgp.getPlayer());
                }
            info.setData(infos);
            info.sendPacket(getPlayer());
        }

        getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 1, false, false));
    }

    @SuppressWarnings("deprecation")
    public void updateSkin() {
        if (getGame() != null && player != null) {
            for (LGPlayer lgp : getGame().getInGame()) {
                if (lgp == this) {
                    WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
                    ArrayList<PlayerInfoData> infos = new ArrayList<>();
                    info.setAction(PlayerInfoAction.ADD_PLAYER);
                    infos.add(new PlayerInfoData(new WrappedGameProfile(getPlayer().getUniqueId(), getName(true)), 0,
                            NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(getName(true))));
                    info.setData(infos);
                    info.sendPacket(getPlayer());
                } else if (!isDead() && lgp.getPlayer() != null) {
                    lgp.getPlayer().hidePlayer(getPlayer());
                    lgp.getPlayer().showPlayer(getPlayer());
                }
            }
        }
    }

    public void updateOwnSkin() {
        if (player != null) {
            // On change son skin avec un packet de PlayerInfo (dans le tab)
            WrapperPlayServerPlayerInfo infos = new WrapperPlayServerPlayerInfo();
            infos.setAction(PlayerInfoAction.ADD_PLAYER);
            WrappedGameProfile gameProfile = new WrappedGameProfile(getPlayer().getUniqueId(), getPlayer().getName());
            infos.setData(Collections.singletonList(new PlayerInfoData(gameProfile, 10, NativeGameMode.SURVIVAL,
                    WrappedChatComponent.fromText(getPlayer().getName()))));
            infos.sendPacket(getPlayer());
            // Pour qu'il voit son skin changer (sa main et en f5), on lui dit qu'il respawn
            // (alors qu'il n'est pas mort mais ça marche quand même mdr)
            PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(0, EnumDifficulty.NORMAL, WorldType.NORMAL,
                    EnumGamemode.ADVENTURE);
            ((CraftPlayer) getPlayer()).getHandle().playerConnection.sendPacket(respawn);
            // Enfin, on le téléporte à sa potion actuelle car sinon il se verra dans le
            // vide
            getPlayer().teleport(getPlayer().getLocation());
            float speed = getPlayer().getWalkSpeed();
            getPlayer().setWalkSpeed(0.2f);
            Player tempPlayer = getPlayer();
            new BukkitRunnable() {
                @Override
                public void run() {
                    tempPlayer.updateInventory();
                    tempPlayer.setWalkSpeed(speed);
                }
            }.runTaskLater(MainLg.getInstance(), 5);
            // Et c'est bon, le joueur se voit avec un nouveau skin avec quasiment aucun
            // problème visible à l'écran :D
        }
    }

    public LGPlayer getPlayerOnCursor(List<LGPlayer> list) {
        Location loc = getPlayer().getLocation();
        if (loc.getPitch() > 60) {
            return (blacklistedChoice.contains(this)) ? null : this;
        }
        for (int i = 0; i < 50; i++) {
            loc.add(loc.getDirection());
            for (LGPlayer currentPlayer : list) {
                if (currentPlayer != this && !blacklistedChoice.contains(currentPlayer)
                        && (!currentPlayer.isDead() || canSelectDead)
                        && VariousUtils.distanceSquaredXZ(loc, currentPlayer.getPlayer().getLocation()) < 0.35
                        && Math.abs(loc.getY() - currentPlayer.getPlayer().getLocation().getY()) < 2) {
                    return currentPlayer;
                }
            }
        }
        return null;
    }

    public boolean isVampire() {
        return this.getCache().getBoolean(RVampire.INFECTED_BY_VAMPIRE);
    }

    public boolean isInfected() {
        return this.getCache().getBoolean(RLoupGarouNoir.INFECTED_BY_BLACK_WOLF);
    }

    public boolean hasProperty(String property) {
        return this.getCache().getBoolean(property);
    }

    public void setProperty(String property) {
        this.getCache().set(property, true);
    }

    public void removeProperty(String property) {
        this.getCache().remove(property);
    }

    public RoleType getRoleType() {
        if (this.isVampire()) {
            return RoleType.VAMPIRE;
        }

        if (this.isInfected()) {
            return RoleType.LOUP_GAROU;
        }

        return getRole().getType();
    }

    public RoleWinType getRoleWinType() {
        if (this.isVampire()) {
            return RoleWinType.VAMPIRE;
        }

        if (this.isInfected()) {
            return RoleWinType.LOUP_GAROU;
        }

        return getRole().getWinType(this);
    }

    public boolean isRoleActive() {
        return !this.isVampire();
    }

    public void die() {
        setMuted();
    }

    @SuppressWarnings("deprecation")
    private void setMuted() {
        if (player != null)
            for (LGPlayer lgp : getGame().getInGame())
                if (lgp != this && lgp.getPlayer() != null)
                    lgp.getPlayer().hidePlayer(getPlayer());
        muted = true;
    }

    public void resetMuted() {
        muted = false;
    }

    public void joinChat(LGChat chat, LGChatCallback callback) {
        joinChat(chat, callback, false);
    }

    public void joinChat(LGChat chat) {
        joinChat(chat, null, false);
    }

    public void joinChat(LGChat chat, boolean muted) {
        joinChat(chat, null, muted);
    }

    public void joinChat(LGChat chat, LGChatCallback callback, boolean muted) {
        if (this.chat != null && !muted)
            this.chat.leave(this);

        if (!muted)
            this.chat = chat;

        if (chat != null && player != null)
            chat.join(this, callback == null ? chat.getDefaultCallback() : callback);
    }

    public void leaveChat() {
        joinChat(new LGNoChat(), null);
    }

    public void onChat(String message) {
        if (chat != null) {
            chat.sendMessage(this, message);
        }
    }

    public void playAudio(LGSound sound, double volume) {
        if (player != null)
            getPlayer().playSound(getPlayer().getLocation(), sound.getSound(), (float) volume, 1);
    }

    public void stopAudio(LGSound sound) {
        if (player != null)
            getPlayer().stopSound(sound.getSound());
    }

    public void chooseAction() {
        long now = System.currentTimeMillis();
        if (lastChoose + 200 < now) {
            if (chooseCallback != null)
                chooseCallback.callback(getPlayerOnCursor(getGame().getInGame()));
            lastChoose = now;
        }
    }

    @Override
    public String toString() {
        return super.toString() + " (" + getName() + ")";
    }

    public void disableAbilityToSelectDead() {
        this.canSelectDead = false;
    }

    public void enableAbilityToSelectDead() {
        this.canSelectDead = true;
    }

    public interface LGChooseCallback {
        void callback(LGPlayer choosen);
    }
}
