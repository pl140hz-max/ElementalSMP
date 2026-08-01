package com.elementalsmp.gems.commands;

import com.elementalsmp.gems.managers.TrustManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class TrustCommand implements CommandExecutor {

    private final TrustManager trustManager;

    public TrustCommand(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can execute trust commands.");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        if (cmd.equals("trust")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /trust <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer not found or offline.");
                return true;
            }
            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage("§cYou cannot trust yourself!");
                return true;
            }

            trustManager.trustPlayer(player.getUniqueId(), target.getUniqueId());
            player.sendMessage("§aYou have trusted " + target.getName() + "! They won't take damage from your gem abilities.");
            return true;

        } else if (cmd.equals("untrust")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /untrust <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer not found or offline.");
                return true;
            }

            trustManager.untrustPlayer(player.getUniqueId(), target.getUniqueId());
            player.sendMessage("§cYou have untrusted " + target.getName() + ".");
            return true;

        } else if (cmd.equals("trustlist")) {
            Set<UUID> trustedList = trustManager.getTrusted(player.getUniqueId());

            if (trustedList == null || trustedList.isEmpty()) {
                player.sendMessage("§eYou do not have any trusted players.");
                return true;
            }

            player.sendMessage("§a--- Trusted Players ---");
            for (UUID uuid : trustedList) {
                OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(uuid);
                player.sendMessage("§7- §f" + (trustedPlayer.getName() != null ? trustedPlayer.getName() : "Unknown Player"));
            }
            return true;
        }

        return false;
    }
}
