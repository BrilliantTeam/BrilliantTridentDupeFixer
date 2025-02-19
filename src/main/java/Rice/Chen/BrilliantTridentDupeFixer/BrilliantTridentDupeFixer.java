package Rice.Chen.BrilliantTridentDupeFixer;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BrilliantTridentDupeFixer extends JavaPlugin {
    private final TridentEventListener eventListener;
    private MessageConfig messageConfig;

    public BrilliantTridentDupeFixer() {
        this.eventListener = new TridentEventListener(this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.messageConfig = new MessageConfig(this);
        
        getServer().getPluginManager().registerEvents(eventListener, this);
        
        TridentDupeFixerCommand commandExecutor = new TridentDupeFixerCommand(this);
        getCommand("tridentdupefixer").setExecutor(commandExecutor);
        getCommand("tridentdupefixer").setTabCompleter(commandExecutor);
        
        getLogger().info("Trident duplication prevention system enabled");
    }

    @Override
    public void onDisable() {
        eventListener.cleanup();
        getLogger().info("Trident duplication prevention system disabled");
    }
    
    public MessageConfig getMessageConfig() {
        return messageConfig;
    }
    
    public void reloadMessageConfig() {
        reloadConfig();
        messageConfig.loadConfig();
        getLogger().info("Configuration reloaded successfully");
    }
}

class TridentEventListener implements Listener {
    private final BrilliantTridentDupeFixer plugin;
    private final Set<UUID> playersReadyToThrow;
    private final Map<UUID, Integer> lastProjectileCancel;
    private final Set<UUID> playersWithOpenContainer;

    public TridentEventListener(BrilliantTridentDupeFixer plugin) {
        this.plugin = plugin;
        this.playersReadyToThrow = ConcurrentHashMap.newKeySet();
        this.lastProjectileCancel = new ConcurrentHashMap<>();
        this.playersWithOpenContainer = ConcurrentHashMap.newKeySet();
    }

    public void cleanup() {
        playersReadyToThrow.clear();
        lastProjectileCancel.clear();
        playersWithOpenContainer.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void handleTridentLaunch(PlayerLaunchProjectileEvent event) {
        if (!isTrident(event.getItemStack())) {
            return;
        }

        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        player.closeInventory();

        if (playersWithOpenContainer.contains(playerUUID)) {
            playersWithOpenContainer.remove(playerUUID);
            playersReadyToThrow.remove(playerUUID);
            return;
        }

        if (isProjectileCancelled(player)) {
            logWarning(MessageConfig.WarningType.HOTKEY_DUPE, player);
            event.setCancelled(true);
            return;
        }

        playersReadyToThrow.remove(playerUUID);
    }

    @EventHandler
    public void handlePlayerInteraction(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        Player player = event.getPlayer();
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && 
            event.getClickedBlock() != null && 
            isContainer(event.getClickedBlock().getType())) {
            playersWithOpenContainer.add(player.getUniqueId());
            return;
        }

        if (isRightClick(event.getAction()) && isTrident(item)) {
            playersReadyToThrow.add(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (!isTrident(currentItem)) {
            return;
        }

        UUID playerUUID = event.getWhoClicked().getUniqueId();
        
        if (playersWithOpenContainer.contains(playerUUID)) {
            return;
        }
        
        if (playersReadyToThrow.contains(playerUUID)) {
            if (event.getWhoClicked() instanceof Player player) {
                logWarning(MessageConfig.WarningType.INVENTORY_MANIPULATION, player);
            } else {
                logWarning(MessageConfig.WarningType.INVENTORY_MANIPULATION, event.getWhoClicked().getName());
            }
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            if (event.getWhoClicked() instanceof Player player) {
                logWarning(MessageConfig.WarningType.HOTBAR_SWAP, player);
            } else {
                logWarning(MessageConfig.WarningType.HOTBAR_SWAP, event.getWhoClicked().getName());
            }
            lastProjectileCancel.put(playerUUID, plugin.getServer().getCurrentTick());
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void handleInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            playersWithOpenContainer.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void handleInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            playersWithOpenContainer.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void handleItemHoldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        
        if (!isTrident(newItem)) {
            playersReadyToThrow.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        playersReadyToThrow.remove(playerUUID);
        lastProjectileCancel.remove(playerUUID);
        playersWithOpenContainer.remove(playerUUID);
    }

    private boolean isTrident(ItemStack item) {
        return item != null && item.getType() == Material.TRIDENT;
    }

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }

    private boolean isProjectileCancelled(Player player) {
        int currentTick = plugin.getServer().getCurrentTick();
        Integer lastCancelTick = lastProjectileCancel.getOrDefault(player.getUniqueId(), -1);
        return currentTick == lastCancelTick;
    }

    private boolean isContainer(Material material) {
        return switch (material) {
            case CHEST, TRAPPED_CHEST, ENDER_CHEST, BARREL,
                 SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX, 
                 MAGENTA_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX, YELLOW_SHULKER_BOX,
                 LIME_SHULKER_BOX, PINK_SHULKER_BOX, GRAY_SHULKER_BOX,
                 LIGHT_GRAY_SHULKER_BOX, CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX,
                 BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX,
                 RED_SHULKER_BOX, BLACK_SHULKER_BOX, FURNACE, BLAST_FURNACE,
                 SMOKER, HOPPER, DISPENSER, DROPPER, BREWING_STAND -> true;
            default -> false;
        };
    }

    private void logWarning(MessageConfig.WarningType type, Player player) {
        plugin.getMessageConfig().sendWarning(type, player);
    }

    private void logWarning(MessageConfig.WarningType type, String playerName) {
        plugin.getMessageConfig().sendWarning(type, playerName);
    }
}