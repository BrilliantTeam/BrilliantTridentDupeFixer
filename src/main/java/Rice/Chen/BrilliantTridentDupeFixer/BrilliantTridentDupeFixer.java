package Rice.Chen.BrilliantTridentDupeFixer;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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

    public BrilliantTridentDupeFixer() {
        this.eventListener = new TridentEventListener(this);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(eventListener, this);
        getLogger().info("Trident duplication prevention system enabled");
    }

    @Override
    public void onDisable() {
        eventListener.cleanup();
        getLogger().info("Trident duplication prevention system disabled");
    }
}

class TridentEventListener implements Listener {
    private final BrilliantTridentDupeFixer plugin;
    private final Set<UUID> playersReadyToThrow;
    private final Map<UUID, Integer> lastProjectileCancel;

    public TridentEventListener(BrilliantTridentDupeFixer plugin) {
        this.plugin = plugin;
        this.playersReadyToThrow = ConcurrentHashMap.newKeySet();
        this.lastProjectileCancel = new ConcurrentHashMap<>();
    }

    public void cleanup() {
        playersReadyToThrow.clear();
        lastProjectileCancel.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void handleTridentLaunch(PlayerLaunchProjectileEvent event) {
        if (!isTrident(event.getItemStack())) {
            return;
        }

        Player player = event.getPlayer();
        player.closeInventory();

        if (isProjectileCancelled(player)) {
            logWarning("hotkey duplication", player);
            event.setCancelled(true);
            return;
        }

        playersReadyToThrow.remove(player.getUniqueId());
    }

    @EventHandler
    public void handlePlayerInteraction(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        if (isRightClick(event.getAction()) && isTrident(item)) {
            playersReadyToThrow.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (!isTrident(currentItem)) {
            return;
        }

        UUID playerUUID = event.getWhoClicked().getUniqueId();
        if (playersReadyToThrow.contains(playerUUID)) {
            logWarning("inventory manipulation during throw state", event.getWhoClicked().getName());
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            logWarning("hotbar swap", event.getWhoClicked().getName());
            lastProjectileCancel.put(playerUUID, plugin.getServer().getCurrentTick());
            event.setResult(Event.Result.DENY);
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

    private void logWarning(String action, String playerName) {
        plugin.getLogger().warning(String.format("Detected player %s attempting to %s trident.", playerName, action));
    }

    private void logWarning(String action, Player player) {
        logWarning(action, player.getName());
    }
}