package com.gems;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class GemListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> activeTridents = new HashMap<>();

    public GemListener(JavaPlugin plugin) {
        this.plugin = plugin;
        startPassiveRunnable();
    }

    private void startPassiveRunnable() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                ItemStack item = p.getInventory().getItemInMainHand();

                if (isGem(item, "Air Gem")) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 0, false, false));
                } else if (isGem(item, "Fire Gem")) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30, 0, false, false));
                } else if (isGem(item, "Earth Gem")) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0, false, false));
                } else if (isGem(item, "Water Gem")) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 30, 0, false, false));
                    if (p.isInWater()) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 1, false, false));
                    }
                }
            }
        }, 0L, 10L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;

        if (activeTridents.getOrDefault(player.getUniqueId(), 0) > 0) {
            launchTrident(player);
            return;
        }

        if (item == null || !item.hasItemMeta()) return;

        if (isGem(item, "Air Gem")) {
            if (player.isSneaking()) {
                if (checkCooldown(player, "air_breeze", 30)) {
                    summonBreezes(player);
                }
            } else {
                if (checkCooldown(player, "air_dash", 8)) {
                    Vector dir = player.getLocation().getDirection().normalize().multiply(2.2);
                    player.setVelocity(dir);
                    player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 1.0f, 1.0f);
                }
            }
        } else if (isGem(item, "Fire Gem")) {
            if (checkCooldown(player, "fireball", 6)) {
                Fireball fireball = player.launchProjectile(Fireball.class);
                fireball.setIsIncendiary(true);
                fireball.setYield(2.5f);
                fireball.setMetadata("ArmorPiercingFireball", new FixedMetadataValue(plugin, true));
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
            }
        } else if (isGem(item, "Earth Gem")) {
            if (player.isSneaking()) {
                if (checkCooldown(player, "earth_wall", 20)) {
                    spawnPackedMudWall(player);
                }
            } else {
                if (checkCooldown(player, "earth_might", 25)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
                    spawnTemporaryGolem(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1.0f, 0.8f);
                }
            }
        } else if (isGem(item, "Water Gem")) {
            if (player.isSneaking()) {
                if (checkCooldown(player, "water_tridents", 30)) {
                    activeTridents.put(player.getUniqueId(), 3);
                    player.sendMessage("3 Floating Tridents active! Right-click to launch them!");
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.2f);
                }
            } else {
                if (checkCooldown(player, "tsunami", 18)) {
                    triggerTsunamiRide(player);
                }
            }
        }
    }

    private void launchTrident(Player player) {
        int remaining = activeTridents.get(player.getUniqueId());
        Trident trident = player.launchProjectile(Trident.class);
        trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        trident.setDamage(8.0);
        
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.0f, 1.0f);
        remaining--;

        if (remaining <= 0) {
            activeTridents.remove(player.getUniqueId());
            player.sendMessage("You have thrown all tridents!");
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
        player.sendMessage("Summoned 2 Breeze allies!");
    }

    private void spawnTemporaryGolem(Player player) {
        IronGolem golem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!golem.isDead()) {
                golem.remove();
            }
        }, 100L);
    }

    private void spawnPackedMudWall(Player player) {
        Location center = player.getLocation();
        int radius = 5;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.abs(x * x + z * z - radius * radius) < radius) {
                    Block block = center.clone().add(x, 0, z).getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.PACKED_MUD);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (block.getType() == Material.PACKED_MUD) block.setType(Material.AIR);
                        }, 160L);
                    }
                }
            }
        }
    }

    private void triggerTsunamiRide(Player player) {
        Vector dir = player.getLocation().getDirection().normalize().setY(0.2);
        player.setVelocity(dir.multiply(1.8));

        player.getNearbyEntities(4, 4, 4).forEach(entity -> {
            if (entity instanceof LivingEntity target && entity != player) {
                target.setRemainingAir(0);
                target.damage(6.0, player);
            }
        });
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Fireball fireball && fireball.hasMetadata("ArmorPiercingFireball")) {
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
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        cooldowns.putIfAbsent(uuid, new HashMap<>());
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);

        if (playerCooldowns.containsKey(ability)) {
            long lastUse = playerCooldowns.get(ability);
            long timeLeft = (lastUse + (seconds * 1000L)) - now;

            if (timeLeft > 0) {
                player.sendMessage("Ability on cooldown! Wait " + (timeLeft / 1000 + 1) + "s.");
                return false;
            }
        }

        playerCooldowns.put(ability, now);
        return true;
    }

    private boolean isGem(ItemStack item, String gemName) {
        return item != null && item.hasItemMeta() && 
               item.getItemMeta().getDisplayName().contains(gemName);
    }
}
