package com.elementalsmp.gems;

import com.elementalsmp.gems.commands.GiveGemCommand;
import com.elementalsmp.gems.commands.TrustCommand;
import com.elementalsmp.gems.listeners.GemListener;
import com.elementalsmp.gems.managers.TrustManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GemsPlugin extends JavaPlugin {

    private TrustManager trustManager;

    @Override
    public void onEnable() {
        // Initialize Managers
        this.trustManager = new TrustManager();

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(new GemListener(this, trustManager), this);

        // Register Trust Commands
        TrustCommand trustCommand = new TrustCommand(trustManager);
        if (getCommand("trust") != null) getCommand("trust").setExecutor(trustCommand);
        if (getCommand("untrust") != null) getCommand("untrust").setExecutor(trustCommand);
        if (getCommand("trustlist") != null) getCommand("trustlist").setExecutor(trustCommand);

        // Register Give Gem Command
        GiveGemCommand giveGemCommand = new GiveGemCommand();
        if (getCommand("givegem") != null) {
            getCommand("givegem").setExecutor(giveGemCommand);
            getCommand("givegem").setTabCompleter(giveGemCommand);
        }

        getLogger().info("ElementalGems has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ElementalGems has been disabled.");
    }

    public TrustManager getTrustManager() {
        return trustManager;
    }
}