package dev.brella.khronus.core.patches
//
//import org.objectweb.asm.Opcodes.ACC_PUBLIC
//import org.objectweb.asm.tree.ClassNode
//import org.objectweb.asm.tree.MethodNode
//import java.util.function.Consumer
//
//object WorldTransformer : Consumer<ClassNode> {
//    public const val KHRONUS_TICKABLE_TILE_ENTITIES = "khronusTickableTileEntities"
//    public const val KHRONUS_TICK_ACCELERATION = "tickAcceleration"
//    public const val KHRONUS_TICK_LENGTH = "tickLength"
//    public const val KHRONUS_TICK_CHECKUPS = "tickCheckups"
//
//    val methodPatches: Map<String, Consumer<MethodNode>> = mapOf(
//        "updateEntities" to WorldUpdateEntities,
//        "func_72939_s" to WorldUpdateEntities,
//
//        "addTileEntity" to WorldAddTileEntity,
//        "func_175700_a" to WorldAddTileEntity,
//
//        "removeTileEntity" to WorldRemoveTileEntity,
//        "func_175713_t" to WorldRemoveTileEntity,
//
//        "<init>" to WorldInit
//    )
//
//    override fun accept(classNode: ClassNode) {
//        println("==World Terraforming==")
//
//        println("- Seizing the means of ticking")
//        classNode.visitField(ACC_PUBLIC,
//            KHRONUS_TICKABLE_TILE_ENTITIES,
//            "Ljava/util/Map;",
//            "Ljava/util/Map<Lnet/minecraft/tileentity/TileEntity;Ldev/brella/khronus/TemporalBounds;>;",
//            null).visitEnd()
//
//        println("- Accelerating our realm")
//        classNode.visitField(ACC_PUBLIC,
//            KHRONUS_TICK_ACCELERATION,
//            "Ljava/util/Map;",
//            "Ljava/util/Map<Lnet/minecraft/tileentity/TileEntity;Ljava/lang/Integer;>;",
//            null).visitEnd()
//
//        println("- Getting out a planetary ruler")
//        classNode.visitField(ACC_PUBLIC,
//            KHRONUS_TICK_LENGTH,
//            "Ljava/util/Map;",
//            "Ljava/util/Map<Lnet/minecraft/tileentity/TileEntity;Ljava/lang/Long;>;",
//            null).visitEnd()
//
//        println("- Hiring a solar doctor")
//        classNode.visitField(ACC_PUBLIC,
//            KHRONUS_TICK_CHECKUPS,
//            "Ljava/util/Map;",
//            "Ljava/util/Map<Lnet/minecraft/tileentity/TileEntity;Ljava/lang/Long;>;",
//            null).visitEnd()
//
//
//        classNode.methods.forEach { method ->
//            val patcher = methodPatches[method.name] ?: return@forEach
//            println("- World#${method.name} ${method.desc} now belongs to us!")
//            patcher.accept(method)
//        }
//
//        println("==/World Terraformed/==")
//    }
//}