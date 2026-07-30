package com.gems;

import org.bukkit.plugin.java.JavaPlugin;

public class GemsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new GemListener(this), this);
        getLogger().info("ElementalSMP successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ElementalSMP disabled.");
    }
}
