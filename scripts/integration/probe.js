// Test-only KubeJS script, copied into the test server by scripts/integration-test.sh.
// Calls the real Banhammer code with a fake player, to prove that a node set in LuckPerms
// reaches a Fabric mod through the bridge. Never ships with the mod or the pack.
ServerEvents.commandRegistry(event => {
  const Commands = event.commands
  event.register(Commands.literal('zzprobe').executes(ctx => {
    const uuid = Java.loadClass('java.util.UUID').fromString('11111111-1111-1111-1111-111111111111')
    const FakePlayerFactory = Java.loadClass('net.neoforged.neoforge.common.util.FakePlayerFactory')
    const GameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const fake = FakePlayerFactory.get(ctx.source.server.overworld(), new GameProfile(uuid, 'zzprobe'))
    const ConfigManager = Java.loadClass('eu.pb4.banhammer.impl.config.ConfigManager')
    const limit = ConfigManager.getConfig().getDurationLimit(fake.createCommandSourceStack())
    // Long.MAX_VALUE means "no limit"; JavaScript numbers cannot hold it exactly.
    console.log('ZZPROBE banhammer.durationLimit=' + (limit > 1e18 ? 'unlimited' : limit))
    return 1
  }))
})
