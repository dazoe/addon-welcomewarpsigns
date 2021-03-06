package bentobox.addon.warps;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;

import bentobox.addon.warps.commands.WarpCommand;
import bentobox.addon.warps.commands.WarpsCommand;
import bentobox.addon.warps.config.PluginConfig;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.util.Util;

/**
 * Addin to BSkyBlock that enables welcome warp signs
 * @author tastybento
 *
 */
public class Warp extends Addon {

    private static final String BSKYBLOCK = "BSkyBlock";
    private static final String ACIDISLAND = "AcidIsland";

    // The plugin instance.
    private BentoBox plugin;

    // Warp panel object
    private WarpPanelManager warpPanelManager;

    // Warps signs object
    private WarpSignsManager warpSignsManager;

    private Set<World> registeredWorlds;

    private PluginConfig settings;

    @Override
    public void onEnable() {
        // Load the plugin's config
        settings = new PluginConfig(this);
        // Get the BSkyBlock plugin. This will be available because this plugin depends on it in plugin.yml.
        plugin = this.getPlugin();
        // Check if it is enabled - it might be loaded, but not enabled.
        if (!plugin.isEnabled()) {
            this.setEnabled(false);
            return;
        }
        registeredWorlds = new HashSet<>();
        // Start warp signs
        warpSignsManager = new WarpSignsManager(this, plugin);
        warpPanelManager = new WarpPanelManager(this);
        // Load the listener
        getServer().getPluginManager().registerEvents(new WarpSignsListener(this), plugin);
        // Register commands

        // BSkyBlock hook in
        this.getPlugin().getAddonsManager().getAddonByName(BSKYBLOCK).ifPresent(acidIsland -> {
            CompositeCommand bsbIslandCmd = BentoBox.getInstance().getCommandsManager().getCommand("island");
            if (bsbIslandCmd != null) {
                new WarpCommand(this, bsbIslandCmd);
                new WarpsCommand(this, bsbIslandCmd);
                registeredWorlds.add(plugin.getIWM().getWorld(BSKYBLOCK));
            }
        });
        // AcidIsland hook in
        this.getPlugin().getAddonsManager().getAddonByName(ACIDISLAND).ifPresent(acidIsland -> {
            CompositeCommand acidIslandCmd = getPlugin().getCommandsManager().getCommand("ai");
            if (acidIslandCmd != null) {
                new WarpCommand(this, acidIslandCmd);
                new WarpsCommand(this, acidIslandCmd);
                registeredWorlds.add(plugin.getIWM().getWorld(ACIDISLAND));
            }
        });

        // Done
    }

    @Override
    public void onDisable(){
        // Save the warps
        if (warpSignsManager != null)
            warpSignsManager.saveWarpList();
    }

    /**
     * Get warp panel manager
     * @return
     */
    public WarpPanelManager getWarpPanelManager() {
        return warpPanelManager;
    }

    public WarpSignsManager getWarpSignsManager() {
        return warpSignsManager;
    }

    public String getPermPrefix(World world) {
        return this.getPlugin().getIWM().getPermissionPrefix(world);
    }

    /**
     * Check if an event is in a registered world
     * @param world - world to check
     * @return true if it is
     */
    public boolean inRegisteredWorld(World world) {
        return registeredWorlds.contains(Util.getWorld(world));
    }

    /**
     * @return the settings
     */
    public PluginConfig getSettings() {
        return settings;
    }

}
