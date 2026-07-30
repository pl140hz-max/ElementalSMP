package com.elementalsmp.gems;

import com.elementalsmp.gems.commands.GemCommand;
import com.elementalsmp.gems.listeners.GemListener;
import org.bukkit.plugin.java.JavaPlugin;

public class GemsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(new GemListener(), this);

        // Register command and tab completer
        if (getCommand("givegem") != null) {
            GemCommand gemCommand = new GemCommand();
            getCommand("givegem").setExecutor(gemCommand);
            getCommand("givegem").setTabCompleter(gemCommand);
        }

        getLogger().info("ElementalSMP GemsPlugin successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ElementalSMP GemsPlugin disabled.");
    }
}
