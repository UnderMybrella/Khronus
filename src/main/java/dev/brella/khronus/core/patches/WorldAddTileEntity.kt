package dev.brella.khronus.core.patches
//
//import dev.brella.khronus.core.buildAsmPattern
//import dev.brella.khronus.core.buildInstructionList
//import org.objectweb.asm.tree.*
//import java.util.function.Consumer
//
//object WorldAddTileEntity : Consumer<MethodNode> {
//    /*
//                         * L3
//                         *     LINENUMBER 1920 L3
//                         *     ILOAD 2
//                         *     IFEQ L4
//                         *     ALOAD 1
//                         *     INSTANCEOF net/minecraft/util/ITickable
//                         *     IFEQ L4
//                         *    L5
//                         *     LINENUMBER 1922 L5
//                         *     ALOAD 0
//                         *     GETFIELD net/minecraft/world/World.tickableTileEntities : Ljava/util/List;
//                         *     ALOAD 1
//                         *     INVOKEINTERFACE java/util/List.add (Ljava/lang/Object;)Z (itf)
//                         *     POP
//                         *    L4
//                         */
//
//    /*
//     *     ALOAD 1
//     *     INSTANCEOF net/minecraft/util/ITickable
//     *     IFEQ L4
//     */
//    val checkPattern = buildAsmPattern {
//        aload(1)
//        instanceOf("net/minecraft/util/ITickable")
//        ifEq()
//    }
//
//    /*
//     *     ALOAD 0
//     *     GETFIELD net/minecraft/world/World.tickableTileEntities : Ljava/util/List;
//     *     ALOAD 1
//     *     INVOKEINTERFACE java/util/List.add (Ljava/lang/Object;)Z (itf)
//     *     POP
//     */
//    val blockPattern = buildAsmPattern {
//        aload(0)
//        getField("net/minecraft/world/World", "tickableTielEntities", "Ljava/util/List;")
//        aload(1)
//        invokeInterface("java/util/List", "add", "(Ljava/lang/Object;)Z")
//        pop()
//    }
//
//    /*
//     GETSTATIC dev/brella/khronus/TickDog.watchdog : Ldev/brella/khronus/watchdogs/KhronusWatchdog;
//     ALOAD 1
//     INVOKEINTERFACE .updateEntities  (itf)
//     */
//    val replacement by lazy {
//        buildInstructionList {
//            add(KhronusTransformer.getWatchdog())
//            aload(1)
//            invokeInterface("dev/brella/khronus/watchdogs/KhronusWatchdog",
//                "addTileEntity",
//                "(Lnet/minecraft/world/World;Lnet/minecraft/tileentity/TileEntity;)V")
//        }
//    }
//
//    override fun accept(method: MethodNode) {
//        var checkStart = -1
//        var blockStart = -1
//
//        var i = 0
//        while (i < method.instructions.size()) {
//            if (checkStart == -1) {
//                if (checkPattern.matches(method.instructions, i)) {
//                    checkStart = i
//                    i += checkPattern.size
//                }
//            }
//
//            if (blockStart == -1) {
//                if (blockPattern.matches(method.instructions, i)) {
//                    blockStart = i
//                    break
//                }
//            }
//
//            i++
//        }
//
//        if (blockStart != -1 && checkStart != -1) {
//            println("Got our blocks: $checkStart,$blockStart..")
//            println("Brace yourselves...")
//
//            val check = Array<AbstractInsnNode>(3) { method.instructions[checkStart + it] }
//            val block = Array<AbstractInsnNode>(5) { method.instructions[blockStart + it] }
//            val suffix = block[block.size - 1].next
//
//            check.forEach(method.instructions::remove)
//            block.forEach(method.instructions::remove)
//
//            println("Removed instructions")
//
//            method.instructions.insertBefore(suffix, replacement)
//        }
//    }
//}