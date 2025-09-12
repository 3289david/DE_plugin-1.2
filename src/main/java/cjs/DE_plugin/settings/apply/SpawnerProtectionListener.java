package cjs.DE_plugin.settings.apply;

import cjs.DE_plugin.settings.SettingsManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SpawnerProtectionListener implements Listener {

    private final SettingsManager sm;

    public SpawnerProtectionListener(SettingsManager settingsManager) {
        this.sm = settingsManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.SPAWNER) {
            if (sm.getBoolean(SettingsManager.SPAWNER_PROTECTION_ENABLED)) {
                Player player = event.getPlayer();
                // 관리자 권한이 있는 플레이어는 파괴 가능
                if (!player.hasPermission("de.admin.bypass.spawner")) {
                    event.setCancelled(true);
                    player.sendMessage("§c스포너는 파괴할 수 없습니다.");
                }
            }
        }
    }
}