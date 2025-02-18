# BrilliantTridentDupeFixer

The plugin uses several event listeners to monitor and prevent trident duplication:

- `PlayerLaunchProjectileEvent`
- `PlayerInteractEvent`
- `InventoryClickEvent`
- `PlayerItemHeldEvent`
- `PlayerQuitEvent`

Thread safety is ensured through the use of ConcurrentHashMap for tracking player states.

## References

This project combines approaches from two existing trident duplication prevention plugins:

- [NoTridentDupe](https://github.com/patyhank/NoTridentDupe)
  - Adopted the tick-based validation system (`lastProjectileCancel`) to prevent hotbar swap exploits
  - Inherited the use of ConcurrentHashMap for thread-safe operation
  - Integrated inventory close on projectile launch

- [TridentDupeFixer](https://github.com/Null-K/TridentDupeFixer)
  - Adopted the player state tracking system (`playersReadyToThrow`) using UUID sets
  - Integrated the comprehensive event handling for player interactions and item hold changes

Additional improvements in this implementation:
- Enhanced thread safety by using ConcurrentHashMap's `newKeySet()` for player state tracking
- Added detailed warning logs with player names and specific exploit attempts
- Implemented event priority handling (LOWEST) for inventory clicks
- Separated event listener logic into a dedicated class for better code organization
- Added proper cleanup on plugin disable
- Improved null-safety checks throughout the codebase

## License

GNU General Public License v3.0