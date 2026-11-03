# Holder snapshot integration check

This test uses the real supplied 26.2 core, a real world chest and CraftInventory,
and the registered FancyFirework menu listener. Only the player and inventory view
are synthetic proxies. It does not simulate Minecraft client packets.

`SnapshotAgent` observes entry to the real core's `createSnapshot` method using
the Java 25 classfile API. `SnapshotCounter` is bootstrap-visible because the
bundled core and Bukkit plugins use separate class loaders. The observer never
changes a snapshot's return value. Counter control checks require an ordinary
`getHolder()` to create exactly one snapshot and `getHolder(false)` to create none.

Prerequisites: JDK 25, the supplied Shiroha/Mizuki 26.2 bundler, a separate local
test server with unpacked libraries, and JetBrains annotations in the local Maven
cache. Keep the test server bound to loopback on an unused port. The test builds
are separate from the production JAR.

From the repository root:

```powershell
./tests/holder-probe/Build-Probe.ps1 -RuntimeDir '<test-server>' -OutputDir '<test-build>'
```

Install one FancyFirework build in the test server's plugins directory. Start the
server from that directory with the observer:

```powershell
& 'D:/java/bin/java.exe' '-javaagent:<test-build>/snapshot-agent.jar=<test-build>/snapshot-counter.jar' -Xms512M -Xmx2G -jar '<supplied-bundler>' nogui
```

Run `holderprobe` from the console. The suite checks ordinary chest
click/drag/close/quit snapshots, real item grants (blue battery fixture: left 1,
middle 64, Shift+left 64), permission refusal, full inventory refusal, cancelled
events, drag/Shift/double-click blocking, preview identity removal, and menu
cleanup. It creates a test chest at `(8,80,8)` in the isolated overworld.

The old -6 build produces four expected snapshot-check failures. The fixed -7
build produces `HOLDER_RESULT passed=25 failed=0`. Stop normally before swapping
the plugin JAR. The 2026-11-04 test logs and exact hashes are in
`audit/fancyfirework-holder-test-20261104` beside this repository.

The original incident chest NBT was not supplied. The nested item-text sample is
a synthetic fixture; it does not reproduce or measure the original 5.23-second
stall. Real client interaction and PlugManX reload are not covered by this suite.
