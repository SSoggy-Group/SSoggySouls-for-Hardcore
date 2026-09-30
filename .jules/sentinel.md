
## 2026-03-29 - Avoid Manual Command Re-execution via performCommand
**Vulnerability:** Asynchronous command interception in `PlayerCommandPreprocessEvent` cancelled the original command event and later called `player.performCommand(commandToRun)` asynchronously for alive players/visitors. This allowed command injection/bypasses.
**Learning:** Re-executing raw player commands via `performCommand` inside event handlers can bypass security checks and command preprocessing pipelines.
**Prevention:** Instead of cancelling the event and calling `performCommand` later, evaluate player permissions/state synchronously during `PlayerCommandPreprocessEvent` and allow non-restricted events to proceed through standard Bukkit command handling naturally.
