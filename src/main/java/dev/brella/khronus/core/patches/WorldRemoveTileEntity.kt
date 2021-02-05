package dev.brella.khronus.core.patches

import dev.brella.khronus.core.KhronusTransformer
import dev.brella.khronus.core.KhronusTransformer.getWatchdog
import dev.brella.khronus.core.buildAsmPattern
import dev.brella.khronus.core.buildInstructionList
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.function.Consumer

object WorldRemoveTileEntity : Consumer<MethodNode> {
    /*
     *     ALOAD 0
     *     GETFIELD net/minecraft/world/World.tickableTileEntities : Ljava/util/List;
     *     ALOAD 2
     *     INVOKEINTERFACE java/util/List.remove (Ljava/lang/Object;)Z (itf)
     *     POP
     */
    val blockPattern = buildAsmPattern {
        aload(0)
        getField("net/minecraft/world/World", "tickableTileEntities", "Ljava/util/List;")
        aload(2)
        invokeInterface("java/util/List", "remove", "(Ljava/lang/Object;)Z")
        pop()
    }

    val replacement by lazy {
        buildInstructionList {
            add(getWatchdog())
            aload(0)
            aload(1)
            aload(2)
            invokeInterface("dev/brella/khronus/watchdogs/KhronusWatchdog",
                "removeTileEntity",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V")
        }
    }

    override fun accept(method: MethodNode) {
        var blockStart = -1

        for (i in 0 until method.instructions.size()) {
            if (blockPattern.matches(method.instructions, i)) {
                blockStart = i
                break
            }
        }

        if (blockStart != -1) {
            println("Got our block: $blockStart")
            println("Brace yourselves...")

            val block = Array<AbstractInsnNode>(5) { method.instructions[blockStart + it] }
            val suffix = block[block.size - 1].next
            block.forEach(method.instructions::remove)

            println("Removed instructions")
            method.instructions.insertBefore(suffix, replacement)
        }
    }
}