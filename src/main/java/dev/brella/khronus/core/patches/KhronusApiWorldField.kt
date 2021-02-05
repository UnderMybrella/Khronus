package dev.brella.khronus.core.patches

import dev.brella.khronus.core.KhronusTransformer
import dev.brella.khronus.core.buildAsmPattern
import dev.brella.khronus.core.buildInstructionList
import dev.brella.khronus.core.expecting
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.function.Consumer

class KhronusApiWorldField(fieldName: String) : Consumer<MethodNode> {
    val pattern = expecting { expect<MethodInsnNode>(Opcodes.INVOKESTATIC) { it.owner == "java/util/Collections" && it.name == "emptyMap" } }

    val newInstructions = buildInstructionList {
        aload(0) //World
        add(FieldInsnNode(Opcodes.GETFIELD,
            "net/minecraft/world/World",
            fieldName,
            "Ljava/util/Map;"))
    }

    override fun accept(method: MethodNode) {
        var instruction: AbstractInsnNode

        for (i in 0 until method.instructions.size()) {
            instruction = method.instructions.get(i)
            //INVOKESTATIC java/util/Collections.emptyMap ()Ljava/util/Map;
            if (pattern.test(instruction)) {
                if (method.instructions.get(i + 1).opcode == Opcodes.ARETURN) {
                    println("Got our block: $i")
                    println("Brace yourselves...")

                    val suffix = instruction.next
                    method.instructions.remove(instruction)
                    method.instructions.insertBefore(suffix, newInstructions)

                    break
                }
            }
        }
    }
}