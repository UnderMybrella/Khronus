package dev.brella.khronus.core.patches

import dev.brella.khronus.core.InstructionList
import dev.brella.khronus.core.buildInstructionList
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.function.Consumer

object KhronusWorldInit : Consumer<MethodNode> {
    /*
    ALOAD 0
    INVOKESTATIC com/google/common/collect/Maps.newConcurrentMap ()Ljava/util/concurrent/ConcurrentMap;
    PUTFIELD net/minecraft/world/World.loadedEntityList : Ljava/util/List;
    */

    /**
     * NEW java/util/WeakHashMap
    DUP
    INVOKESPECIAL java/util/WeakHashMap.<init> ()V
     */

    inline fun InstructionList.weakHashMapInit() {
        new("java/util/WeakHashMap")
        dup()
        invokeSpecial("java/util/WeakHashMap", "<init>", "()V")
    }

    inline fun InstructionList.concurrentHashMapInit() {
        add(MethodInsnNode(Opcodes.INVOKESTATIC,
            "com/google/common/collect/Maps",
            "newConcurrentMap",
            "()Ljava/util/concurrent/ConcurrentMap;",
            false))
    }

    val newInstructions
        get() = buildInstructionList {
            aload(0)
            putField("net/minecraft/world/World",
                WorldTransformer.DELAYED_TICKABLE_TILE_ENTITIES,
                "Ljava/util/Map;") { weakHashMapInit() }

            aload(0)
            putField("net/minecraft/world/World",
                WorldTransformer.KHRONUS_TICK_ACCELERATION,
                "Ljava/util/Map;") { weakHashMapInit() }

            aload(0)
            putField("net/minecraft/world/World",
                WorldTransformer.KHRONUS_TICK_LENGTH,
                "Ljava/util/Map;") { weakHashMapInit() }

            aload(0)
            putField("net/minecraft/world/World",
                WorldTransformer.KHRONUS_TICK_CHECKUPS,
                "Ljava/util/Map;") { weakHashMapInit() }
        }

    override fun accept(method: MethodNode) {
        method.instructions.clear()
        method.instructions.add(newInstructions)
        method.instructions.insert(newInstructions)
    }
}