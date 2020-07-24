package fr.leomelki.fr.farmvivi.avadia;

import fr.leomelki.loupgarou.MainLg;
import fr.leomelki.loupgarou.classes.LGGame;
import fr.leomelki.loupgarou.events.LGGameEndEvent;
import fr.leomelki.loupgarou.events.LGGameJoinEvent;
import fr.leomelki.loupgarou.roles.Role;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class AvadiaListener implements Listener {
    @Getter
    private final Map<String, Constructor<? extends Role>> roles;

    public AvadiaListener(Map<String, Constructor<? extends Role>> roles) {
        this.roles = roles;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();

        if (item == null) return;

        if (item.getType().equals(Material.TRIPWIRE_HOOK)) {
            Player p = e.getPlayer();

            Inventory gui = Bukkit.createInventory(null, InventoryType.HOPPER, "Paramètres de la partie");
            gui.setItem(1, new ItemBuilder(Material.SKULL_ITEM).durability(3).name("§6Choisir les rôles (manuel)").make());
            ItemBuilder scoreboard = new ItemBuilder(Material.WATCH);
            if (MainLg.getInstance().getConfig().getBoolean("showScoreboard")) {
                scoreboard.name("§eCacher les rôles");
            } else {
                scoreboard.name("§eAfficher les rôles");
            }
            gui.setItem(3, scoreboard.make());
            p.openInventory(gui);
        } else if (item.getType().equals(Material.LEVER)) {
            Player p = e.getPlayer();

            Inventory gui = Bukkit.createInventory(null, InventoryType.HOPPER, "Lancement de la partie");
            gui.setItem(1, new ItemBuilder(Material.SHEARS).name("§7Mode manuel").make());
            gui.setItem(3, new ItemBuilder(Material.REDSTONE).name("§cMode automatique").make());
            p.openInventory(gui);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null)
            return;
        ItemStack item = e.getCurrentItem();
        switch (e.getView().getTitle()) {
            case "Lancement de la partie":
                e.setCancelled(true);

                if (item.getType() == Material.SHEARS) {
                    MainLg.getInstance().getConfig().set("roleDistribution", "fixed");
                    MainLg.getInstance().saveConfig();
                    MainLg.getInstance().loadConfig();
                } else if (item.getType() == Material.REDSTONE) {
                    MainLg.getInstance().getConfig().set("roleDistribution", "random");
                    Bukkit.dispatchCommand(p, "lg random players " + Bukkit.getOnlinePlayers().size());
                }

                Bukkit.dispatchCommand(p, "lg joinAll");
                Bukkit.dispatchCommand(p, "lg start " + p.getName());

                break;
            case "Paramètres de la partie":
                e.setCancelled(true);

                if (item.getType() == Material.SKULL) {
                    AtomicInteger index = new AtomicInteger();
                    Inventory gui = Bukkit.createInventory(null, 4 * 9, "Rôles (manuel)");
                    getRoles().forEach((s, constructor) -> gui.setItem(index.getAndIncrement(), getItem(s)));
                    gui.setItem(35, new ItemBuilder(Material.GOLD_NUGGET).name("§aValider").make());
                    p.openInventory(gui);
                } else if (item.getType() == Material.WATCH) {
                    if (MainLg.getInstance().getConfig().getBoolean("showScoreboard")) {
                        MainLg.getInstance().getConfig().set("showScoreboard", false);
                        p.sendMessage("§eLes rôles ne seront plus visibles.");
                    } else {
                        MainLg.getInstance().getConfig().set("showScoreboard", true);
                        p.sendMessage("§eLes rôles seront visibles.");
                    }
                    MainLg.getInstance().saveConfig();
                    MainLg.getInstance().loadConfig();
                    p.closeInventory();
                }
                break;
            case "Rôles (manuel)":
                AtomicInteger index = new AtomicInteger();
                AtomicInteger n = new AtomicInteger();

                e.setCancelled(true);

                if (item.getType() == Material.GOLD_NUGGET) {
                    p.closeInventory();
                    Bukkit.dispatchCommand(p, "lg roles");
                } else if (e.isLeftClick()) {
                    MainLg.getInstance().getRolesBuilder().forEach((s, constructor) -> {
                        if (s.equals(Objects.requireNonNull(item.getItemMeta()).getDisplayName().replaceFirst("§6", ""))) {
                            n.set(MainLg.getInstance().getConfig().getInt("distributionFixed." + s));
                            Bukkit.dispatchCommand(p, "lg roles set " + index + " " + (n.get() + 1));
                            e.setCurrentItem(getItem(s));
                            return;
                        }
                        index.getAndIncrement();
                    });
                } else if (e.isRightClick()) {
                    MainLg.getInstance().getRolesBuilder().forEach((s, constructor) -> {
                        if (s.equals(Objects.requireNonNull(item.getItemMeta()).getDisplayName().replaceFirst("§6", ""))) {
                            n.set(MainLg.getInstance().getConfig().getInt("distributionFixed." + s));
                            if (n.get() > 0)
                                Bukkit.dispatchCommand(p, "lg roles set " + index + " " + (n.get() - 1));
                            e.setCurrentItem(getItem(s));
                            return;
                        }
                        index.getAndIncrement();
                    });
                }
                break;
        }
    }

    @EventHandler
    public void onJoin(LGGameJoinEvent event) {
        Player player = event.getPlayer().getPlayer();
        setupItems(player);
    }

    @EventHandler
    public void onGameEnd(LGGameEndEvent event) {
        MainLg.getInstance().setEndGame(true);
        MainLg.getInstance().setStartGame(false);
        Bukkit.getScheduler().runTaskLater(MainLg.getInstance(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.kickPlayer("Partie terminé.");
            }
            FileConfiguration config = MainLg.getInstance().getConfig();
            for (String role : getRoles().keySet())
                config.set("distributionFixed." + role, 0);
            config.set("distributionFixed.Villageois", 12);
            config.set("distributionRandom.amountOfPlayers", 12);
            config.set("showScoreboard", true);
            MainLg.getInstance().setEndGame(false);
            MainLg.getInstance().saveConfig();
            MainLg.getInstance().loadConfig();
        }, 100);
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        LGGame game = MainLg.getInstance().getCurrentGame();
        event.setMaxPlayers(game.getMaxPlayers());
        if (MainLg.getInstance().isStartGame()) {
            event.setMotd("En jeu");
        } else if (MainLg.getInstance().isEndGame()) {
            event.setMotd("Partie terminé");
        } else if (event.getNumPlayers() >= event.getMaxPlayers()) {
            event.setMotd("En lancement");
        } else {
            event.setMotd("En attente");
        }

    }

    private void setupItems(Player player) {
        if (player.hasPermission("loupgarou.admin")) {
            player.getInventory().setItem(7, new ItemBuilder(Material.TRIPWIRE_HOOK).name("§6Paramètres de la partie").make());
            player.getInventory().setItem(1, new ItemBuilder(Material.LEVER).name("§aLancer la partie").make());
        }
    }

    private ItemStack getItem(String role) {
        int amount = MainLg.getInstance().getConfig().getInt("distributionFixed." + role);
        ItemBuilder item;
        if (amount > 0) {
            item = new ItemBuilder(Material.SKULL_ITEM).durability(3).skullOwner(role).amount(amount);
        } else {
            item = new ItemBuilder(Material.BARRIER);
        }
        item = item.name("§6" + role)
                .lore("§6Places: §e" + amount)
                .lore("§7 ")
                .lore("§7§oClic gauche: +1")
                .lore("§7§oClic droit: -1");
        return item.make();
    }
}
