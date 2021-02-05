package dev.brella.khronus.core.patches

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.util.function.Consumer

object KhronusApiTranformer: Consumer<ClassNode> {
    val methodPatches: Map<String, Consumer<MethodNode>> = mapOf(
        "getKhronusTileEntities" to KhronusApiWorldField(WorldTransformer.KHRONUS_TICKABLE_TILE_ENTITIES),
        "getTickAcceleration" to KhronusApiWorldField(WorldTransformer.KHRONUS_TICK_ACCELERATION),
        "getTickLength" to KhronusApiWorldField(WorldTransformer.KHRONUS_TICK_LENGTH),
        "getTickCheckup" to KhronusApiWorldField(WorldTransformer.KHRONUS_TICK_CHECKUPS)
    )

    override fun accept(classNode: ClassNode) {
        println("==Rewriting The Api==")

        classNode.methods.forEach { method ->
            val patcher = methodPatches[method.name] ?: return@forEach
            println("- KhronusApi#${method.name} ${method.desc} now belongs to us!")
            patcher.accept(method)
        }

        println("==/Api Rewritten/==")
    }
}