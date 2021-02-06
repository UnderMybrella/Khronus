package dev.brella.khronus.core.patches

//import org.objectweb.asm.tree.*
//import java.util.*
//import java.util.function.Consumer
//
//object WorldInit : Consumer<MethodNode> {
//    /*
//    ALOAD 0
//    INVOKESTATIC com/google/common/collect/Maps.newConcurrentMap ()Ljava/util/concurrent/ConcurrentMap;
//    PUTFIELD net/minecraft/world/World.loadedEntityList : Ljava/util/List;
//    */
//
//    /**
//     * NEW java/util/WeakHashMap
//    DUP
//    INVOKESPECIAL java/util/WeakHashMap.<init> ()V
//     */
//
//    inline fun InstructionList.weakHashMapInit() {
//        new("java/util/WeakHashMap")
//        dup()
//        invokeSpecial("java/util/WeakHashMap", "<init>", "()V")
//    }
//
//    inline fun InstructionList.concurrentHashMapInit() {
//        add(MethodInsnNode(Opcodes.INVOKESTATIC,
//            "com/google/common/collect/Maps",
//            "newConcurrentMap",
//            "()Ljava/util/concurrent/ConcurrentMap;",
//            false))
//    }
//
//    val newInstructions = buildInstructionList {
//        aload(0)
//        putField("net/minecraft/world/World", WorldTransformer.KHRONUS_TICKABLE_TILE_ENTITIES, "Ljava/util/Map;") { weakHashMapInit() }
//
//        aload(0)
//        putField("net/minecraft/world/World", WorldTransformer.KHRONUS_TICK_ACCELERATION, "Ljava/util/Map;") { weakHashMapInit() }
//
//        aload(0)
//        putField("net/minecraft/world/World", WorldTransformer.KHRONUS_TICK_LENGTH, "Ljava/util/Map;") { weakHashMapInit() }
//
//        aload(0)
//        putField("net/minecraft/world/World", WorldTransformer.KHRONUS_TICK_CHECKUPS, "Ljava/util/Map;") { weakHashMapInit() }
//    }
//
//    override fun accept(method: MethodNode) {
//        method.instructions.insertBefore(method.instructions.last.previous, newInstructions)
//    }
//}