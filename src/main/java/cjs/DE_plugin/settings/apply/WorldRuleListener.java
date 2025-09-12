package cjs.DE_plugin.settings.apply;

import cjs.DE_plugin.settings.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldRuleListener implements Listener {

    private final SettingsManager sm;

    public WorldRuleListener(SettingsManager settingsManager) {
        this.sm = settingsManager;
    }

    public void applyAllWorldRules() {
        for (World world : Bukkit.getWorlds()) {
            applyRulesToWorld(world);
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        applyRulesToWorld(event.getWorld());
    }

    private void applyRulesToWorld(World world) {
        // HIDE_COORDINATES 설정은 reducedDebugInfo 게임 규칙을 제어합니다.
        world.setGameRule(GameRule.REDUCED_DEBUG_INFO, sm.getBoolean(SettingsManager.HIDE_COORDINATES));

        world.setGameRule(GameRule.LOCATOR_BAR, !sm.getBoolean(SettingsManager.LOCATION_BAR_DISABLED));

        // [추가] 발전과제 숨기기 설정 적용
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, !sm.getBoolean(SettingsManager.HIDE_ADVANCEMENTS));

        // [추가] 킬로그 비활성화 설정 적용
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, !sm.getBoolean(SettingsManager.KILL_LOG_DISABLED));
    }
}
