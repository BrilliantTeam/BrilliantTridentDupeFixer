package Rice.Chen.BrilliantTridentDupeFixer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TridentDupeFixerCommand implements CommandExecutor, TabCompleter {
    
    private final BrilliantTridentDupeFixer plugin;
    private static final String PERMISSION = "tridentdupefixer.admin";
    
    public TridentDupeFixerCommand(BrilliantTridentDupeFixer plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sendMessageWithPrefix(sender, plugin.getMessageConfig().getCommandMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadMessageConfig();
                // Only display config-reloaded message to players, not console
                if (sender instanceof Player) {
                    sendMessageWithPrefix(sender, plugin.getMessageConfig().getCommandMessage("config-reloaded"));
                } else {
                    // For console, use the built-in logger instead
                    plugin.getLogger().info("Configuration reloaded successfully");
                }
            }
            case "info" -> {
                showPluginInfo(sender);
            }
            case "help" -> showHelp(sender);
            default -> {
                sendMessageWithPrefix(sender, plugin.getMessageConfig().getCommandMessage("unknown-command"));
            }
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sendMessageWithPrefix(sender, plugin.getMessageConfig().getCommandMessage("help-header"));
        sendMessageWithPrefix(sender, plugin.getMessageConfig().getCommandMessage("help-reload"));
        sendMessageWithPrefix(sender, plugin.getMessageConfig().getCommandMessage("help-info"));
        sendMessageWithPrefix(sender, plugin.getMessageConfig().getCommandMessage("help-help"));
    }
    
    private void showPluginInfo(CommandSender sender) {
        sendMessageWithPrefix(sender, "<aqua>Plugin Information:</aqua>");
        sendMessageWithPrefix(sender, "<white>Name: <yellow>BrilliantTridentDupeFixer</yellow></white>");
        sendMessageWithPrefix(sender, "<white>Version: <yellow>" + plugin.getDescription().getVersion() + "</yellow></white>");
        sendMessageWithPrefix(sender, "<white>Author: <yellow>RiceChen_</yellow></white>");
    }
    
    private void sendMessageWithPrefix(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        String prefix = plugin.getMessageConfig().getPrefix();
        
        if (sender instanceof Player player) {
            Component prefixComponent = MiniMessage.miniMessage().deserialize(prefix);
            Component messageComponent = MiniMessage.miniMessage().deserialize(message);
            player.sendMessage(prefixComponent.append(Component.text(" ")).append(messageComponent));
        } else {
            Component prefixComponent = MiniMessage.miniMessage().deserialize(prefix);
            Component messageComponent = MiniMessage.miniMessage().deserialize(message);
            String plainPrefix = PlainTextComponentSerializer.plainText().serialize(prefixComponent);
            String plainMessage = PlainTextComponentSerializer.plainText().serialize(messageComponent);
            sender.sendMessage(plainPrefix + " " + plainMessage);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("reload", "info", "help");
            return subCommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}