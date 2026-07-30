package com.gems;

import org.bukkit.plugin.java.JavaPlugin;

public class GemsPlugin extends JavaPlugin {

    // Direct link to your .zip resource pack (leave empty until it's ready)
    private String resourcePackUrl = "";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new GemListener(this), this);
        getLogger().info("ElementalSMP successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ElementalSMP disabled.");
    }

    public String getResourcePackUrl() {
        return resourcePackUrl;
    }

    public void setResourcePackUrl(String resourcePackUrl) {
        this.resourcePackUrl = resourcePackUrl;
    }
}
