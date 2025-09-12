package cjs.DE_plugin.listeners;

import cjs.DE_plugin.DE_plugin;
import cjs.DE_plugin.team.Team;
import cjs.DE_plugin.team.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final DE_plugin plugin;
    private TeamManager teamManager;

    public PlayerListener(DE_plugin plugin) {
        this.plugin = plugin;
    }

    private TeamManager getTeamManager() {
        if (this.teamManager == null) {
            this.teamManager = plugin.getTeamManager();
        }
        return this.teamManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayerDisplayName(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 플레이어가 나가면 다른 플레이어에게는 자동으로 이름표가 업데이트되므로 별도 처리가 필요 없을 수 있습니다.
        // 하지만 만약을 위해 남겨둡니다.
    }

    public void updatePlayerDisplayName(Player player) {
        Team team = getTeamManager().getPlayerTeam(player);
        if (team != null) {
            Component teamPrefix = Component.text("[" + team.getName() + "] ", NamedTextColor.NAMES.value(team.getColor().name().toLowerCase()));
            player.displayName(teamPrefix.append(Component.text(player.getName())));
            player.playerListName(teamPrefix.append(Component.text(player.getName())));
        } else {
            // 팀이 없으면 기본 이름으로 설정
            player.displayName(Component.text(player.getName()));
            player.playerListName(Component.text(player.getName()));
        }
    }
}
