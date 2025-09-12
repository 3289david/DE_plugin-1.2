package cjs.DE_plugin.tracker;

import cjs.DE_plugin.DE_plugin;
import cjs.DE_plugin.settings.SettingsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TrackerManager {

    private final DE_plugin plugin;
    private final SettingsManager sm;
    private final NamespacedKey trackerKey;
    private final NamespacedKey creationTimeKey;
    private final NamespacedKey activationTimeKey;

    public TrackerManager(DE_plugin plugin) {
        this.plugin = plugin;
        this.sm = plugin.getSettingsManager();
        this.trackerKey = new NamespacedKey(plugin, "dragon_egg_tracker");
        this.creationTimeKey = new NamespacedKey(plugin, "tracker_creation_time");
        this.activationTimeKey = new NamespacedKey(plugin, "tracker_activation_time");
        startTrackerUpdateTask();
    }

    public DE_plugin getPlugin() {
        return plugin;
    }

    public boolean isTracker(ItemStack item) {
        if (item == null || item.getType() != Material.RECOVERY_COMPASS || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(trackerKey, PersistentDataType.BYTE);
    }

    public void createTracker(ItemStack item) {
        if (item == null || item.getType() != Material.RECOVERY_COMPASS) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(Material.RECOVERY_COMPASS);
        }

        meta.getPersistentDataContainer().set(trackerKey, PersistentDataType.BYTE, (byte) 1);

        meta.displayName(Component.text("드래곤 알 추적기", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));

        item.setItemMeta(meta);
        updateTrackerLore(item);
    }

    public void activateTracker(ItemStack item) {
        if (!isTracker(item) || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // 이미 활성화되어 있으면 무시
        if (pdc.has(activationTimeKey, PersistentDataType.LONG)) {
            return;
        }

        pdc.set(activationTimeKey, PersistentDataType.LONG, System.currentTimeMillis());
        item.setItemMeta(meta);
        updateTrackerLore(item);
    }


    private void startTrackerUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!sm.getBoolean(SettingsManager.TRACKER_ENABLED)) {
                    return;
                }

                Location eggLocation = findEggLocation();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (isTracker(item) && isTrackerActive(item)) {
                            updateTracker(player, item, eggLocation);
                        }
                    }
                    // 손에 들고 있는 아이템도 확인
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    if (isTracker(mainHand)) {
                        updateTracker(player, mainHand, eggLocation);
                    }
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    if (isTracker(offHand)) {
                        updateTracker(player, offHand, eggLocation);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 1초마다 실행
    }

    private void updateTracker(Player player, ItemStack tracker, Location eggLocation) {
        if (tracker == null || !(tracker.getItemMeta() instanceof CompassMeta meta)) {
            return;
        }

        // 활성화되지 않은 추적기는 업데이트하지 않음 (로어만 업데이트)
        if (!isTrackerActive(tracker)) {
            updateTrackerLore(tracker);
            // 활성화되지 않은 추적기는 플레이어의 나침반 목표를 초기화할 수 있음
            if (player.getCompassTarget().equals(meta.getLodestone())) {
                player.setCompassTarget(player.getWorld().getSpawnLocation());
            }
            return;
        }

        // 수명 확인 및 내구도 감소
        long activationTime = meta.getPersistentDataContainer().getOrDefault(activationTimeKey, PersistentDataType.LONG, 0L);
        long lifetimeMillis = sm.getInt(SettingsManager.TRACKER_LIFETIME_MINUTES) * 60 * 1000L;
        long elapsedTime = System.currentTimeMillis() - activationTime;

        if (elapsedTime >= lifetimeMillis) {
            tracker.setAmount(0); // 아이템 파괴
            player.sendMessage("§c추적기의 수명이 다했습니다.");
            return;
        }

        // 나침반 목표 설정
        if (eggLocation != null) {
            meta.setLodestone(eggLocation);
            meta.setLodestoneTracked(false);
            player.setCompassTarget(eggLocation); // 플레이어의 개인 나침반 목표도 설정
        } else {
            // 알을 찾을 수 없을 때 (예: 엔더 월드)
            meta.setLodestone(null);
        }
        tracker.setItemMeta(meta);
        updateTrackerLore(tracker);
    }

    private void updateTrackerLore(ItemStack item) {
        if (!isTracker(item)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("드래곤 알의 위치를 가리킵니다.", NamedTextColor.GRAY));

        if (isTrackerActive(item)) {
            long activationTime = meta.getPersistentDataContainer().getOrDefault(activationTimeKey, PersistentDataType.LONG, 0L);
            long lifetimeMinutes = sm.getInt(SettingsManager.TRACKER_LIFETIME_MINUTES);
            long lifetimeMillis = lifetimeMinutes * 60 * 1000L;
            long elapsedTime = System.currentTimeMillis() - activationTime;
            long remainingMillis = lifetimeMillis - elapsedTime;
            long remainingSeconds = Math.max(0, remainingMillis / 1000);

            lore.add(Component.text("남은 수명: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%d분 %d초", remainingSeconds / 60, remainingSeconds % 60), NamedTextColor.YELLOW)));
        } else {
            lore.add(Component.text("우클릭하여 활성화", NamedTextColor.GREEN));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private boolean isTrackerActive(ItemStack item) {
        if (!isTracker(item) || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(activationTimeKey, PersistentDataType.LONG);
    }

    private Location findEggLocation() {
        // 1. 설치된 알 위치 확인
        Location placedEggLoc = plugin.getPlacedEggManager().getPlacedEggLocation();
        if (placedEggLoc != null) {
            return placedEggLoc;
        }

        // 2. 플레이어가 들고 있는 알 위치 확인
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getInventory().contains(Material.DRAGON_EGG) ||
                    p.getInventory().getItemInOffHand().getType() == Material.DRAGON_EGG ||
                    p.getItemOnCursor().getType() == Material.DRAGON_EGG) {
                return p.getLocation();
            }
        }
        return null;
    }
}