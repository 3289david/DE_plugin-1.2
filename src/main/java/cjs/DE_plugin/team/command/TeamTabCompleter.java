package cjs.DE_plugin.team.command;

import cjs.DE_plugin.team.Team;
import cjs.DE_plugin.team.TeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TeamTabCompleter implements TabCompleter {

    private final TeamManager teamManager;
    private static final List<String> SUB_COMMANDS = Arrays.asList("create", "join", "leave", "tp");

    public TeamTabCompleter(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // /team <subcommand>
            StringUtil.copyPartialMatches(args[0], SUB_COMMANDS, completions);
        } else if (args.length == 2) {
            // /team join <team_name>
            if (args[0].equalsIgnoreCase("join")) {
                List<String> teamNames = teamManager.getAllTeamNames();
                StringUtil.copyPartialMatches(args[1], teamNames, completions);
            } else if (args[0].equalsIgnoreCase("tp") && sender instanceof Player player) {
                // /team tp <member_name>
                Team team = teamManager.getPlayerTeam(player);
                if (team != null) {
                    List<String> memberNames = team.getOnlineMembers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList());
                    StringUtil.copyPartialMatches(args[1], memberNames, completions);
                }
            }
        }

        return completions;
    }
}