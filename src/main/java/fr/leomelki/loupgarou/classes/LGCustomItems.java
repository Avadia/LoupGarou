package fr.leomelki.loupgarou.classes;

import fr.leomelki.fr.farmvivi.avadia.ItemBuilder;
import fr.leomelki.loupgarou.MainLg;
import fr.leomelki.loupgarou.events.LGCustomItemChangeEvent;
import fr.leomelki.loupgarou.roles.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class LGCustomItems {
    private static final Map<String, Map<String, Short>> mappings = new HashMap<>();

    public static short getItem(Role role) {
        return getItem(role, new ArrayList<>());
    }

    public static short getItem(LGPlayer player, List<LGCustomItemsConstraints> constraints) {
        Bukkit.getPluginManager().callEvent(new LGCustomItemChangeEvent(player.getGame(), player, constraints));

        return getItem(player.getRole(), constraints);
    }

    public static short getItem(LGPlayer player) {
        return getItem(player, new ArrayList<>());
    }

    public static void updateItem(LGPlayer lgp) {
        lgp.getPlayer().getInventory().setItemInOffHand(new ItemBuilder(Material.MAP).name("").durability(getItem(lgp)).make());
        lgp.getPlayer().updateInventory();
    }

    public static void updateItem(LGPlayer lgp, List<LGCustomItemsConstraints> constraints) {
        lgp.getPlayer().getInventory().setItemInOffHand(new ItemBuilder(Material.MAP).name("").durability(getItem(lgp, constraints)).make());
        lgp.getPlayer().updateInventory();
    }

    public static void initRole(String name) throws IOException {
        Map<String, Short> maps = new HashMap<>();
        List<LGCustomItemsConstraints> constraints = new ArrayList<>();
        initRole(maps, name, constraints);
        constraints.clear();
        constraints.add(LGCustomItemsConstraints.DEAD);
        initRole(maps, name, constraints);

        constraints.clear();
        constraints.add(LGCustomItemsConstraints.MAYOR);
        initRole(maps, name, constraints);
        constraints.clear();
        constraints.add(LGCustomItemsConstraints.MAYOR);
        constraints.add(LGCustomItemsConstraints.DEAD);
        initRole(maps, name, constraints);

        constraints.clear();
        constraints.add(LGCustomItemsConstraints.INFECTED);
        initRole(maps, name, constraints);
        constraints.clear();
        constraints.add(LGCustomItemsConstraints.INFECTED);
        constraints.add(LGCustomItemsConstraints.DEAD);
        initRole(maps, name, constraints);

        constraints.clear();
        constraints.add(LGCustomItemsConstraints.VAMPIRE_INFECTE);
        initRole(maps, name, constraints);
        constraints.clear();
        constraints.add(LGCustomItemsConstraints.VAMPIRE_INFECTE);
        constraints.add(LGCustomItemsConstraints.DEAD);
        initRole(maps, name, constraints);

        constraints.clear();
        constraints.add(LGCustomItemsConstraints.INFECTED);
        constraints.add(LGCustomItemsConstraints.MAYOR);
        initRole(maps, name, constraints);
        constraints.clear();
        constraints.add(LGCustomItemsConstraints.INFECTED);
        constraints.add(LGCustomItemsConstraints.MAYOR);
        constraints.add(LGCustomItemsConstraints.DEAD);
        initRole(maps, name, constraints);

        constraints.clear();
        constraints.add(LGCustomItemsConstraints.VAMPIRE_INFECTE);
        constraints.add(LGCustomItemsConstraints.MAYOR);
        initRole(maps, name, constraints);
        constraints.clear();
        constraints.add(LGCustomItemsConstraints.VAMPIRE_INFECTE);
        constraints.add(LGCustomItemsConstraints.MAYOR);
        constraints.add(LGCustomItemsConstraints.DEAD);
        initRole(maps, name, constraints);
        mappings.put(name, maps);
    }

    private static void initRole(Map<String, Short> maps, String name, List<LGCustomItemsConstraints> constraints) throws IOException {
        Collections.sort(constraints);

        StringJoiner sj = new StringJoiner("_");
        for (LGCustomItemsConstraints s : constraints)
            sj.add(s.getName());

        String itemName = sj.toString();

        MapView mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
        BufferedImage role_image = ImageIO.read(new File(MainLg.getInstance().getDataFolder(), "roles" + File.separator + name + ".png"));
        BufferedImage mort_image = ImageIO.read(new File(MainLg.getInstance().getDataFolder(), "overlays" + File.separator + "mort.png"));
        BufferedImage infecte_image = ImageIO.read(new File(MainLg.getInstance().getDataFolder(), "overlays" + File.separator + "infecte.png"));
        BufferedImage maire_image = ImageIO.read(new File(MainLg.getInstance().getDataFolder(), "overlays" + File.separator + "maire.png"));
        BufferedImage vampire_infecte_image = ImageIO.read(new File(MainLg.getInstance().getDataFolder(), "overlays" + File.separator + "vampire-infecte.png"));
        BufferedImage combined_image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

        Graphics g = combined_image.getGraphics();
        g.drawImage(role_image, 0, 0, null);
        if (constraints.contains(LGCustomItemsConstraints.DEAD))
            g.drawImage(mort_image, 0, 0, null);
        if (constraints.contains(LGCustomItemsConstraints.INFECTED))
            g.drawImage(infecte_image, 0, 0, null);
        if (constraints.contains(LGCustomItemsConstraints.MAYOR))
            g.drawImage(maire_image, 0, 0, null);
        if (constraints.contains(LGCustomItemsConstraints.VAMPIRE_INFECTE))
            g.drawImage(vampire_infecte_image, 0, 0, null);

        g.dispose();
        role_image.flush();
        mort_image.flush();
        infecte_image.flush();
        maire_image.flush();
        vampire_infecte_image.flush();
        mapView.getRenderers().forEach(mapView::removeRenderer);
        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(0, 0, combined_image);
            }
        });
        combined_image.flush();
        maps.put(itemName, mapView.getId());
    }

    public static synchronized short getItem(Role role, List<LGCustomItemsConstraints> constraints) {
        Collections.sort(constraints);

        if (!mappings.containsKey(role.getRawName())) {
            Map<String, Short> map = new HashMap<>();
            mappings.put(role.getRawName(), map);
        }

        Map<String, Short> mapps = mappings.get(role.getRawName());

        StringJoiner sj = new StringJoiner("_");
        for (LGCustomItemsConstraints s : constraints)
            sj.add(s.getName());

        String itemName = sj.toString();

        if (mapps.containsKey(itemName)) {
            return mapps.get(itemName);
        } else {
            MapView mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
            Bukkit.getScheduler().runTaskAsynchronously(MainLg.getInstance(), () -> {
                try {
                    BufferedImage role_image = ImageIO.read(new File(MainLg.getInstance().getDataFolder(), "roles" + File.separator + role.getRawName() + ".png"));
                    BufferedImage mort_image = ImageIO.read(new File(MainLg.getInstance().getDataFolder(), "overlays" + File.separator + "mort.png"));
                    BufferedImage infecte_image = ImageIO.read(new File(MainLg.getInstance().getDataFolder(), "overlays" + File.separator + "infecte.png"));
                    BufferedImage maire_image = ImageIO.read(new File(MainLg.getInstance().getDataFolder(), "overlays" + File.separator + "maire.png"));
                    BufferedImage vampire_infecte_image = ImageIO.read(new File(MainLg.getInstance().getDataFolder(), "overlays" + File.separator + "vampire-infecte.png"));
                    BufferedImage combined_image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

                    Graphics g = combined_image.getGraphics();
                    g.drawImage(role_image, 0, 0, null);
                    if (constraints.contains(LGCustomItemsConstraints.DEAD))
                        g.drawImage(mort_image, 0, 0, null);
                    if (constraints.contains(LGCustomItemsConstraints.INFECTED))
                        g.drawImage(infecte_image, 0, 0, null);
                    if (constraints.contains(LGCustomItemsConstraints.MAYOR))
                        g.drawImage(maire_image, 0, 0, null);
                    if (constraints.contains(LGCustomItemsConstraints.VAMPIRE_INFECTE))
                        g.drawImage(vampire_infecte_image, 0, 0, null);

                    g.dispose();
                    mapView.getRenderers().forEach(mapView::removeRenderer);
                    mapView.addRenderer(new MapRenderer() {
                        @Override
                        public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                            mapCanvas.drawImage(0, 0, combined_image);
                        }
                    });
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });
            mapps.put(itemName, mapView.getId());
            return mapView.getId();
        }
    }

    @RequiredArgsConstructor
    public enum LGCustomItemsConstraints {
        INFECTED("infecte"),
        MAYOR("maire"),
        VAMPIRE_INFECTE("vampire-infecte"),
        DEAD("mort");

        @Getter
        private final String name;
    }
}
