package cjs.DE_plugin.tracker;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class TrackerListener implements Listener {

    private final TrackerManager trackerManager;
    private final NamespacedKey trackerRecipeKey;

    public TrackerListener(TrackerManager trackerManager) {
        this.trackerManager = trackerManager;
        this.trackerRecipeKey = new NamespacedKey(trackerManager.getPlugin(), "custom_dragon_egg_tracker");
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null || event.getRecipe().getResult().getType() != Material.RECOVERY_COMPASS) {
            return;
        }

        // 우리 플러그인의 커스텀 레시피인지 확인
        if (event.getRecipe() instanceof org.bukkit.inventory.ShapedRecipe shapedRecipe) {
            if (shapedRecipe.getKey().equals(trackerRecipeKey)) {
                ItemStack result = new ItemStack(Material.RECOVERY_COMPASS);
                trackerManager.createTracker(result);
                event.getInventory().setResult(result);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }

        ItemStack item = event.getItem();
        if (trackerManager.isTracker(item)) {
            trackerManager.activateTracker(item);
        }
    }
}