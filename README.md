# BrilliantTridentDupeFixer

A comprehensive solution for preventing trident duplication exploits on Paper servers, with configurable messaging and advanced container interaction handling.

## Features

- **Robust Exploit Prevention**
  - Prevents hotkey duplication exploits
  - Blocks inventory manipulation during trident throws
  - Stops hotbar swap exploits
  - Allows legitimate container interaction

- **Configurable Messaging System**
  - Separate player and console messages
  - Customizable prefix
  - MiniMessage format support for rich text
  - Multilingual support through configuration

- **Admin Commands**
  - `/tridentdupefixer reload` - Reload configuration
  - `/tridentdupefixer info` - Display plugin information
  - `/tridentdupefixer help` - Show help information

- **Permissions**
  - `tridentdupefixer.admin` - Access to admin commands

## Event Monitoring

The plugin uses several event listeners to monitor and prevent trident duplication:

- `PlayerLaunchProjectileEvent`
- `PlayerInteractEvent`
- `InventoryClickEvent`
- `PlayerItemHeldEvent`
- `PlayerQuitEvent`
- `InventoryOpenEvent`
- `InventoryCloseEvent`

Thread safety is ensured through the use of ConcurrentHashMap for tracking player states.

## Configuration

```yaml
messages:
  # Should warnings be logged to the console?
  log-to-console: true
  
  # Should warnings be shown to players?
  notify-player: true
  
  # Prefix for player messages (supports MiniMessage format)
  prefix: "<dark_red>[BrilliantTridentDupeFixer]</dark_red>"
  
  # Player warning messages (supports MiniMessage format)
  player:
    hotkey-dupe: "<red>Detected attempted hotkey duplication of trident.</red>"
    inventory-manipulation: "<red>Detected inventory manipulation during trident throw state.</red>"
    hotbar-swap: "<red>Detected hotbar swap with trident.</red>"
  
  # Console warning messages (plain text)
  console:
    hotkey-dupe: "Detected attempted hotkey duplication of trident"
    inventory-manipulation: "Detected inventory manipulation during trident throw state"
    hotbar-swap: "Detected hotbar swap with trident"

# Command messages (supports MiniMessage format)
command-messages:
  # Permission message
  no-permission: "<red>You don't have permission to use this command.</red>"
  
  # Command responses
  config-reloaded: "<green>Plugin configuration has been reloaded.</green>"
  unknown-command: "<red>Unknown command. Use <white>/tdf help</white> to see available commands.</red>"
  
  # Help command messages
  help-header: "<yellow>=== BrilliantTridentDupeFixer Help ===</yellow>"
  help-reload: "<white>/tdf reload</white> <gray>- Reload plugin configuration</gray>"
  help-info: "<white>/tdf info</white> <gray>- Display plugin information</gray>"
  help-help: "<white>/tdf help</white> <gray>- Show this help message</gray>"
```

## Advanced Features

### Container Interaction
The plugin intelligently differentiates between legitimate container usage and duplication attempts. Players can safely:
- Open chests, barrels, shulker boxes
- Access furnaces, hoppers, dispensers
- Use ender chests and other inventories
without triggering false positives.

### Message Formatting
- Player-facing messages support full MiniMessage formatting
- Console messages are displayed as clean plain text
- Command responses use the same prefix as warning messages for consistency

## References

This project combines approaches from two existing trident duplication prevention plugins:

- [NoTridentDupe](https://github.com/patyhank/NoTridentDupe)
  - Adopted the tick-based validation system (`lastProjectileCancel`) to prevent hotbar swap exploits
  - Inherited the use of ConcurrentHashMap for thread-safe operation
  - Integrated inventory close on projectile launch

- [TridentDupeFixer](https://github.com/Null-K/TridentDupeFixer)
  - Adopted the player state tracking system (`playersReadyToThrow`) using UUID sets
  - Integrated the comprehensive event handling for player interactions and item hold changes

## License

GNU General Public License v3.0