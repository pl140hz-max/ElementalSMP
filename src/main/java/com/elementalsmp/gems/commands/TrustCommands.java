package com.elementalsmp.gems.commands;

import com.elementalsmp.gems.managers.TrustManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TrustCommands implements CommandExecutor, TabCompleter {

    private final TrustManager trustManager;

    public TrustCommands(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use trust commands!");
            return true;
        }

        String cmdName = command.getName().toLowerCase();

        if (cmdName.equals("trust")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /trust <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer not found!");
                return true;
            }
            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage("§cYou cannot trust yourself!");
                return true;
            }

            trustManager.trustPlayer(player.getUniqueId(), target.getUniqueId());
            player.sendMessage("§aYou have trusted " + target.getName() + "!");
            target.sendMessage("§a" + player.getName() + " has trusted you!");
            return true;
        }

        if (cmdName.equals("untrust")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /untrust <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer not found!");
                return true;
            }

            trustManager.untrustPlayer(player.getUniqueId(), target.getUniqueId());
            player.sendMessage("§cYou have untrusted " + target.getName() + ".");
            return true;
        }

        if (cmdName.equals("trustlist")) {
            Set<UUID> trustedSet = trustManager.getTrustedPlayers(player.getUniqueId());
            if (trustedSet == null || trustedSet.isEmpty()) {
                player.sendMessage("§eYou do not have any trusted players.");
                return true;
            }

            List<String> names = new ArrayList<>();
            for (UUID uuid : trustedSet) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    names.add(p.getName());
                } else {
                    names.add(Bukkit.getOfflinePlayer(uuid).getName());
                }
            }

            player.sendMessage("§aTrusted Players: §f" + String.join(", ", names));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && (command.getName().equalsIgnoreCase("trust") || command.getName().equalsIgnoreCase("untrust"))) {
            List<String> playerNames = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().equalsIgnoreCase(sender.getName())) {
                    playerNames.add(p.getName());
                }
            }
            return playerNames;
        }
        return List.of();
    }
}
