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

public class GemCommand implements CommandExecutor, TabCompleter {

    private static final List<String> GEM_TYPES = Arrays.asList("fire", "water", "earth", "wind");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /givegem <player> <type>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or offline.");
            return true;
        }

        String gemType = args[1].toLowerCase();
        ItemStack gem = createGem(gemType);

        if (gem == null) {
            sender.sendMessage(ChatColor.RED + "Invalid gem type! Available: fire, water, earth, wind.");
            return true;
        }

        target.getInventory().addItem(gem);
        target.sendMessage(ChatColor.GREEN + "You have received the " + gemType.toUpperCase() + " Gem!");
        sender.sendMessage(ChatColor.GREEN + "Gave " + gemType.toUpperCase() + " Gem to " + target.getName() + ".");

        return true;
    }

    private ItemStack createGem(String type) {
        Material material;
        String name;

        switch (type) {
            case "fire":
                material = Material.MAGMA_CREAM;
                name = ChatColor.RED + "" + ChatColor.BOLD + "Fire Gem";
                break;
            case "water":
                material = Material.HEART_OF_THE_SEA;
                name = ChatColor.AQUA + "" + ChatColor.BOLD + "Water Gem";
                break;
            case "earth":
                material = Material.EMERALD;
                name = ChatColor.GREEN + "" + ChatColor.BOLD + "Earth Gem";
                break;
            case "wind":
                material = Material.FEATHER;
                name = ChatColor.WHITE + "" + ChatColor.BOLD + "Wind Gem";
                break;
            default:
                return null;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Right-Click: Primary Ability",
                ChatColor.GRAY + "Drop Key (Q): Ultimate Ability",
                ChatColor.GRAY + "Sneak + Right-Click: Passive Buff"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return null; // Standard online player list
        } else if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            for (String gem : GEM_TYPES) {
                if (gem.startsWith(args[1].toLowerCase())) {
                    completions.add(gem);
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
