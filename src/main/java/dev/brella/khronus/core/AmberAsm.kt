package dev.brella.khronus.core

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.function.Predicate


/*
    'V' - void
    'Z' - boolean
    'C' - char
    'B' - byte
    'S' - short
    'I' - int
    'F' - float
    'J' - long
    'D' - double
 */

public const val TYPE_VOID = 'V'
public const val TYPE_BOOLEAN = 'Z'
public const val TYPE_CHAR = 'C'
public const val TYPE_BYTE = 'B'
public const val TYPE_SHORT = 'S'
public const val TYPE_INT = 'I'
public const val TYPE_FLOAT = 'F'
public const val TYPE_LONG = 'J'
public const val TYPE_DOUBLE = 'D'

class InstructionList(val backing: InsnList = InsnList()) {
    inline fun add(node: AbstractInsnNode) = apply { backing.add(node) }
    inline fun add(opcode: Int) = add(InsnNode(opcode))
    inline fun addType(opcode: Int, desc: String) = add(TypeInsnNode(opcode, desc))
    inline fun addMethod(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) =
        add(MethodInsnNode(opcode, owner, name, desc, itf))

    inline fun addField(opcode: Int, owner: String, name: String, desc: String) =
        add(FieldInsnNode(opcode, owner, name, desc))

    inline fun dup() = add(Opcodes.DUP)

    inline fun aload(variable: Int) = add(VarInsnNode(Opcodes.ALOAD, variable))

    inline fun invokeInterface(owner: String, name: String, desc: String, ownerIsInterface: Boolean = true) =
        addMethod(Opcodes.INVOKEINTERFACE, owner, name, desc, ownerIsInterface)

    inline fun invokeSpecial(owner: String, name: String, desc: String, ownerIsInterface: Boolean = false) =
        addMethod(Opcodes.INVOKESPECIAL, owner, name, desc, ownerIsInterface)

    inline fun new(type: String) = addType(Opcodes.NEW, type)

    inline fun putField(owner: String, name: String, desc: String) = addField(Opcodes.PUTFIELD, owner, name, desc)

    inline fun putField(owner: String, name: String, desc: String, init: InstructionList.() -> Unit) = try {
        init()
    } finally {
        addField(Opcodes.PUTFIELD, owner, name, desc)
    }
}

inline fun buildInstructionList(builder: InstructionList.() -> Unit): InsnList =
    InstructionList().apply(builder).backing

object ExpectingSelector {
    inline fun <T : AbstractInsnNode> expect(
        opcode: Int,
        crossinline filter: (node: T) -> Boolean
    ): Predicate<AbstractInsnNode> = Predicate { node -> node.opcode == opcode && filter(node as T) }

    inline fun aload(variable: Int) = expect<VarInsnNode>(Opcodes.ALOAD) { it.`var` == variable }

    inline fun getField(owner: String) = expect<FieldInsnNode>(Opcodes.GETFIELD) { it.owner == owner }
    inline fun getField(owner: String, fieldName: String) =
        expect<FieldInsnNode>(Opcodes.GETFIELD) { it.owner == owner && it.name == fieldName }

    inline fun getField(owner: String, fieldNames: Array<String>) =
        expect<FieldInsnNode>(Opcodes.GETFIELD) { it.owner == owner && it.name in fieldNames }

    inline fun getField(owner: String, fieldName: String, desc: String) =
        expect<FieldInsnNode>(Opcodes.GETFIELD) { it.owner == owner && it.name == fieldName && it.desc == desc }

    inline fun getField(owner: String, fieldNames: Array<String>, desc: String) =
        expect<FieldInsnNode>(Opcodes.GETFIELD) { it.owner == owner && it.desc == desc && it.name in fieldNames }

    inline fun putField(owner: String) = expect<FieldInsnNode>(Opcodes.PUTFIELD) { it.owner == owner }
    inline fun putField(owner: String, fieldName: String) =
        expect<FieldInsnNode>(Opcodes.PUTFIELD) { it.owner == owner && it.name == fieldName }

    inline fun putField(owner: String, fieldNames: Array<String>) =
        expect<FieldInsnNode>(Opcodes.PUTFIELD) { it.owner == owner && it.name in fieldNames }

    inline fun putField(owner: String, fieldName: String, desc: String) =
        expect<FieldInsnNode>(Opcodes.PUTFIELD) { it.owner == owner && it.name == fieldName && it.desc == desc }

    inline fun putField(owner: String, fieldNames: Array<String>, desc: String) =
        expect<FieldInsnNode>(Opcodes.PUTFIELD) { it.owner == owner && it.desc == desc && it.name in fieldNames }


    inline fun invokeInterface(owner: String) =
        expect<MethodInsnNode>(Opcodes.INVOKEINTERFACE) { it.itf && it.owner == owner }

    inline fun invokeInterface(owner: String, methodName: String) =
        expect<MethodInsnNode>(Opcodes.INVOKEINTERFACE) { it.itf && it.owner == owner && it.name == methodName }

    inline fun invokeInterface(owner: String, methodNames: Array<String>) =
        expect<MethodInsnNode>(Opcodes.INVOKEINTERFACE) { it.itf && it.owner == owner && it.name in methodNames }

    inline fun invokeInterface(owner: String, methodName: String, desc: String) =
        expect<MethodInsnNode>(Opcodes.INVOKEINTERFACE) { it.itf && it.owner == owner && it.name == methodName && it.desc == desc }

    inline fun invokeInterface(owner: String, methodNames: Array<String>, desc: String) =
        expect<MethodInsnNode>(Opcodes.INVOKEINTERFACE) { it.itf && it.owner == owner && it.desc == desc && it.name in methodNames }
}

inline fun expecting(selector: ExpectingSelector.() -> Predicate<AbstractInsnNode>): Predicate<AbstractInsnNode> =
    ExpectingSelector.selector()

class AsmPattern(val predicates: Array<out Predicate<AbstractInsnNode>>) {
    companion object {
        operator fun invoke(vararg predicates: Predicate<AbstractInsnNode>) = AsmPattern(predicates)
    }

    inline val size get() = predicates.size

    fun matches(list: InsnList, start: Int): Boolean {
        predicates.forEachIndexed { index, predicate ->
            if (!predicate.test(list[start + index])) return false
        }

        return true
    }
}

class PatternBuilder(val patterns: MutableList<Predicate<AbstractInsnNode>> = ArrayList()) {
    inline fun add(pattern: Predicate<AbstractInsnNode>) = patterns.add(pattern)
    inline fun add(selector: ExpectingSelector.() -> Predicate<AbstractInsnNode>) =
        patterns.add(ExpectingSelector.selector())

    inline fun add(opcode: Int) = patterns.add(Predicate { it.opcode == opcode })

    inline fun frame() = add(Predicate { it is FrameNode })

    inline fun aload(variable: Int) = add { aload(variable) }

    inline fun getField(owner: String) = add { getField(owner) }
    inline fun getField(owner: String, fieldName: String) = add { getField(owner, fieldName) }

    inline fun getField(owner: String, fieldNames: Array<String>) = add { getField(owner, fieldNames) }

    inline fun getField(owner: String, fieldName: String, desc: String) = add { getField(owner, fieldName, desc) }

    inline fun getField(owner: String, fieldNames: Array<String>, desc: String) =
        add { getField(owner, fieldNames, desc) }


    inline fun putField(owner: String) = add { putField(owner) }
    inline fun putField(owner: String, fieldName: String) = add { putField(owner, fieldName) }

    inline fun putField(owner: String, fieldNames: Array<String>) = add { putField(owner, fieldNames) }

    inline fun putField(owner: String, fieldName: String, desc: String) = add { putField(owner, fieldName, desc) }

    inline fun putField(owner: String, fieldNames: Array<String>, desc: String) =
        add { putField(owner, fieldNames, desc) }


    inline fun invokeInterface(owner: String) = add { invokeInterface(owner) }

    inline fun invokeInterface(owner: String, methodName: String) = add { invokeInterface(owner, methodName) }

    inline fun invokeInterface(owner: String, methodNames: Array<String>) = add { invokeInterface(owner, methodNames) }

    inline fun invokeInterface(owner: String, methodName: String, desc: String) =
        add { invokeInterface(owner, methodName, desc) }

    inline fun invokeInterface(owner: String, methodNames: Array<String>, desc: String) =
        add { invokeInterface(owner, methodNames, desc) }

    inline fun instanceOf(desc: String) = add { expect<TypeInsnNode>(Opcodes.INSTANCEOF) { it.desc == desc } }

    inline fun ifEq() = add(Opcodes.IFEQ)

    inline fun iconst0() = add(Opcodes.ICONST_0)

    inline fun pop() = add(Opcodes.POP)
}

inline fun buildAsmPattern(block: PatternBuilder.() -> Unit): AsmPattern =
    AsmPattern(PatternBuilder().apply(block).patterns.toTypedArray())