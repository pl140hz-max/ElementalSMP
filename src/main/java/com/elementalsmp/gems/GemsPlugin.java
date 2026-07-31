package com.elementalsmp.gems;

import com.elementalsmp.gems.listeners.GemListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class GemsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Pass 'this' into GemListener so it can schedule tasks and handle metadata!
        getServer().getPluginManager().registerEvents(new GemListener(this), this);
        getLogger().info("Elemental Gems Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Elemental Gems Plugin Disabled!");
    }
}
