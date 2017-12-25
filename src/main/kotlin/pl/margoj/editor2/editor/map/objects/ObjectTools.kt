package pl.margoj.editor2.editor.map.objects

import pl.margoj.editor2.editor.map.objects.tools.GatewayObjectTool
import pl.margoj.editor2.editor.map.objects.tools.NpcObjectTool
import pl.margoj.editor2.editor.map.objects.tools.SpawnPointObjectTool
import pl.margoj.mrf.map.objects.MapObject

object ObjectTools
{
    private var tools_ = arrayListOf<ObjectTool<*>>()

    val tools: Collection<ObjectTool<*>> = this.tools_

    init
    {
        this.registerTool(GatewayObjectTool())
        this.registerTool(SpawnPointObjectTool())
        this.registerTool(NpcObjectTool())
    }

    fun registerTool(tool: ObjectTool<*>)
    {
        this.tools_.add(tool)
    }

    fun unregisterTool(tool: ObjectTool<*>)
    {
        this.tools_.remove(tool)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: MapObject<T>> getFor(mapObject: T): ObjectTool<T>?
    {
        return this.tools.firstOrNull { it.mapObjectType.isInstance(mapObject) } as ObjectTool<T>?
    }

    @Suppress("UNCHECKED_CAST")
    fun getForRaw(mapObject: MapObject<*>): ObjectTool<*>?
    {
        return this.tools.firstOrNull { it.mapObjectType.isInstance(mapObject) }
    }
}