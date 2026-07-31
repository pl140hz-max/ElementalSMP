package com.elementalsmp.gems.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GiveGemCommand implements CommandExecutor, TabCompleter {

    private final List<String> GEM_TYPES = Arrays.asList("air", "fire", "water", "earth");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /givegem <player> <air|fire|water|earth>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        String gemType = args[1].toLowerCase();
        ItemStack gem = createGem(gemType);

        if (gem == null) {
            sender.sendMessage(ChatColor.RED + "Invalid gem type! Choose: air, fire, water, or earth.");
            return true;
        }

        target.getInventory().addItem(gem);
        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " a " + gem.getItemMeta().getDisplayName());
        return true;
    }

    private ItemStack createGem(String type) {
        ItemStack item;
        ItemMeta meta;

        switch (type) {
            case "air":
            case "wind":
                item = new ItemStack(Material.FEATHER);
                meta = item.getItemMeta();
                if (meta != null) meta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + "Air Gem");
                break;
            case "fire":
                item = new ItemStack(Material.FIRE_CHARGE);
                meta = item.getItemMeta();
                if (meta != null) meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Fire Gem");
                break;
            case "water":
                item = new ItemStack(Material.HEART_OF_THE_SEA);
                meta = item.getItemMeta();
                if (meta != null) meta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Water Gem");
                break;
            case "earth":
                item = new ItemStack(Material.EMERALD);
                meta = item.getItemMeta();
                if (meta != null) meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Earth Gem");
                break;
            default:
                return null;
        }

        item.setItemMeta(meta);
        return item;
    }

    // --- Enables Tab Auto-Completion in Chat ---
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Auto-completes player names for argument 1
            return null; 
        } else if (args.length == 2) {
            // Auto-completes gem names for argument 2
            List<String> suggestions = new ArrayList<>();
            for (String gem : GEM_TYPES) {
                if (gem.startsWith(args[1].toLowerCase())) {
                    suggestions.add(gem);
                }
            }
            return suggestions;
        }
        return new ArrayList<>();
    }
}
