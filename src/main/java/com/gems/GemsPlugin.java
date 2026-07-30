package com.gems;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class GemsPlugin extends JavaPlugin {

    private NamespacedKey gemKey;
    // Coloque aqui o link do download direto da sua texture pack (arquivo .zip)
    private static final String RESOURCE_PACK_URL = "https://exemplo.com/gems_resource_pack.zip"; 

    @Override
    public void onEnable() {
        this.gemKey = new NamespacedKey(this, "elemental_gem");
        getServer().getPluginManager().registerEvents(new GemListener(this), this);
        getLogger().info("GemsPlugin enabled successfully!");
    }

    public NamespacedKey getGemKey() {
        return gemKey;
    }

    public String getResourcePackUrl() {
        return RESOURCE_PACK_URL;
    }
}
