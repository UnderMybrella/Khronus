package dev.brella.khronus.core.patches

import dev.brella.khronus.core.InstructionList
import dev.brella.khronus.core.buildInstructionList
import dev.brella.khronus.core.toTextRepresentation
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.function.Consumer

object WorldInit : Consumer<MethodNode> {
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
            var lineNumber = 116

            for (name in arrayOf(WorldTransformer.DELAYED_TICKABLE_TILE_ENTITIES, WorldTransformer.KHRONUS_TICKABLE_TILE_ENTITIES, WorldTransformer.KHRONUS_TICK_ACCELERATION, WorldTransformer.KHRONUS_TICK_LENGTH, WorldTransformer.KHRONUS_TICK_CHECKUPS)) {
                val label = LabelNode()

                add(label)
                add(LineNumberNode(lineNumber++, label))

                aload(0)
                new("java/util/WeakHashMap")
                dup()
                invokeSpecial("java/util/WeakHashMap", "<init>", "()V")
                putField("net/minecraft/world/World", name, "Ljava/util/Map;")
            }
        }

    override fun accept(method: MethodNode) {
        println("World#init: ")
        method.instructions.iterator().forEach { println(it.toTextRepresentation()) }

        println("///Transforming///")

        try {
            method.instructions.iterator().forEach { node ->
                if (node.opcode == Opcodes.PUTFIELD) {
                    println("Inserting @ $node")
                    method.instructions.insert(node, newInstructions)
                    return
                }
            }
        } finally {
            println("Post Transformation: ")
            method.instructions.iterator().forEach { println(it.toTextRepresentation()) }
        }
    }
}