package com.elementalsmp.gems;

import com.elementalsmp.gems.commands.GiveGemCommand;
import com.elementalsmp.gems.commands.TrustCommands;
import com.elementalsmp.gems.listeners.GemListener;
import com.elementalsmp.gems.managers.TrustManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GemsPlugin extends JavaPlugin {

    private TrustManager trustManager;

    @Override
    public void onEnable() {
        // Instantiate TrustManager first
        this.trustManager = new TrustManager();

        // Register Event Listener passing BOTH 'this' and 'trustManager'
        getServer().getPluginManager().registerEvents(new GemListener(this, trustManager), this);

        // Register /givegem Command
        GiveGemCommand giveGemCmd = new GiveGemCommand();
        if (getCommand("givegem") != null) {
            getCommand("givegem").setExecutor(giveGemCmd);
            getCommand("givegem").setTabCompleter(giveGemCmd);
        }

        // Register Trust Commands (/trust, /untrust, /trustlist)
        TrustCommands trustCmds = new TrustCommands(trustManager);
        for (String cmd : new String[]{"trust", "untrust", "trustlist"}) {
            if (getCommand(cmd) != null) {
                getCommand(cmd).setExecutor(trustCmds);
                getCommand(cmd).setTabCompleter(trustCmds);
            }
        }

        getLogger().info("Elemental Gems Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Elemental Gems Plugin disabled!");
    }
}
