package com.gems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

public class GemListener implements Listener {

    private final GemsPlugin plugin;
    private final Random random = new Random();

    public GemListener(GemsPlugin plugin) {
        this.plugin = plugin;

        // Loop de efeitos passivos (Roda a cada 1 segundo)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                applyPassives(player);
            }
        }, 0L, 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 1. Forçar download do Resource Pack
        if (!plugin.getResourcePackUrl().isEmpty()) {
            player.setResourcePack(plugin.getResourcePackUrl());
        }

        // 2. Primeiro acesso: Entrega uma gema aleatória
        if (!player.hasPlayedBefore()) {
            GemType[] gems = GemType.values();
            GemType randomGem = gems[random.nextInt(gems.length)];
            player.getInventory().addItem(randomGem.createItem(plugin));
            player.sendMessage(Component.text("Welcome! You have been granted the " + randomGem.name() + " Gem!", NamedTextColor.GOLD));
        }
    }

    // Habilidades Passivas
    private void applyPassives(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            GemType gem = GemType.fromItem(plugin, item);
            if (gem == null) continue;

            switch (gem) {
                case FIRE -> player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, false, false));
                case WATER -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
                    if (player.isInWater()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));
                    }
                }
                case EARTH -> player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, false, false));
                case WIND -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, false, false));
                }
            }
        }
    }

    // Habilidades Ativas: Clique Direito e Shift + Clique Direito
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        GemType gem = GemType.fromItem(plugin, item);
        if (gem == null) return;

        event.setCancelled(true);

        if (player.isSneaking()) {
            // Ação de Shift + Clique Direito
            switch (gem) {
                case FIRE -> {
                    player.getWorld().createExplosion(player.getLocation(), 0.0f, false, false);
                    for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                        if (entity instanceof LivingEntity target) target.setFireTicks(100);
                    }
                    player.sendMessage(Component.text("Fire Nova Activated!", NamedTextColor.RED));
                }
                case WATER -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);
                    player.sendMessage(Component.text("Healing Wave Activated!", NamedTextColor.AQUA));
                }
                case EARTH -> {
                    for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                        if (entity instanceof LivingEntity target) {
                            target.setVelocity(new Vector(0, 1.2, 0));
                        }
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.0f, 0.5f);
                    player.sendMessage(Component.text("Ground Slam Activated!", NamedTextColor.GREEN));
                }
                case WIND -> {
                    player.setVelocity(new Vector(0, 1.5, 0));
                    player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.0f);
                    player.sendMessage(Component.text("Wind Burst Activated!", NamedTextColor.WHITE));
                }
            }
        } else {
            // Ação de Clique Direito Comum
            switch (gem) {
                case FIRE -> player.launchProjectiles(Fireball.class);
                case WATER -> player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(0.2));
                case EARTH -> player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
                case WIND -> player.setVelocity(player.getLocation().getDirection().multiply(2.0));
            }
        }
    }

    // Habilidade Ativa: Tecla de Dropar (Q)
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        GemType gem = GemType.fromItem(plugin, item);
        if (gem == null) return;

        event.setCancelled(true); // Impede o item de cair no chão
        Player player = event.getPlayer();

        switch (gem) {
            case FIRE -> {
                for (Entity entity : player.getNearbyEntities(4, 4, 4)) {
                    entity.setFireTicks(60);
                }
                player.sendMessage(Component.text("Flame Ring Triggered!", NamedTextColor.RED));
            }
            case WATER -> {
                player.setFireTicks(0);
                for (Entity entity : player.getNearbyEntities(4, 4, 4)) {
                    entity.setVelocity(entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5));
                }
                player.sendMessage(Component.text("Water Blast Triggered!", NamedTextColor.AQUA));
            }
            case EARTH -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 200, 1));
                player.sendMessage(Component.text("Earth Power Triggered!", NamedTextColor.GREEN));
            }
            case WIND -> {
                for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                    entity.setVelocity(entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(2.0));
                }
                player.sendMessage(Component.text("Wind Gust Triggered!", NamedTextColor.WHITE));
            }
        }
    }

    // Proteção de Container (Impede guardar em baús, mochilas, bundles, etc.)
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean currentIsGem = GemType.fromItem(plugin, current) != null;
        boolean cursorIsGem = GemType.fromItem(plugin, cursor) != null;

        if (!currentIsGem && !cursorIsGem) return;

        InventoryType type = event.getInventory().getType();

        // Bloqueia colocar em inventários externos (Baús, Shulkers, Funis, etc.)
        if (type != InventoryType.CRAFTING && type != InventoryType.PLAYER) {
            event.setCancelled(true);
            player.sendMessage(Component.text("you can place gems into a container", NamedTextColor.RED));
            return;
        }

        // Bloqueia colocar em Bundles dentro do próprio inventário
        if (event.getClick() == ClickType.RIGHT && current != null && current.getType() == Material.BUNDLE && cursorIsGem) {
            event.setCancelled(true);
            player.sendMessage(Component.text("you can place gems into a container", NamedTextColor.RED));
        }
    }
}