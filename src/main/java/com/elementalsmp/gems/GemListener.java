package com.elementalsmp.gems.listeners;

import com.elementalsmp.gems.managers.TrustManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class GemListener implements Listener {

    private final JavaPlugin plugin;
    private final TrustManager trustManager;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> activeTridents = new HashMap<>();

    public GemListener(JavaPlugin plugin, TrustManager trustManager) {
        this.plugin = plugin;
        this.trustManager = trustManager;
        startPassiveAndHudRunnable();
    }

    // --- Passive Effects & Reorganized HUD Runnable ---
    private void startPassiveAndHudRunnable() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                ItemStack mainHand = p.getInventory().getItemInMainHand();
                ItemStack offHand = p.getInventory().getItemInOffHand();
                ItemStack heldGem = getActiveGem(mainHand, offHand);

                if (heldGem == null) continue;

                if (isGem(heldGem, Material.BREEZE_ROD, "Air Gem")) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 0, false, false));
                    sendActionBar(p, buildHud(p, "§b§lAIR GEM", 
                            "Dash [RC]", "air_dash", 8, 
                            "Breeze [OFF]", "air_breeze", 30, 
                            "Pulse [SH-RC]", "air_pulse", 15));

                } else if (isGem(heldGem, Material.BLAZE_ROD, "Fire Gem")) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30, 0, false, false));
                    sendActionBar(p, buildHud(p, "§c§lFIRE GEM", 
                            "Fireball [RC]", "fireball", 6, 
                            "Flame Wave [OFF]", "flame_wave", 12, 
                            "Flame Nova [SH-RC]", "flame_nova", 15));

                } else if (isGem(heldGem, Material.FIREWORK_STAR, "Earth Gem")) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0, false, false));
                    sendActionBar(p, buildHud(p, "§e§lEARTH GEM", 
                            "Might [RC]", "earth_might", 25, 
                            "Mud Wall [OFF]", "earth_wall", 20, 
                            "Slam [SH-RC]", "earth_slam", 15));

                } else if (isGem(heldGem, Material.HEART_OF_THE_SEA, "Water Gem")) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 30, 0, false, false));
                    if (p.isInWater()) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 1, false, false));
                    }
                    sendActionBar(p, buildHud(p, "§9§lWATER GEM", 
                            "Tsunami [RC]", "tsunami", 18, 
                            "Tridents [OFF]", "water_tridents", 30, 
                            "Heal Wave [SH-RC]", "healing_wave", 20));
                }
            }
        }, 0L, 10L);
    }

    private String buildHud(Player player, String title, String a1, String k1, int c1, String a2, String k2, int c2, String a3, String k3, int c3) {
        return title + " §8| " +
                "§f" + a1 + ": " + formatCd(player, k1, c1) + " §8| " +
                "§f" + a2 + ": " + formatCd(player, k2, c2) + " §8| " +
                "§f" + a3 + ": " + formatCd(player, k3, c3);
    }

    private String formatCd(Player player, String key, int seconds) {
        long remaining = getRemainingCooldownSeconds(player, key, seconds);
        return remaining > 0 ? "§c" + remaining + "s" : "§aREADY";
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    // --- Right Click Events: 1st Ability (Normal) & 3rd Ability (Shift + Right Click) ---
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        ItemStack gem = getActiveGem(mainHand, offHand);
        if (gem == null) return;

        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND && isGemItem(mainHand)) return;

        // Active Trident Throwing
        if (activeTridents.getOrDefault(player.getUniqueId(), 0) > 0) {
            launchTrident(player);
            return;
        }

        // --- 3RD ABILITY: Shift + Right Click ---
        if (player.isSneaking()) {
            if (isGem(gem, Material.BREEZE_ROD, "Air Gem")) {
                if (checkCooldown(player, "air_pulse", 15)) triggerAirPulse(player);
            } else if (isGem(gem, Material.BLAZE_ROD, "Fire Gem")) {
                if (checkCooldown(player, "flame_nova", 15)) triggerFlameNova(player);
            } else if (isGem(gem, Material.FIREWORK_STAR, "Earth Gem")) {
                if (checkCooldown(player, "earth_slam", 15)) triggerEarthSlam(player);
            } else if (isGem(gem, Material.HEART_OF_THE_SEA, "Water Gem")) {
                if (checkCooldown(player, "healing_wave", 20)) triggerHealingWave(player);
            }
            return;
        }

        // --- 1ST ABILITY: Normal Right Click ---
        if (isGem(gem, Material.BREEZE_ROD, "Air Gem")) {
            if (checkCooldown(player, "air_dash", 8)) {
                Vector dir = player.getLocation().getDirection().normalize().multiply(2.2);
                player.setVelocity(dir);
                player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 1.0f, 1.0f);
            }
        } else if (isGem(gem, Material.BLAZE_ROD, "Fire Gem")) {
            if (checkCooldown(player, "fireball", 6)) {
                Fireball fireball = player.launchProjectile(Fireball.class);
                fireball.setIsIncendiary(true);
                fireball.setYield(2.5f);
                fireball.setMetadata("ArmorPiercingFireball", new FixedMetadataValue(plugin, true));
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
            }
        } else if (isGem(gem, Material.FIREWORK_STAR, "Earth Gem")) {
            if (checkCooldown(player, "earth_might", 25)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
                spawnTemporaryGolem(player);
                player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1.0f, 0.8f);
            }
        } else if (isGem(gem, Material.HEART_OF_THE_SEA, "Water Gem")) {
            if (checkCooldown(player, "tsunami", 18)) triggerTsunamiRide(player);
        }
    }

    // --- 2ND ABILITY: Offhand Key ('F' Swap Hand) ---
    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack heldGem = getActiveGem(player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand());

        if (heldGem == null) return;

        event.setCancelled(true); // Prevent item swap when holding a gem

        if (isGem(heldGem, Material.BREEZE_ROD, "Air Gem")) {
            if (checkCooldown(player, "air_breeze", 30)) summonBreezes(player);
        } else if (isGem(heldGem, Material.BLAZE_ROD, "Fire Gem")) {
            if (checkCooldown(player, "flame_wave", 12)) triggerFlameWave(player);
        } else if (isGem(heldGem, Material.FIREWORK_STAR, "Earth Gem")) {
            if (checkCooldown(player, "earth_wall", 20)) spawnPackedMudWall(player);
        } else if (isGem(heldGem, Material.HEART_OF_THE_SEA, "Water Gem")) {
            if (checkCooldown(player, "water_tridents", 30)) {
                activeTridents.put(player.getUniqueId(), 3);
                player.sendMessage("§a[Water Gem] 3 Floating Tridents active! Right-click to launch them!");
                player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.2f);
            }
        }
    }

    // --- Ability Executions ---

    private void triggerFlameWave(Player player) {
        Vector dir = player.getLocation().getDirection().setY(0).normalize();
        Location start = player.getLocation();
        World world = start.getWorld();

        if (world != null) {
            world.playSound(start, Sound.ITEM_FIRECHARGE_USE, 1.5f, 0.8f);
            for (int i = 1; i <= 8; i++) {
                Location waveLoc = start.clone().add(dir.clone().multiply(i));
                world.spawnParticle(Particle.FLAME, waveLoc, 20, 0.8, 0.5, 0.8, 0.05);
                
                for (Entity entity : world.getNearbyEntities(waveLoc, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity target && entity != player) {
                        if (target instanceof Player p && trustManager.isTrusted(player.getUniqueId(), p.getUniqueId())) continue;
                        target.setFireTicks(80);
                        target.damage(6.0, player);
                    }
                }
            }
        }
    }

    private void triggerAirPulse(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.5f, 1.0f);
        player.getNearbyEntities(6, 6, 6).forEach(entity -> {
            if (entity instanceof LivingEntity target && entity != player) {
                if (target instanceof Player p && trustManager.isTrusted(player.getUniqueId(), p.getUniqueId())) return;
                Vector push = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.8).setY(0.5);
                entity.setVelocity(push);
            }
        });
    }

    private void triggerFlameNova(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (world != null) {
            world.spawnParticle(Particle.FLAME, loc, 150, 2.5, 1.0, 2.5, 0.2);
            world.spawnParticle(Particle.LAVA, loc, 40, 1.5, 1.0, 1.5, 0.1);
            world.playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.5f, 0.8f);
        }

        player.getNearbyEntities(6, 6, 6).forEach(entity -> {
            if (entity instanceof LivingEntity target && entity != player) {
                if (target instanceof Player p && trustManager.isTrusted(player.getUniqueId(), p.getUniqueId())) return;
                target.setFireTicks(120);
                target.damage(8.0, player);

                Vector push = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.2).setY(0.4);
                target.setVelocity(push);
            }
        });
    }

    private void triggerEarthSlam(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 0.5f);
        player.getNearbyEntities(5, 5, 5).forEach(entity -> {
            if (entity instanceof LivingEntity target && entity != player) {
                if (target instanceof Player p && trustManager.isTrusted(player.getUniqueId(), p.getUniqueId())) return;
                target.setVelocity(new Vector(0, 1.2, 0));
                target.damage(4.0, player);
            }
        });
    }

    private void triggerHealingWave(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.0f, 1.0f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1));
        player.getNearbyEntities(6, 6, 6).forEach(entity -> {
            if (entity instanceof Player ally && entity != player) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0));
                ally.sendMessage("§aHealed by " + player.getName() + "'s Water Wave!");
            }
        });
    }

    private void triggerTsunamiRide(Player player) {
        Vector dashDirection = player.getLocation().getDirection().normalize().multiply(2.5).setY(0.4);
        player.setVelocity(dashDirection);
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.5f, 0.8f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 15 || player.isDead() || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                Location loc = player.getLocation();
                World world = loc.getWorld();

                if (world != null) {
                    world.spawnParticle(Particle.SPLASH, loc, 35, 0.6, 0.6, 0.6, 0.2);
                    world.spawnParticle(Particle.BUBBLE_POP, loc, 20, 0.5, 0.5, 0.5, 0.1);
                    world.spawnParticle(Particle.FALLING_WATER, loc, 15, 0.4, 0.4, 0.4, 0.1);
                }

                for (Entity entity : player.getNearbyEntities(3.5, 3.5, 3.5)) {
                    if (entity instanceof LivingEntity target && entity != player) {
                        if (target instanceof Player p && trustManager.isTrusted(player.getUniqueId(), p.getUniqueId())) continue;

                        target.damage(8.0, player);
                        Vector throwVec = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5).setY(0.6);
                        target.setVelocity(throwVec);
                        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.2f, 0.9f);
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // --- Fixed & Rebuilt Mud Wall Logic ---
    private void spawnPackedMudWall(Player player) {
        Location pLoc = player.getLocation();
        World world = pLoc.getWorld();
        if (world == null) return;

        int radius = 3;
        int wallHeight = 6;
        int startY = pLoc.getBlockY();
        List<Block> wallBlocks = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Outer ring ring calculations for circular wall
                double distance = Math.sqrt(x * x + z * z);
                if (distance >= radius - 0.8 && distance <= radius + 0.8) {
                    for (int y = 0; y < wallHeight; y++) {
                        Block block = world.getBlockAt(pLoc.getBlockX() + x, startY + y, pLoc.getBlockZ() + z);

                        if (isUnbreakable(block.getType())) continue;

                        if (block.getType() != Material.AIR) {
                            block.breakNaturally();
                        }

                        block.setType(Material.PACKED_MUD);
                        wallBlocks.add(block);
                    }
                }
            }
        }

        world.playSound(pLoc, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.5f, 0.5f);

        // Despawn mud wall after 8 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Block b : wallBlocks) {
                if (b.getType() == Material.PACKED_MUD) {
                    b.setType(Material.AIR);
                }
            }
        }, 160L);
    }

    private boolean isUnbreakable(Material mat) {
        return mat == Material.BEDROCK ||
               mat == Material.OBSIDIAN ||
               mat == Material.CRYING_OBSIDIAN ||
               mat == Material.ANVIL ||
               mat == Material.CHIPPED_ANVIL ||
               mat == Material.DAMAGED_ANVIL ||
               mat == Material.ENCHANTING_TABLE ||
               mat == Material.REINFORCED_DEEPSLATE;
    }

    // --- Helpers & Cooldown Logic ---
    private void launchTrident(Player player) {
        int remaining = activeTridents.get(player.getUniqueId());
        Trident trident = player.launchProjectile(Trident.class);
        trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        trident.setDamage(8.0);
        
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.0f, 1.0f);
        remaining--;

        if (remaining <= 0) {
            activeTridents.remove(player.getUniqueId());
        } else {
            activeTridents.put(player.getUniqueId(), remaining);
        }
    }

    private void summonBreezes(Player player) {
        World world = player.getWorld();
        for (int i = 0; i < 2; i++) {
            Breeze breeze = (Breeze) world.spawnEntity(player.getLocation().add(i, 0, i), EntityType.BREEZE);
            breeze.setMetadata("NoLootBreeze", new FixedMetadataValue(plugin, true));
        }
    }

    private void spawnTemporaryGolem(Player player) {
        IronGolem golem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!golem.isDead()) golem.remove();
        }, 100L);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Fireball fireball && fireball.hasMetadata("ArmorPiercingFireball")) {
            if (event.getEntity() instanceof Player target && fireball.getShooter() instanceof Player shooter) {
                if (trustManager.isTrusted(shooter.getUniqueId(), target.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getEntity() instanceof LivingEntity target) {
                event.setDamage(0);
                target.damage(10.0);
            }
        }
    }

    @EventHandler
    public void onBreezeDeath(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("NoLootBreeze")) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    private boolean checkCooldown(Player player, String ability, int seconds) {
        long remaining = getRemainingCooldownSeconds(player, ability, seconds);
        if (remaining > 0) return false;

        cooldowns.putIfAbsent(player.getUniqueId(), new HashMap<>());
        cooldowns.get(player.getUniqueId()).put(ability, System.currentTimeMillis());
        return true;
    }

    private long getRemainingCooldownSeconds(Player player, String ability, int seconds) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null || !playerCooldowns.containsKey(ability)) return 0;

        long lastUse = playerCooldowns.get(ability);
        long timeLeftMillis = (lastUse + (seconds * 1000L)) - System.currentTimeMillis();

        return timeLeftMillis > 0 ? (timeLeftMillis / 1000L) + 1 : 0;
    }

    private ItemStack getActiveGem(ItemStack mainHand, ItemStack offHand) {
        if (isGemItem(mainHand)) return mainHand;
        if (isGemItem(offHand)) return offHand;
        return null;
    }

    private boolean isGemItem(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && 
               (item.getItemMeta().getDisplayName().contains("Gem"));
    }

    private boolean isGem(ItemStack item, Material material, String gemName) {
        return item != null && item.getType() == material && item.hasItemMeta() && 
               item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains(gemName);
    }
}
