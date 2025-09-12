package cjs.DE_plugin.team;

import cjs.DE_plugin.DE_plugin;
import cjs.DE_plugin.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

public class TeamManager {

    private final Map<String, Team> teams = new HashMap<>();
    private final Map<UUID, Team> playerTeams = new HashMap<>();
    private final PlayerListener playerListener;
    private final DE_plugin plugin;
    private final Scoreboard scoreboard;

    public TeamManager(DE_plugin plugin, PlayerListener playerListener, Scoreboard scoreboard) {
        this.plugin = plugin;
        this.playerListener = playerListener;
        this.scoreboard = scoreboard;
        // 스코어보드 팀 정리 (서버 리로드 시 남아있는 팀 제거)
        scoreboard.getTeams().forEach(sbTeam -> {
            if (sbTeam.getName().startsWith("de_")) {
                sbTeam.unregister();
            }
        });
    }

    public Team createTeam(String name, ChatColor color) {
        Team team = new Team(name, color);
        teams.put(name.toLowerCase(), team);

        // 스코어보드에 팀 등록
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.registerNewTeam(team.getScoreboardTeamName());
        scoreboardTeam.setPrefix(team.getColor() + "[" + team.getName() + "] ");
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setColor(team.getColor());

        return team;
    }

    public void joinTeam(Player player, Team team) {
        // 기존 팀에서 나가기
        leaveTeam(player);

        team.addMember(player.getUniqueId());
        playerTeams.put(player.getUniqueId(), team);
        player.sendMessage(team.getColor() + "[" + team.getName() + "]§f 팀에 가입했습니다.");

        // 스코어보드 팀에 플레이어 추가
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(team.getScoreboardTeamName());
        if (scoreboardTeam != null) {
            scoreboardTeam.addEntry(player.getName());
        }

        // 팀원들에게 알림
        team.getOnlineMembers().forEach(member -> {
            if (!member.equals(player)) {
                member.sendMessage(team.getColor() + player.getName() + "§f님이 팀에 참여했습니다.");
            }
        });

        // 이름표 업데이트
        if (playerListener != null) {
            playerListener.updatePlayerDisplayName(player);
        }
    }

    public void leaveTeam(Player player) {
        Team team = getPlayerTeam(player);
        if (team == null) return;

        team.removeMember(player.getUniqueId());
        playerTeams.remove(player.getUniqueId());
        player.sendMessage(team.getColor() + "[" + team.getName() + "]§f 팀에서 탈퇴했습니다.");

        // 스코어보드 팀에서 플레이어 제거
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(team.getScoreboardTeamName());
        if (scoreboardTeam != null) {
            scoreboardTeam.removeEntry(player.getName());
        }

        // 이름표 업데이트
        if (playerListener != null) {
            playerListener.updatePlayerDisplayName(player);
        }

        // 팀원이 0명이면 팀 삭제
        if (team.getMembers().isEmpty()) {
            teams.remove(team.getName().toLowerCase());
            // 스코어보드에서 팀 제거
            org.bukkit.scoreboard.Team sbTeamToUnregister = scoreboard.getTeam(team.getScoreboardTeamName());
            if (sbTeamToUnregister != null) {
                sbTeamToUnregister.unregister();
            }
            Bukkit.broadcastMessage(team.getColor() + "[" + team.getName() + "]§f 팀이 해체되었습니다.");
        }
    }

    public Team getTeam(String name) {
        return teams.get(name.toLowerCase());
    }

    public Team getPlayerTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    public List<String> getAllTeamNames() {
        return teams.values().stream().map(Team::getName).collect(Collectors.toList());
    }
}