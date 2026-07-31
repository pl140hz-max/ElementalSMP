package com.elementalsmp.gems;

import com.elementalsmp.gems.commands.GiveGemCommand;
import com.elementalsmp.gems.listeners.GemListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class GemsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register Event Listener for Gem Abilities & Passives
        getServer().getPluginManager().registerEvents(new GemListener(this), this);

        // Register /givegem Command and Tab Completer
        GiveGemCommand giveGemCmd = new GiveGemCommand();
        if (getCommand("givegem") != null) {
            getCommand("givegem").setExecutor(giveGemCmd);
            getCommand("givegem").setTabCompleter(giveGemCmd);
        }

        getLogger().info("Elemental Gems Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Elemental Gems Plugin disabled!");
    }
}
