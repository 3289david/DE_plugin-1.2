package cjs.DE_plugin.team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 팀의 정보를 저장하는 데이터 클래스입니다.
 */
public class Team {
    private final String name;
    private final ChatColor color;
    private final Set<UUID> members;
    private final String scoreboardTeamName;

    public Team(String name, ChatColor color) {
        this.name = name;
        this.color = color;
        this.members = new HashSet<>();
        this.scoreboardTeamName = "de_" + UUID.randomUUID().toString().substring(0, 12);
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public String getScoreboardTeamName() {
        return scoreboardTeamName;
    }

    public void addMember(UUID playerUuid) {
        members.add(playerUuid);
    }

    public void removeMember(UUID playerUuid) {
        members.remove(playerUuid);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }
    public List<Player> getOnlineMembers() {
        return members.stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .collect(Collectors.toList());
    }
}