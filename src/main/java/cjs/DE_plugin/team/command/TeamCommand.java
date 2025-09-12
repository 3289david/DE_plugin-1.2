package cjs.DE_plugin.team.command;

import cjs.DE_plugin.gametime.GameTimeManager;
import cjs.DE_plugin.team.Team;
import cjs.DE_plugin.team.TeamManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {

    private final TeamManager teamManager;
    private final GameTimeManager gameTimeManager;

    public TeamCommand(TeamManager teamManager, GameTimeManager gameTimeManager) {
        this.teamManager = teamManager;
        this.gameTimeManager = gameTimeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(player, args);
                break;
            case "join":
                handleJoin(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "_create_confirm": // 색상 선택 UI에서 내부적으로 호출되는 명령어
                handleCreateConfirm(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c사용법: /team create <팀이름>");
            return;
        }
        String teamName = args[1];

        if (teamManager.getTeam(teamName) != null) {
            player.sendMessage("§c이미 존재하는 팀 이름입니다.");
            return;
        }

        player.sendMessage("§a팀 [" + teamName + "]§a의 색상을 선택하세요:");
        sendColorSelectionUI(player, teamName);
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c사용법: /team join <팀이름>");
            return;
        }
        String teamName = args[1];
        Team team = teamManager.getTeam(teamName);

        if (team == null) {
            player.sendMessage("§c존재하지 않는 팀입니다.");
            return;
        }

        teamManager.joinTeam(player, team);
    }

    private void handleLeave(Player player) {
        if (teamManager.getPlayerTeam(player) == null) {
            player.sendMessage("§c소속된 팀이 없습니다.");
            return;
        }
        teamManager.leaveTeam(player);
    }

    private void handleCreateConfirm(Player player, String[] args) {
        if (args.length < 3) return; // 내부 명령어이므로 오류 메시지 없이 종료
        String teamName = args[1];
        try {
            ChatColor color = ChatColor.valueOf(args[2].toUpperCase());
            Team newTeam = teamManager.createTeam(teamName, color);
            player.sendMessage(color + "팀 [" + newTeam.getName() + "]§f이(가) 성공적으로 생성되었습니다!");
            teamManager.joinTeam(player, newTeam); // 생성 후 바로 가입
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c잘못된 색상 코드입니다.");
        }
    }

    private void sendColorSelectionUI(Player player, String teamName) {
        ComponentBuilder builder = new ComponentBuilder();
        ChatColor[] colors1 = {
                ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.AQUA,
                ChatColor.BLUE, ChatColor.LIGHT_PURPLE, ChatColor.WHITE
        };
        ChatColor[] colors2 = {
                ChatColor.DARK_RED, ChatColor.DARK_GREEN, ChatColor.DARK_BLUE, ChatColor.DARK_AQUA,
                ChatColor.DARK_PURPLE, ChatColor.DARK_GRAY, ChatColor.GRAY, ChatColor.BLACK
        };

        for (ChatColor color : colors1) {
            TextComponent colorComponent = new TextComponent("■");
            colorComponent.setColor(net.md_5.bungee.api.ChatColor.valueOf(color.name()));
            colorComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team _create_confirm " + teamName + " " + color.name()));
            colorComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(color + color.name()).create()));
            builder.append(colorComponent).append(" ");
        }
        builder.append("\n"); // 줄바꿈
        for (ChatColor color : colors2) {
            TextComponent colorComponent = new TextComponent("■");
            colorComponent.setColor(net.md_5.bungee.api.ChatColor.valueOf(color.name()));
            colorComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team _create_confirm " + teamName + " " + color.name()));
            colorComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(color + color.name()).create()));
            builder.append(colorComponent).append(" ");
        }
        player.spigot().sendMessage(builder.create());
    }

    private void sendHelp(Player player) {
        player.sendMessage("§e========== 팀 명령어 도움말 ==========");
        player.sendMessage("§6/team create <이름> §f- 새로운 팀을 생성합니다.");
        player.sendMessage("§6/team join <이름> §f- 기존 팀에 가입합니다.");
        player.sendMessage("§6/team leave §f- 현재 팀에서 탈퇴합니다.");
        player.sendMessage("§e===================================");
    }
}