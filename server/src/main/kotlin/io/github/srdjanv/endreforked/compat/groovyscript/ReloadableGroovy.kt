package io.github.srdjanv.endreforked.compat.groovyscript

import com.cleanroommc.groovyscript.api.GroovyBlacklist
import com.cleanroommc.groovyscript.api.GroovyLog
import com.cleanroommc.groovyscript.api.IScriptReloadable
import com.cleanroommc.groovyscript.helper.Alias
import com.cleanroommc.groovyscript.registry.NamedRegistry
import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import io.github.srdjanv.endreforked.compat.scripting.ScriptLayer

/**
 * A script property with the reload cycle of GroovyScript. The mod registries do the work, so there is no backup of removed entries:
 * `onReload` drops the recorded operations of the script layer, the scripts record them again, and `afterScriptLoad` rebuilds once.
 * The rebuild sends the new snapshot to the clients (see RegistrySync). Several properties may share one layer, the calls are idempotent.
 */
abstract class ReloadableGroovy<E : RegistryEntry>(aliases: Alias, internal val layer: ScriptLayer<E>) :
    NamedRegistry(aliases), IScriptReloadable {
    @GroovyBlacklist
    override fun onReload() = layer.reset()

    @GroovyBlacklist
    override fun afterScriptLoad() {
        layer.flush()?.errors?.forEach { GroovyLog.get().error("{}: {}", layer.name, it) }
    }
}
