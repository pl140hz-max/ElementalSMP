package com.elementalsmp.gems.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class GemListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        String name = item.getItemMeta().getDisplayName();

        // Check if player is sneaking (Sneak + Right Click)
        if (player.isSneaking()) {
            if (name.contains("Fire Gem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0));
                player.sendMessage(ChatColor.GOLD + "Activated Fire Aura!");
            } else if (name.contains("Water Gem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 200, 0));
                player.sendMessage(ChatColor.AQUA + "Activated Ocean Blessing!");
            } else if (name.contains("Earth Gem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
                player.sendMessage(ChatColor.GREEN + "Activated Earth Shield!");
            } else if (name.contains("Wind Gem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                player.sendMessage(ChatColor.WHITE + "Activated Wind Sprint!");
            }
            return;
        }

        // Regular Right-Click Abilities
        if (name.contains("Fire Gem")) {
            player.launchProjectile(Fireball.class); // CORRIGIDO AQUI!
            player.sendMessage(ChatColor.RED + "Fireball launched!");
        } else if (name.contains("Water Gem")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
            player.sendMessage(ChatColor.AQUA + "Healed by Water Essence!");
        } else if (name.contains("Earth Gem")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 300, 1));
            player.sendMessage(ChatColor.GREEN + "Gained Earth Barrier!");
        } else if (name.contains("Wind Gem")) {
            Vector jump = player.getLocation().getDirection().multiply(1.5).setY(1.0);
            player.setVelocity(jump);
            player.sendMessage(ChatColor.WHITE + "Wind Dash!");
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = item.getItemMeta().getDisplayName();

            if (name.contains("Gem")) {
                event.setCancelled(true); // Prevents dropping the gem item
                Player player = event.getPlayer();

                if (name.contains("Fire Gem")) {
                    player.getWorld().createExplosion(player.getLocation(), 2.0f, false, false);
                    player.sendMessage(ChatColor.DARK_RED + "Fire Nova unleashed!");
                } else if (name.contains("Water Gem")) {
                    player.getWorld().getNearbyEntities(player.getLocation(), 5, 5, 5).forEach(e -> {
                        if (e != player) e.setFreezeTicks(100);
                    });
                    player.sendMessage(ChatColor.DARK_AQUA + "Absolute Zero freeze applied!");
                } else if (name.contains("Earth Gem")) {
                    player.getWorld().spawnFallingBlock(player.getLocation().add(0, 3, 0), Material.DIRT.createBlockData());
                    player.sendMessage(ChatColor.DARK_GREEN + "Earthquake summoned!");
                } else if (name.contains("Wind Gem")) {
                    player.getWorld().getNearbyEntities(player.getLocation(), 6, 6, 6).forEach(e -> {
                        if (e != player) e.setVelocity(new Vector(0, 1.5, 0));
                    });
                    player.sendMessage(ChatColor.GRAY + "Tornado Blast launched enemies upward!");
                }
            }
        }
    }
}
