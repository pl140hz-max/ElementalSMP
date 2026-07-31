package com.elementalsmp.gems.commands;

import com.elementalsmp.gems.managers.TrustManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TrustCommands implements CommandExecutor, TabCompleter {

    private final TrustManager trustManager;

    public TrustCommands(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use trust commands!");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        if (cmd.equals("trustlist")) {
            Set<UUID> trusted = trustManager.getTrustedPlayers(player.getUniqueId());
            if (trusted.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "You haven't trusted any players yet.");
                return true;
            }

            List<String> names = trusted.stream()
                    .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            player.sendMessage(ChatColor.GREEN + "--- Trusted Players ---");
            player.sendMessage(ChatColor.AQUA + String.join(", ", names));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /" + cmd + " <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot trust or untrust yourself!");
            return true;
        }

        if (cmd.equals("trust")) {
            boolean added = trustManager.trustPlayer(player.getUniqueId(), target.getUniqueId());
            if (added) {
                player.sendMessage(ChatColor.GREEN + "You have trusted " + target.getName() + "!");
            } else {
                player.sendMessage(ChatColor.RED + "that player is already trusted");
            }
        } else if (cmd.equals("untrust")) {
            boolean removed = trustManager.untrustPlayer(player.getUniqueId(), target.getUniqueId());
            if (removed) {
                player.sendMessage(ChatColor.YELLOW + "You untrusted " + target.getName() + ".");
            } else {
                player.sendMessage(ChatColor.RED + "that player is already untrusted");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && !command.getName().equalsIgnoreCase("trustlist")) {
            return null; // Standard online player name completion
        }
        return Collections.emptyList();
    }
}