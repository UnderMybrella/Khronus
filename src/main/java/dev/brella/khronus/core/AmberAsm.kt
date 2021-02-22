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

val OPCODES_TO_STRING = arrayOf(
    "NOP",
    "ACONST_NULL",
    "ICONST_M1",
    "ICONST_0",
    "ICONST_1",
    "ICONST_2",
    "ICONST_3",
    "ICONST_4",
    "ICONST_5",
    "LCONST_0",
    "LCONST_1",
    "FCONST_0",
    "FCONST_1",
    "FCONST_2",
    "DCONST_0",
    "DCONST_1",
    "BIPUSH",
    "SIPUSH",
    "LDC",
    "PRIV_LDC_W",
    "PRIV_LDC2_W",
    "ILOAD",
    "LLOAD",
    "FLOAD",
    "DLOAD",
    "ALOAD",
    "PRIV_ILOAD_0",
    "PRIV_ILOAD_1",
    "PRIV_ILOAD_2",
    "PRIV_ILOAD_3",
    "PRIV_LLOAD_0",
    "PRIV_LLOAD_1",
    "PRIV_LLOAD_2",
    "PRIV_LLOAD_3",
    "PRIV_FLOAD_0",
    "PRIV_FLOAD_1",
    "PRIV_FLOAD_2",
    "PRIV_FLOAD_3",
    "PRIV_DLOAD_0",
    "PRIV_DLOAD_1",
    "PRIV_DLOAD_2",
    "PRIV_DLOAD_3",
    "PRIV_ALOAD_0",
    "PRIV_ALOAD_1",
    "PRIV_ALOAD_2",
    "PRIV_ALOAD_3",
    "IALOAD",
    "LALOAD",
    "FALOAD",
    "DALOAD",
    "AALOAD",
    "BALOAD",
    "CALOAD",
    "SALOAD",
    "ISTORE",
    "LSTORE",
    "FSTORE",
    "DSTORE",
    "ASTORE",
    "PRIV_ISTORE_0",
    "PRIV_ISTORE_1",
    "PRIV_ISTORE_2",
    "PRIV_ISTORE_3",
    "PRIV_LSTORE_0",
    "PRIV_LSTORE_1",
    "PRIV_LSTORE_2",
    "PRIV_LSTORE_3",
    "PRIV_FSTORE_0",
    "PRIV_FSTORE_1",
    "PRIV_FSTORE_2",
    "PRIV_FSTORE_3",
    "PRIV_DSTORE_0",
    "PRIV_DSTORE_1",
    "PRIV_DSTORE_2",
    "PRIV_DSTORE_3",
    "PRIV_ASTORE_0",
    "PRIV_ASTORE_1",
    "PRIV_ASTORE_2",
    "PRIV_ASTORE_3",
    "IASTORE",
    "LASTORE",
    "FASTORE",
    "DASTORE",
    "AASTORE",
    "BASTORE",
    "CASTORE",
    "SASTORE",
    "POP",
    "POP2",
    "DUP",
    "DUP_X1",
    "DUP_X2",
    "DUP2",
    "DUP2_X1",
    "DUP2_X2",
    "SWAP",
    "IADD",
    "LADD",
    "FADD",
    "DADD",
    "ISUB",
    "LSUB",
    "FSUB",
    "DSUB",
    "IMUL",
    "LMUL",
    "FMUL",
    "DMUL",
    "IDIV",
    "LDIV",
    "FDIV",
    "DDIV",
    "IREM",
    "LREM",
    "FREM",
    "DREM",
    "INEG",
    "LNEG",
    "FNEG",
    "DNEG",
    "ISHL",
    "LSHL",
    "ISHR",
    "LSHR",
    "IUSHR",
    "LUSHR",
    "IAND",
    "LAND",
    "IOR",
    "LOR",
    "IXOR",
    "LXOR",
    "IINC",
    "I2L",
    "I2F",
    "I2D",
    "L2I",
    "L2F",
    "L2D",
    "F2I",
    "F2L",
    "F2D",
    "D2I",
    "D2L",
    "D2F",
    "I2B",
    "I2C",
    "I2S",
    "LCMP",
    "FCMPL",
    "FCMPG",
    "DCMPL",
    "DCMPG",
    "IFEQ",
    "IFNE",
    "IFLT",
    "IFGE",
    "IFGT",
    "IFLE",
    "IF_ICMPEQ",
    "IF_ICMPNE",
    "IF_ICMPLT",
    "IF_ICMPGE",
    "IF_ICMPGT",
    "IF_ICMPLE",
    "IF_ACMPEQ",
    "IF_ACMPNE",
    "GOTO",
    "JSR",
    "RET",
    "TABLESWITCH",
    "LOOKUPSWITCH",
    "IRETURN",
    "LRETURN",
    "FRETURN",
    "DRETURN",
    "ARETURN",
    "RETURN",
    "GETSTATIC",
    "PUTSTATIC",
    "GETFIELD",
    "PUTFIELD",
    "INVOKEVIRTUAL",
    "INVOKESPECIAL",
    "INVOKESTATIC",
    "INVOKEINTERFACE",
    "INVOKEDYNAMIC",
    "NEW",
    "NEWARRAY",
    "ANEWARRAY",
    "ARRAYLENGTH",
    "ATHROW",
    "CHECKCAST",
    "INSTANCEOF",
    "MONITORENTER",
    "MONITOREXIT",
    "PRIV_WIDE",
    "MULTIANEWARRAY",
    "IFNULL",
    "IFNONNULL",
    "PRIV_GOTO_W",
    "PRIV_JSR_W"
)

fun AbstractInsnNode.toTextRepresentation(): String =
    buildString {
        when (this@toTextRepresentation) {
            is InsnNode ->
                if (opcode in OPCODES_TO_STRING.indices)
                    append(OPCODES_TO_STRING[opcode])
                else {
                    append("UNK_")
                    append(opcode)
                }

            is IntInsnNode -> {
                if (opcode in OPCODES_TO_STRING.indices)
                    append(OPCODES_TO_STRING[opcode])
                else {
                    append("UNK_")
                    append(opcode)
                }

                append(' ')
                append(operand)
            }
            is VarInsnNode -> {
                if (opcode in OPCODES_TO_STRING.indices)
                    append(OPCODES_TO_STRING[opcode])
                else {
                    append("UNK_")
                    append(opcode)
                }

                append(' ')
                append(`var`)
            }
            is TypeInsnNode -> {
                if (opcode in OPCODES_TO_STRING.indices)
                    append(OPCODES_TO_STRING[opcode])
                else {
                    append("UNK_")
                    append(opcode)
                }

                append(' ')
                append(desc)

            }
            is FieldInsnNode -> {
                if (opcode in OPCODES_TO_STRING.indices)
                    append(OPCODES_TO_STRING[opcode])
                else {
                    append("UNK_")
                    append(opcode)
                }

                append(' ')
                append(this@toTextRepresentation.owner)
                append('.')
                append(this@toTextRepresentation.name)
                append(' ')
                append(this@toTextRepresentation.desc)
            }
            is MethodInsnNode -> {
                if (opcode in OPCODES_TO_STRING.indices)
                    append(OPCODES_TO_STRING[opcode])
                else {
                    append("UNK_")
                    append(opcode)
                }

                append(' ')
                append(this@toTextRepresentation.owner)
                append('.')
                append(this@toTextRepresentation.name)
                append(' ')
                append(this@toTextRepresentation.desc)

                if (this@toTextRepresentation.itf) append(" (itf)")
            }

            is InvokeDynamicInsnNode -> {
                append("INVOKEDYNAMIC ")
                append(this@toTextRepresentation.name)
                append(' ')
                append(this@toTextRepresentation.desc)
            }

            is JumpInsnNode -> {
                if (opcode in OPCODES_TO_STRING.indices)
                    append(OPCODES_TO_STRING[opcode])
                else {
                    append("UNK_")
                    append(opcode)
                }

                append(" -> ")
                append(this@toTextRepresentation.label?.label)
            }

            is LabelNode -> {
                append("LABEL: ")
                append(this@toTextRepresentation.label)
            }

            is LdcInsnNode -> {
                append("LDC ")
                append(this@toTextRepresentation.cst)
            }

            is FrameNode -> {
                append("FRAME [Local: ")
                append(this@toTextRepresentation.local?.joinToString())
                append("] [Stack: ")
                append(this@toTextRepresentation.stack?.joinToString())
                append(']')
            }

            is LineNumberNode -> {
                append("Line ")
                append(this@toTextRepresentation.line)
                append(" @ ")
                append(this@toTextRepresentation.start?.label)
            }

            else -> {
                if (opcode in OPCODES_TO_STRING.indices)
                    append(OPCODES_TO_STRING[opcode])
                else {
                    append("UNK_")
                    append(opcode)
                }

                append(' ')

                append(this@toTextRepresentation.type)
            }
        }
    }