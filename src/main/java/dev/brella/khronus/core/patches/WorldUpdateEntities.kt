package dev.brella.khronus.core.patches
//
//import dev.brella.khronus.core.KhronusTransformer.getWatchdog
//import dev.brella.khronus.core.buildAsmPattern
//import dev.brella.khronus.core.buildInstructionList
//import org.objectweb.asm.Opcodes
//import org.objectweb.asm.tree.*
//import java.util.HashMap
//import java.util.function.Consumer
//
//object WorldUpdateEntities : Consumer<MethodNode> {
//    val blockStartPattern = buildAsmPattern {
//        frame()
//        aload(0)
//        getField("net/minecraft/world/World", fieldNames = arrayOf("tickableTileEntities", "field_175730_i"))
//        invokeInterface("java/util/List", "iterator")
//    }
//
//    val blockEndPattern = buildAsmPattern {
//        frame()
//        aload(0)
//        iconst0()
//        putField("net/minecraft/world/World", fieldNames = arrayOf("processingLoadedTiles", "field_147481_N"))
//    }
//
//    val replacement by lazy {
//        buildInstructionList {
//            add(getWatchdog())
//            aload(0)
//            invokeInterface("dev/brella/khronus/watchdogs/KhronusWatchdog",
//                "updateEntities",
//                "(Lnet/minecraft/world/World;)V",
//                true)
//        }
//    }
//
//    override fun accept(method: MethodNode) {
//        var blockStart = -1
//        var blockEnd = -1
//
//        val labels = HashMap<LabelNode, Int>(16)
//        var instruction: AbstractInsnNode
//        var i = 0
//        while (i < method.instructions.size()) {
//            instruction = method.instructions.get(i)
//
//            if (instruction is LabelNode) labels[instruction] = i
//
//            if (blockStart == -1) {
//                if (this.blockStartPattern.matches(method.instructions, i)) {
//                    blockStart = i + 1
//                    i += this.blockStartPattern.size
//                }
//            } else if (blockEnd == -1) {
//                if (this.blockEndPattern.matches(method.instructions, i)) {
//                    blockEnd = i
//                    break
//                }
//            }
//            i++
//        }
//
//        if (blockStart != -1 && blockEnd != -1) {
//            println("Got our block: $blockStart .. $blockEnd")
//            val nodes =
//                Array<AbstractInsnNode>(blockEnd - blockStart) { nodeI -> method.instructions.get(nodeI + blockStart) }
//            println("Brace yourselves...")
//            var startNode: Int
//            var endNode: Int
//            method.localVariables.toTypedArray().forEach { localVariable ->
//                startNode = labels[localVariable.start] ?: return@forEach
//                endNode = labels[localVariable.end] ?: return@forEach
//                if (startNode > blockStart && endNode < blockEnd) {
//                    println("Removing " + localVariable.name + "; it's only present in our block")
//                    method.localVariables.remove(localVariable)
//                } else if (startNode in (blockStart + 1) until blockEnd && endNode >= blockEnd) {
//                    if (localVariable.end.next == null) {
//                        println("Removing " + localVariable.name + "; Its a functional scope but it's only used in our block")
//                        method.localVariables.remove(localVariable)
//                    } else {
//                        println("Uho, not sure how to handle " + localVariable.name + " starting in our block but ending outside it!")
//                    }
//                } else if (startNode <= blockStart && endNode > blockStart && endNode < blockEnd) {
//                    println("Wah, not sure how we're gonna do " + localVariable.name + " since it starts before our block but ends inside it!")
//                }
//            }
//            var tryCatch: TryCatchBlockNode
//            for (i in method.tryCatchBlocks.indices) {
//                tryCatch = method.tryCatchBlocks[i]
//                startNode = labels[tryCatch.start] ?: continue
//                endNode = labels[tryCatch.end] ?: continue
//                if (startNode > blockStart && endNode < blockEnd) {
//                    println("Removing " + tryCatch.type + "; it's only present in our block")
//                    method.tryCatchBlocks.remove(tryCatch)
//                } else if (startNode > blockStart && startNode < blockEnd && endNode!! >= blockEnd) {
//                    println("Uho, not sure how to handle " + tryCatch.type + " starting in our block but ending outside it!")
//                } else if (startNode <= blockStart && endNode!! > blockStart && endNode < blockEnd) {
//                    println("Wah, not sure how we're gonna do " + tryCatch.type + " since it starts before our block but ends inside it!")
//                }
//            }
//            nodes.forEach(method.instructions::remove)
//            method.instructions.insertBefore(method.instructions.get(blockStart), replacement)
//        }
//    }
//}