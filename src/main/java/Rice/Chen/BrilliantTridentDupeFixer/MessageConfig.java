package Rice.Chen.BrilliantTridentDupeFixer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class MessageConfig {
    private final JavaPlugin plugin;
    private boolean logToConsole;
    private boolean notifyPlayer;
    private String prefix;
    
    private String playerHotkeyDupeMessage;
    private String playerInventoryManipulationMessage;
    private String playerHotbarSwapMessage;
    
    private String consoleHotkeyDupeMessage;
    private String consoleInventoryManipulationMessage;
    private String consoleHotbarSwapMessage;
    
    private final Map<String, String> commandMessages = new HashMap<>();

    public MessageConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        
        ConfigurationSection messageSection = config.getConfigurationSection("messages");
        if (messageSection == null) {
            createDefaultConfig();
            messageSection = config.getConfigurationSection("messages");
        }
        
        this.logToConsole = messageSection.getBoolean("log-to-console", true);
        this.notifyPlayer = messageSection.getBoolean("notify-player", true);
        this.prefix = messageSection.getString("prefix", "<dark_red>[BrilliantTridentDupeFixer] </dark_red>");
        
        ConfigurationSection playerMsgSection = messageSection.getConfigurationSection("player");
        if (playerMsgSection != null) {
            this.playerHotkeyDupeMessage = playerMsgSection.getString("hotkey-dupe", 
                "<red>Detected attempted hotkey duplication of trident.</red>");
            this.playerInventoryManipulationMessage = playerMsgSection.getString("inventory-manipulation", 
                "<red>Detected inventory manipulation during trident throw state.</red>");
            this.playerHotbarSwapMessage = playerMsgSection.getString("hotbar-swap", 
                "<red>Detected hotbar swap with trident.</red>");
        } else {
            this.playerHotkeyDupeMessage = "<red>Detected attempted hotkey duplication of trident.</red>";
            this.playerInventoryManipulationMessage = "<red>Detected inventory manipulation during trident throw state.</red>";
            this.playerHotbarSwapMessage = "<red>Detected hotbar swap with trident.</red>";
        }
        
        ConfigurationSection consoleMsgSection = messageSection.getConfigurationSection("console");
        if (consoleMsgSection != null) {
            this.consoleHotkeyDupeMessage = consoleMsgSection.getString("hotkey-dupe", 
                "Detected attempted hotkey duplication of trident");
            this.consoleInventoryManipulationMessage = consoleMsgSection.getString("inventory-manipulation", 
                "Detected inventory manipulation during trident throw state");
            this.consoleHotbarSwapMessage = consoleMsgSection.getString("hotbar-swap", 
                "Detected hotbar swap with trident");
        } else {
            this.consoleHotkeyDupeMessage = "Detected attempted hotkey duplication of trident";
            this.consoleInventoryManipulationMessage = "Detected inventory manipulation during trident throw state";
            this.consoleHotbarSwapMessage = "Detected hotbar swap with trident";
        }
        
        ConfigurationSection commandSection = config.getConfigurationSection("command-messages");
        if (commandSection == null) {
            createDefaultCommandMessages();
            commandSection = config.getConfigurationSection("command-messages");
        }
        
        for (String key : commandSection.getKeys(false)) {
            commandMessages.put(key, commandSection.getString(key, ""));
        }
    }
    
    private void createDefaultConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("messages.log-to-console", true);
        config.set("messages.notify-player", true);
        config.set("messages.prefix", "<dark_red>[TridentDupeFixer]</dark_red>");
        
        config.set("messages.player.hotkey-dupe", "<red>Detected attempted hotkey duplication of trident.</red>");
        config.set("messages.player.inventory-manipulation", "<red>Detected inventory manipulation during trident throw state.</red>");
        config.set("messages.player.hotbar-swap", "<red>Detected hotbar swap with trident.</red>");
        
        config.set("messages.console.hotkey-dupe", "Detected attempted hotkey duplication of trident");
        config.set("messages.console.inventory-manipulation", "Detected inventory manipulation during trident throw state");
        config.set("messages.console.hotbar-swap", "Detected hotbar swap with trident");
        
        createDefaultCommandMessages();
        
        plugin.saveConfig();
    }
    
    private void createDefaultCommandMessages() {
        FileConfiguration config = plugin.getConfig();
        
        config.set("command-messages.no-permission", "<red>You don't have permission to use this command.</red>");
        config.set("command-messages.config-reloaded", "<green>Plugin configuration has been reloaded.</green>");
        config.set("command-messages.unknown-command", "<red>Unknown command. Use <white>/tdf help</white> to see available commands.</red>");
        
        config.set("command-messages.help-header", "<yellow>=== BrilliantTridentDupeFixer Help ===</yellow>");
        config.set("command-messages.help-reload", "<white>/tdf reload</white> </gray>- Reload plugin configuration</gray>");
        config.set("command-messages.help-info", "<white>/tdf info</white> </gray>- Display plugin information</gray>");
        config.set("command-messages.help-help", "<white>/tdf help</white> </gray>- Show this help message</gray>");
        
        plugin.saveConfig();
    }
    
    public void sendWarning(WarningType type, Player player) {
        if (logToConsole) {
            String consoleMessage = getConsoleMessage(type);
            plugin.getLogger().warning(String.format("%s (%s)", consoleMessage, player.getName()));
        }
        
        if (notifyPlayer && player.isOnline()) {
            Component prefixComponent = MiniMessage.miniMessage().deserialize(prefix);
            Component messageComponent = MiniMessage.miniMessage().deserialize(getPlayerMessage(type));
            player.sendMessage(prefixComponent.append(Component.text("")).append(messageComponent));
        }
    }
    
    public void sendWarning(WarningType type, String playerName) {
        if (logToConsole) {
            String consoleMessage = getConsoleMessage(type);
            plugin.getLogger().warning(String.format("%s (%s)", consoleMessage, playerName));
        }
    }
    
    private String getPlayerMessage(WarningType type) {
        return switch (type) {
            case HOTKEY_DUPE -> playerHotkeyDupeMessage;
            case INVENTORY_MANIPULATION -> playerInventoryManipulationMessage;
            case HOTBAR_SWAP -> playerHotbarSwapMessage;
        };
    }
    
    private String getConsoleMessage(WarningType type) {
        return switch (type) {
            case HOTKEY_DUPE -> consoleHotkeyDupeMessage;
            case INVENTORY_MANIPULATION -> consoleInventoryManipulationMessage;
            case HOTBAR_SWAP -> consoleHotbarSwapMessage;
        };
    }
    
    public String getCommandMessage(String key) {
        return commandMessages.getOrDefault(key, "Missing message: " + key);
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public enum WarningType {
        HOTKEY_DUPE,
        INVENTORY_MANIPULATION,
        HOTBAR_SWAP
    }
}