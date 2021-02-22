package dev.brella.khronus.core.patches

import dev.brella.khronus.core.KhronusTransformer
import dev.brella.khronus.core.buildAsmPattern
import dev.brella.khronus.core.buildInstructionList
import dev.brella.khronus.core.expecting
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.function.Consumer

class KhronusApiWorldField(fieldName: String) : Consumer<MethodNode> {
    val newInstructions = buildInstructionList {
        aload(0) //World
        add(FieldInsnNode(Opcodes.GETFIELD,
            "net/minecraft/world/World",
            fieldName,
            "Ljava/util/Map;"))
        add(InsnNode(Opcodes.ARETURN))
    }

    override fun accept(method: MethodNode) {
        method.instructions.clear()
        method.instructions.add(newInstructions)
    }
}