package com.gems;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public enum GemType {
    FIRE("Fire Gem", ChatColor.RED, Material.MAGMA_CREAM, "Increases strength and fire immunity"),
    WATER("Water Gem", ChatColor.BLUE, Material.HEART_OF_THE_SEA, "Water speed and water breathing"),
    EARTH("Earth Gem", ChatColor.GREEN, Material.EMERALD, "Resistance and regeneration on land"),
    WIND("Wind Gem", ChatColor.WHITE, Material.FEATHER, "Super jump and speed in the air");

    private final String name;
    private final ChatColor color;
    private final Material material;
    private final String description;

    GemType(String name, ChatColor color, Material material, String description) {
        this.name = name;
        this.color = color;
        this.material = material;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDescription() {
        return description;
    }

    public ItemStack createItem(GemsPlugin plugin) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + "" + ChatColor.BOLD + name);
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + description,
                "",
                ChatColor.YELLOW + "Elemental Power"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static GemType fromItem(GemsPlugin plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return null;

        for (GemType gem : values()) {
            if (meta.getDisplayName().contains(gem.getName())) {
                return gem;
            }
        }
        return null;
    }
}
