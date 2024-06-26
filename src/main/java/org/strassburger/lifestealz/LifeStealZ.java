package org.strassburger.lifestealz;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.strassburger.lifestealz.listeners.PlayerDeathListener;
import org.strassburger.lifestealz.util.*;
import org.strassburger.lifestealz.util.storage.PlayerDataStorage;
import org.strassburger.lifestealz.util.storage.SQLitePlayerDataStorage;
import org.strassburger.lifestealz.util.worldguard.WorldGuardManager;

public final class LifeStealZ extends JavaPlugin {

    static LifeStealZ instance;
    private VersionChecker versionChecker;
    private PlayerDataStorage playerDataStorage;
    private WorldGuardManager worldGuardManager;
    private final boolean hasWorldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    private final boolean hasPlaceholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    @Override
    public void onLoad() {
        getLogger().info("Loading LifeStealZ...");
        if (hasWorldGuard()) {
            getLogger().info("WorldGuard found! Enabling WorldGuard support...");
            worldGuardManager = new WorldGuardManager();
            getLogger().info("WorldGuard found! Enabled WorldGuard support!");
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        CommandManager.registerCommands();

        EventManager.registerListeners();

        RecipeManager.registerRecipes();

        playerDataStorage = createPlayerDataStorage();
        playerDataStorage.init();

        versionChecker = new VersionChecker();

        // Register bstats
        int pluginId = 18735;
        new Metrics(this, pluginId);

        if (hasPlaceholderApi()) {
            new PapiExpansion().register();
            getLogger().info("PlaceholderAPI found! Enabled PlaceholderAPI support!");
        }

        getLogger().info("LifeStealZ enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LifeStealZ disabled!");
    }

    public static LifeStealZ getInstance() {
        return instance;
    }

    public VersionChecker getVersionChecker() {
        return versionChecker;
    }

    public PlayerDataStorage getPlayerDataStorage() {
        return playerDataStorage;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }

    public boolean hasWorldGuard() {
        return hasWorldGuard;
    }

    public boolean hasPlaceholderApi() {
        return hasPlaceholderApi;
    }

    private PlayerDataStorage createPlayerDataStorage() {
        String option = getConfig().getString("storage.type");

        if (option.equalsIgnoreCase("mysql")) {
            // Todo("Implement MySQL storage");
        }

        getLogger().info("Using SQLite storage");
        return new SQLitePlayerDataStorage();
    }

    public static void setMaxHealth(Player player, double maxHealth) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(maxHealth);
        }
    }
}
