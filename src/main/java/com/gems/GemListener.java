package com.gems;

import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class GemListener implements Listener {

    private final GemsPlugin plugin;

    public GemListener(GemsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String packUrl = plugin.getResourcePackUrl();

        // Only prompts for resource pack if a valid URL is provided
        if (packUrl != null && !packUrl.isEmpty()) {
            player.setResourcePack(packUrl);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) return;

        GemType gem = GemType.fromItemStack(item);
        if (gem == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (gem == GemType.FIRE) {
                player.launchProjectile(Fireball.class);
            }
        }
    }
}
