package cjs.DE_plugin.settings.apply;

import cjs.DE_plugin.settings.SettingsManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BannedItemsListener implements Listener {

    private final SettingsManager sm;

    public BannedItemsListener(SettingsManager settingsManager) {
        this.sm = settingsManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        // 삼지창 던지기 방지
        if (item.getType() == Material.TRIDENT && sm.getBoolean(SettingsManager.TRIDENT_BANNED)) {
            if (event.getAction().name().contains("RIGHT_CLICK")) {
                player.sendMessage("§c삼지창은 사용할 수 없습니다.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        // 삼지창 근접 공격 방지
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.TRIDENT && sm.getBoolean(SettingsManager.TRIDENT_BANNED)) {
            player.sendMessage("§c삼지창은 사용할 수 없습니다.");
            event.setCancelled(true);
        }
    }
}