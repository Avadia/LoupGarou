package fr.leomelki.loupgarou.roles;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import fr.leomelki.com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import fr.leomelki.com.comphenix.packetwrapper.WrapperPlayServerEntityLook;
import fr.leomelki.com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import fr.leomelki.loupgarou.classes.LGGame;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.classes.LGWinType;
import fr.leomelki.loupgarou.events.*;
import fr.leomelki.loupgarou.events.LGPlayerKilledEvent.Reason;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class RCupidon extends Role {
    private static final EntityArmorStand eas = new EntityArmorStand(((CraftWorld) Bukkit.getWorlds().get(0)).getHandle(),
            0, 0, 0);
    private static DataWatcherObject<String> aB;
    private static DataWatcherObject<Boolean> aC;
    private static DataWatcherObject<Byte> Z;

    static {
        try {
            Field f = Entity.class.getDeclaredField("aB");
            f.setAccessible(true);
            aB = (DataWatcherObject<String>) f.get(null);
            f = Entity.class.getDeclaredField("aC");
            f.setAccessible(true);
            aC = (DataWatcherObject<Boolean>) f.get(null);
            f = Entity.class.getDeclaredField("Z");
            f.setAccessible(true);
            Z = (DataWatcherObject<Byte>) f.get(null);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    private static final String CUPIDON_FIRST = "cupidon_first";
    private static final String IN_LOVE = "in_love";

    WrappedDataWatcherObject invisible = new WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));
    WrappedDataWatcherObject noGravity = new WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class));

    public RCupidon(LGGame game) {
        super(game);
    }

    @Override
    public String getRawName() {
        return "Cupidon";
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

        return (amount > 1) ? baseline + "s" : baseline;
    }

    @Override
    public String getName() {
        return "§a§lCupidon";
    }

    @Override
    public String getFriendlyName() {
        return "de " + getName();
    }

    @Override
    public String getShortDescription() {
        return "Tu gagnes avec le §a§lVillage";
    }

    @Override
    public String getDescription() {
        return "Tu gagnes avec le §a§lVillage§f. Dès le début de la partie, tu dois former un couple de deux joueurs. Leur objectif sera de survivre ensemble, car si l'un d'eux meurt, l'autre se suicidera.";
    }

    @Override
    public String getTask() {
        return "Choisis deux joueurs à mettre en couple.";
    }

    @Override
    public String getBroadcastedTask() {
        return getName() + "§9 choisit deux âmes à unir.";
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    public boolean hasPlayersLeft() {
        return getGame().getNight() == 1;
    }

    @Override
    protected void onNightTurn(LGPlayer player, Runnable callback) {
        player.showView();

        player.choose(choosen -> {
            if (choosen != null) {
                if (player.hasProperty(RCupidon.CUPIDON_FIRST)) {
                    LGPlayer first = player.getCache().remove(RCupidon.CUPIDON_FIRST);
                    if (first == choosen) {
                        int entityId = Integer.MAX_VALUE - choosen.getPlayer().getEntityId();
                        WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
                        destroy.setEntityIds(new int[]{entityId});
                        destroy.sendPacket(player.getPlayer());
                    } else {
                        int entityId = Integer.MAX_VALUE - first.getPlayer().getEntityId();
                        WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
                        destroy.setEntityIds(new int[]{entityId});
                        destroy.sendPacket(player.getPlayer());

                        setInLove(first, choosen);
                        player.sendMessage("§7§l" + first.getFullName() + "§9 et §7§l" + choosen.getFullName()
                                + "§9 sont désormais follement amoureux.");
                        player.stopChoosing();
                        player.hideView();
                        callback.run();
                    }
                } else {
                    sendHead(player, choosen);
                    player.getCache().set(RCupidon.CUPIDON_FIRST, choosen);
                }
            }
        });
    }

    protected void setInLove(LGPlayer player1, LGPlayer player2) {
        player1.getCache().set(RCupidon.IN_LOVE, player2);
        player1.sendMessage(
                "§9Tu tombes amoureux de §7§l" + player2.getFullName() + "§9, il est " + player2.getRole().getName());
        player1.sendMessage("§9§oTu peux lui parler en mettant un §e!§9 devant ton message.");

        player2.getCache().set(RCupidon.IN_LOVE, player1);
        player2.sendMessage(
                "§9Tu tombes amoureux de §7§l" + player1.getFullName() + "§9, il est " + player1.getRole().getName());
        player2.sendMessage("§9§oTu peux lui parler en mettant un §e!§9 devant ton message.");

        // On peut créer des cheats grâce à ça (qui permettent de savoir qui est en couple)
        player1.updatePrefix();
        player2.updatePrefix();
    }

    protected void sendHead(LGPlayer to, LGPlayer ofWho) {
        int entityId = Integer.MAX_VALUE - ofWho.getPlayer().getEntityId();
        WrapperPlayServerSpawnEntityLiving spawn = new WrapperPlayServerSpawnEntityLiving();
        spawn.setEntityID(entityId);
        spawn.setType(EntityType.ARMOR_STAND);
        Location loc = ofWho.getPlayer().getLocation();
        spawn.setX(loc.getX());
        spawn.setY(loc.getY() + 0.3/*1.9*/);
        spawn.setZ(loc.getZ());
        spawn.setHeadPitch(0);
        Location toLoc = to.getPlayer().getLocation();
        double diffX = loc.getX() - toLoc.getX();
        double diffZ = loc.getZ() - toLoc.getZ();
        float yaw = 180 - ((float) Math.toDegrees(Math.atan2(diffX, diffZ)));

        spawn.setYaw(yaw);

        DataWatcher datawatcher = new DataWatcher(eas);
        datawatcher.register(Z, (byte) 0x20);
        datawatcher.register(aB, "§c§l♡");
        datawatcher.register(aC, true);
        PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(entityId, datawatcher, true);

        spawn.sendPacket(to.getPlayer());
        ((CraftPlayer) to.getPlayer()).getHandle().playerConnection.sendPacket(meta);

        WrapperPlayServerEntityLook look = new WrapperPlayServerEntityLook();
        look.setEntityID(entityId);
        look.setPitch(0);
        look.setYaw(yaw);
        look.sendPacket(to.getPlayer());

//        WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
//        meta.setEntityID(entityId);
//        meta.setMetadata(
//                Arrays.asList(new WrappedWatchableObject(invisible, (byte) 0x20), new WrappedWatchableObject(noGravity, true)));
//        meta.sendPacket(to.getPlayer());
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.getCache().remove(RCupidon.CUPIDON_FIRST);
        player.stopChoosing();
        player.hideView();
    }

    @EventHandler
    public void onPlayerKill(LGPlayerGotKilledEvent e) {
        if (e.getGame() == getGame() && e.getKilled().getCache().has(RCupidon.IN_LOVE)
                && !e.getKilled().getCache().<LGPlayer>get(RCupidon.IN_LOVE).isDead()) {
            LGPlayer killed = e.getKilled().getCache().get(RCupidon.IN_LOVE);
            LGPlayerKilledEvent event = new LGPlayerKilledEvent(getGame(), killed, Reason.LOVE);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                getGame().kill(event.getKilled(), event.getReason(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGameEnd(LGGameEndEvent e) {
        if (e.getGame() == getGame()) {
            WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
            ArrayList<Integer> ids = new ArrayList<>();
            for (LGPlayer lgp : getGame().getInGame())
                ids.add(Integer.MAX_VALUE - lgp.getPlayer().getEntityId());
            int[] intList = new int[ids.size()];
            for (int i = 0; i < ids.size(); i++)
                intList[i] = ids.get(i);
            destroy.setEntityIds(intList);
            for (LGPlayer lgp : getGame().getInGame())
                destroy.sendPacket(lgp.getPlayer());

            for (LGPlayer lgp : getGame().getInGame())
                if (lgp.getCache().has(RCupidon.IN_LOVE)) {
                    if (e.getWinType() == LGWinType.COUPLE) {
                        if (!e.getWinners().contains(lgp))
                            e.getWinners().add(lgp);
                    } else {
                        LGPlayer player2 = lgp.getCache().get(RCupidon.IN_LOVE);
                        boolean winEnCouple = (lgp.getRoleType() == RoleType.LOUP_GAROU) != (player2
                                .getRoleType() == RoleType.LOUP_GAROU) || lgp.getRoleWinType() == RoleWinType.SEUL
                                || player2.getRoleWinType() == RoleWinType.SEUL;
                        if (winEnCouple) {
                            System.out.println(lgp.getName() + " ne peut pas gagner car il était en couple !");
                            e.getWinners().remove(lgp);
                        }
                    }
                }
        }
    }

    @EventHandler
    public void onEndCheck(LGEndCheckEvent e) {
        if (e.getGame() == getGame()) {
            ArrayList<LGPlayer> winners = new ArrayList<>();
            for (LGPlayer lgp : getGame().getAlive())
                if (lgp.getRoleWinType() != RoleWinType.NONE)
                    winners.add(lgp);
            if (winners.size() == 2) {
                LGPlayer player1 = winners.get(0);
                LGPlayer player2 = winners.get(1);
                if (player1.getCache().get(RCupidon.IN_LOVE) == player2
                        && (player1.getRoleType() == RoleType.LOUP_GAROU) != (player2.getRoleType() == RoleType.LOUP_GAROU))
                    e.setWinType(LGWinType.COUPLE);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        LGPlayer player = LGPlayer.thePlayer(e.getPlayer());
        if (player.getGame() == getGame()) {
            if (e.getMessage().startsWith("!")) {
                if (player.getCache().has(RCupidon.IN_LOVE)) {
                    player.sendMessage("§d\u2764 " + player.getFullName() + " §6» §f" + e.getMessage().substring(1));
                    player.getCache().<LGPlayer>get(RCupidon.IN_LOVE)
                            .sendMessage("§d\u2764 " + player.getFullName() + " §6» §f" + e.getMessage().substring(1));
                } else {
                    player.sendMessage("§4Erreur : §cVous n'êtes pas en couple !");
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUpdatePrefix(LGUpdatePrefixEvent e) {
        if (e.getGame() == getGame() && e.getTo().getCache().get(RCupidon.IN_LOVE) == e.getPlayer()
                || ((e.getTo() == e.getPlayer() || e.getTo().getRole() == this)
                && e.getPlayer().getCache().has(RCupidon.IN_LOVE)))
            e.setPrefix("§d\u2764 §f" + e.getPrefix());
    }
}
