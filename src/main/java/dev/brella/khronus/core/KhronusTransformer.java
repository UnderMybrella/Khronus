package dev.brella.khronus.core;

import com.google.common.collect.Maps;
import dev.brella.khronus.TickDog;
import dev.brella.khronus.core.patches.*;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class KhronusTransformer implements IClassTransformer {
    public static final String WORLD_CLASS = "net.minecraft.world.World";

    public static final Map<String, Supplier<Consumer<ClassNode>>> PATCHES = new HashMap<>();

    static {
        PATCHES.put(WORLD_CLASS, () -> WorldTransformer.INSTANCE);
        PATCHES.put("dev.brella.khronus.api.KhronusApi", () -> KhronusApiTranformer.INSTANCE);

//        METHOD_PATCHES.put(WORLD_CLASS, worldPatches);
//        METHOD_PATCHES.put("dev.brella.khronus.api.KhronusApi", apiPatches);
    }

    public void updateEntities(World world) {
        TickDog.getWatchdog().updateEntities(world);
    }

    /**
     * L3
     * LINENUMBER 1920 L3
     * ILOAD 2
     * IFEQ L4
     * ALOAD 1
     * INSTANCEOF net/minecraft/util/ITickable
     * IFEQ L4
     * L5
     * LINENUMBER 1922 L5
     * ALOAD 0
     * GETFIELD net/minecraft/world/World.tickableTileEntities : Ljava/util/List;
     * ALOAD 1
     * INVOKEINTERFACE java/util/List.add (Ljava/lang/Object;)Z (itf)
     * POP
     * L4
     */
    public void addTileEntity(World world, TileEntity te) {
        TickDog.getWatchdog().addTileEntity(world, te);
    }

    public void removeTileEntity(World world, BlockPos pos, TileEntity te) {
        TickDog.getWatchdog().removeTileEntity(world, pos, te);
    }

    public static AbstractInsnNode getWatchdog() {
        //new FieldInsnNode(Opcodes.GETSTATIC, "dev/brella/khronus/TickDog", "watchdog", "Ldev/brella/khronus/watchdogs/KhronusWatchdog;")
        return new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/brella/khronus/TickDog", "getWatchdog", "()Ldev/brella/khronus/watchdogs/KhronusWatchdog;", false);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.startsWith("java") || name.startsWith("kotlin") || transformedName.startsWith("java") || transformedName.startsWith("kotlin")) return basicClass;

//        if (transformedName.equals(WORLD_CLASS)) {
//            System.out.println("Transforming " + transformedName + "#" + "updateEntities|func_72939_s");
//
//            return transform(basicClass, transformedName, classNode -> {
//                classNode.visitField(ACC_PUBLIC, "khronusTickableTileEntities", "Ljava/util/Map;", "Ljava/util/Map<Lnet/minecraft/tileentity/TileEntity;Ldev/brella/khronus/TemporalBounds;>;", null).visitEnd();
//
//                Iterator<MethodNode> methods = classNode.methods.iterator();
//                MethodNode method;
//
//                while (methods.hasNext()) {
//                    method = methods.next();
//                    AbstractInsnNode instruction;
//
//                    if (method.name.equals(WORLD_UPDATE_ENTITIES) || method.name.equals(WORLD_UPDATE_ENTITIES_SRG)) {
////                        System.out.println("Attempting to patch " + method.name + "; " + method.instructions.size() + " instructions");
////
////                        int blockStart = -1;
////                        int blockEnd = -1;
////
////                        Map<LabelNode, Integer> labels = new HashMap<>();
////
////                        for (int i = 0; i < method.instructions.size(); i++) {
////                            instruction = method.instructions.get(i);
////
////                            if (instruction instanceof LabelNode) {
////                                labels.put((LabelNode) instruction, i);
////                            }
////
////
////                            if (blockStart == -1) {
////                                if (instruction instanceof FrameNode) {
////                                    instruction = method.instructions.get(i + 1);
////
////                                    if (instruction.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) instruction).var == 0) {
////                                        //Sweet, let's check the next element
////
////                                        instruction = method.instructions.get(i + 2);
////
////                                        if (instruction.getOpcode() == Opcodes.GETFIELD && ((FieldInsnNode) instruction).owner.equals("net/minecraft/world/World") && (((FieldInsnNode) instruction).name.equals("tickableTileEntities") || ((FieldInsnNode) instruction).name.equals("field_175730_i"))) {
////                                            //Now let's check if we're getting the iterator
////
////                                            instruction = method.instructions.get(i + 3);
////
////                                            if (instruction.getOpcode() == Opcodes.INVOKEINTERFACE && ((MethodInsnNode) instruction).owner.equals("java/util/List") && ((MethodInsnNode) instruction).name.equals("iterator")) {
////                                                //Got it, this is our place
////
////                                                blockStart = i + 1;
////
////                                                i += 3;
////
////                                                continue;
////                                            }
////                                        }
////
////                                        instruction = method.instructions.get(i);
////                                    }
////                                }
////                            } else if (blockEnd == -1) {
////                                if (instruction instanceof FrameNode) {
////                                    instruction = method.instructions.get(i + 1);
////
////                                    if (instruction.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) instruction).var == 0) {
////                                        //Check time
////
////                                        instruction = method.instructions.get(i + 2);
////
////                                        if (instruction.getOpcode() == Opcodes.ICONST_0) {
////                                            //Are we updating processingLoadedTiles?
////
////                                            instruction = method.instructions.get(i + 3);
////
////                                            if (instruction.getOpcode() == Opcodes.PUTFIELD && ((FieldInsnNode) instruction).owner.equals("net/minecraft/world/World") && (((FieldInsnNode) instruction).name.equals("processingLoadedTiles") || ((FieldInsnNode) instruction).name.equals("field_147481_N"))) {
////                                                //This is us!
////
////                                                blockEnd = i;
////                                            }
////                                        }
////
////                                        instruction = method.instructions.get(i);
////                                    }
////                                }
////                            }
////                        }
////
////                        if (blockStart != -1 && blockEnd != -1) {
////                            System.out.println("Got our block: " + blockStart + ".." + blockEnd);
////
////                            AbstractInsnNode[] nodes = new AbstractInsnNode[blockEnd - blockStart];
////                            for (int i = 0; i < nodes.length; i++)
////                                nodes[i] = method.instructions.get(i + blockStart);
////
////                            System.out.println("Brace yourselves...");
////
////                            LocalVariableNode localVariable;
////                            Integer startNode;
////                            Integer endNode;
////
////                            for (int i = 0; i < method.localVariables.size(); i++) {
////                                localVariable = method.localVariables.get(i);
////                                startNode = labels.get(localVariable.start);
////                                endNode = labels.get(localVariable.end);
////
////                                if (startNode > blockStart && endNode < blockEnd) {
////                                    System.out.println("Removing " + localVariable.name + "; it's only present in our block");
////                                    method.localVariables.remove(localVariable);
////                                } else if (startNode > blockStart && startNode < blockEnd && endNode >= blockEnd) {
////                                    if (localVariable.end.getNext() == null) {
////                                        System.out.println("Removing " + localVariable.name + "; Its a functional scope but it's only used in our block");
////                                        method.localVariables.remove(localVariable);
////                                    } else {
////                                        System.out.println("Uho, not sure how to handle " + localVariable.name + " starting in our block but ending outside it!");
////                                    }
////                                } else if (startNode <= blockStart && endNode > blockStart && endNode < blockEnd) {
////                                    System.out.println("Wah, not sure how we're gonna do " + localVariable.name + " since it starts before our block but ends inside it!");
////                                }
////                            }
////
////                            TryCatchBlockNode tryCatch;
////
////                            for (int i = 0; i < method.tryCatchBlocks.size(); i++) {
////                                tryCatch = method.tryCatchBlocks.get(i);
////
////                                startNode = labels.get(tryCatch.start);
////                                endNode = labels.get(tryCatch.end);
////
////                                if (startNode > blockStart && endNode < blockEnd) {
////                                    System.out.println("Removing " + tryCatch.type + "; it's only present in our block");
////                                    method.tryCatchBlocks.remove(tryCatch);
////                                } else if (startNode > blockStart && startNode < blockEnd && endNode >= blockEnd) {
////                                    System.out.println("Uho, not sure how to handle " + tryCatch.type + " starting in our block but ending outside it!");
////                                } else if (startNode <= blockStart && endNode > blockStart && endNode < blockEnd) {
////                                    System.out.println("Wah, not sure how we're gonna do " + tryCatch.type + " since it starts before our block but ends inside it!");
////                                }
////                            }
////
////                            for (AbstractInsnNode node : nodes)
////                                method.instructions.remove(node);
////
////                            InsnList newInstructions = new InsnList();
////
////                            newInstructions.add(getWatchdog());
////                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
////
////                            newInstructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "dev/brella/khronus/watchdogs/KhronusWatchdog", "updateEntities", "(Lnet/minecraft/world/World;)V", true));
////
////                            method.instructions.insertBefore(method.instructions.get(blockStart), newInstructions);
////                        }
//                    } else if (method.name.equals(WORLD_ADD_TILE_ENTITY) || method.name.equals(WORLD_ADD_TILE_ENTITY_SRG)) {
//                        /*
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
//                        //We need to modify two separate parts of this function
//                        //The first is patching the equals check, we just need to remove ALOAD 1 to IFEQ L4
//
////                        int checkStart = -1;
////                        int blockStart = -1;
////
////                        System.out.println("Attempting to patch " + method.name + "; " + method.instructions.size() + " instructions");
////
////                        for (int i = 0; i < method.instructions.size(); i++) {
////                            instruction = method.instructions.get(i);
////
////                            /*
////                             *     ALOAD 1
////                             *     INSTANCEOF net/minecraft/util/ITickable
////                             *     IFEQ L4
////                             */
////                            if (checkStart == -1) {
////                                /* ALOAD 1 - tile entity */
////                                if (instruction.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) instruction).var == 1) {
////                                    instruction = method.instructions.get(i + 1);
////
////                                    /* INSTANCEOF ITickable */
////                                    if (instruction.getOpcode() == Opcodes.INSTANCEOF && ((TypeInsnNode) instruction).desc.equals("net/minecraft/util/ITickable")) {
////                                        instruction = method.instructions.get(i + 2);
////
////                                        /* IFEQ L4 */
////                                        if (instruction.getOpcode() == Opcodes.IFEQ) {
////                                            checkStart = i;
////
////                                            i += 3;
////                                        }
////                                    }
////                                }
////                            }
////
////                            /*
////                             *     ALOAD 0
////                             *     GETFIELD net/minecraft/world/World.tickableTileEntities : Ljava/util/List;
////                             *     ALOAD 1
////                             *     INVOKEINTERFACE java/util/List.add (Ljava/lang/Object;)Z (itf)
////                             *     POP
////                             */
////                            // This is ***probably*** a safe assumption to make?
////
////                            if (blockStart == -1) {
////                                /* ILOAD 2 - flag */
////                                if (instruction.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) instruction).var == 0) {
////                                    instruction = method.instructions.get(i + 1);
////
////                                    if (instruction.getOpcode() == Opcodes.GETFIELD && ((FieldInsnNode) instruction).owner.equals("net/minecraft/world/World") && (((FieldInsnNode) instruction).name.equals("tickableTileEntities") || ((FieldInsnNode) instruction).name.equals("field_175730_i"))) {
////                                        instruction = method.instructions.get(i + 2);
////
////                                        if (instruction.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) instruction).var == 1) {
////                                            instruction = method.instructions.get(i + 3);
////
////                                            if (instruction.getOpcode() == Opcodes.INVOKEINTERFACE && ((MethodInsnNode) instruction).owner.equals("java/util/List") && ((MethodInsnNode) instruction).name.equals("add")) {
////                                                instruction = method.instructions.get(i + 4);
////
////                                                if (instruction.getOpcode() == Opcodes.POP) {
////                                                    //Got it, this is our place
////                                                    blockStart = i;
////
////                                                    i += 5;
////
////                                                    continue;
////                                                }
////                                            }
////                                        }
////                                    }
////
////                                    instruction = method.instructions.get(i);
////                                }
////                            }
////                        }
////
////                        if (checkStart != -1 && blockStart == -1) {
////                            System.err.println("Error: Check Start == " + checkStart + " but Block Start == -1; recheck our assumptions?");
////                        } else if (checkStart != -1) {
////                            System.out.println("Got our blocks: " + checkStart + "," + blockStart + "..");
////                            System.out.println("Brace yourselves...");
////
//////                            for (AbstractInsnNode node : nodes)
//////                                method.instructions.remove(node);
////
////                            AbstractInsnNode[] check = new AbstractInsnNode[3];
////                            AbstractInsnNode[] block = new AbstractInsnNode[5];
////
////                            for (int i = 0; i < check.length; i++) check[i] = method.instructions.get(checkStart + i);
////                            for (int i = 0; i < block.length; i++) block[i] = method.instructions.get(blockStart + i);
////
////                            AbstractInsnNode suffix = block[block.length - 1].getNext();
////
////                            for (AbstractInsnNode node : check)
////                                method.instructions.remove(node);
////
////                            for (AbstractInsnNode node : block)
////                                method.instructions.remove(node);
////
////                            System.out.println("Removed instructions");
////
////                            InsnList newInstructions = new InsnList();
////                            /*
////                             GETSTATIC dev/brella/khronus/TickDog.watchdog : Ldev/brella/khronus/watchdogs/KhronusWatchdog;
////                             ALOAD 1
////                             INVOKEINTERFACE dev/brella/khronus/watchdogs/KhronusWatchdog.updateEntities (Lnet/minecraft/world/World;)V (itf)
////                             */
////                            newInstructions.add(getWatchdog());
////                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //World
////                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); //Tile Entity
////                            newInstructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "dev/brella/khronus/watchdogs/KhronusWatchdog", "addTileEntity", "(Lnet/minecraft/world/World;Lnet/minecraft/tileentity/TileEntity;)V", true));
////
////                            method.instructions.insertBefore(suffix, newInstructions);
////                        }
//                    } else if (method.name.equals(WORLD_REMOVE_TILE_ENTITY) || method.name.equals(WORLD_REMOVE_TILE_ENTITY_SRG)) {
//                        /*
//                         *     ALOAD 0
//                         *     GETFIELD net/minecraft/world/World.tickableTileEntities : Ljava/util/List;
//                         *     ALOAD 2
//                         *     INVOKEINTERFACE java/util/List.remove (Ljava/lang/Object;)Z (itf)
//                         *     POP
//                         */
//
//                        int blockStart = -1;
//
//                        System.out.println("Attempting to patch " + method.name + "; " + method.instructions.size() + " instructions");
//
//                        for (int i = 0; i < method.instructions.size(); i++) {
//                            instruction = method.instructions.get(i);
//
//                            /*
//                             *     ALOAD 0
//                             *     GETFIELD net/minecraft/world/World.tickableTileEntities : Ljava/util/List;
//                             *     ALOAD 2
//                             *     INVOKEINTERFACE java/util/List.remove (Ljava/lang/Object;)Z (itf)
//                             *     POP
//                             */
//
//                            if (blockStart == -1) {
//                                /* ALOAD 0 */
//                                if (instruction.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) instruction).var == 0) {
//                                    instruction = method.instructions.get(i + 1);
//
//                                    if (instruction.getOpcode() == Opcodes.GETFIELD && ((FieldInsnNode) instruction).owner.equals("net/minecraft/world/World") && (((FieldInsnNode) instruction).name.equals("tickableTileEntities") || ((FieldInsnNode) instruction).name.equals("field_175730_i"))) {
//                                        instruction = method.instructions.get(i + 2);
//
//                                        if (instruction.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) instruction).var == 2) {
//                                            instruction = method.instructions.get(i + 3);
//
//                                            if (instruction.getOpcode() == Opcodes.INVOKEINTERFACE && ((MethodInsnNode) instruction).owner.equals("java/util/List") && ((MethodInsnNode) instruction).name.equals("remove")) {
//                                                instruction = method.instructions.get(i + 4);
//
//                                                if (instruction.getOpcode() == Opcodes.POP) {
//                                                    //Got it, this is our place
//                                                    blockStart = i;
//
//                                                    i += 5;
//
//                                                    continue;
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    instruction = method.instructions.get(i);
//                                }
//                            }
//                        }
//
//                        if (blockStart != -1) {
//                            System.out.println("Got our block: " + blockStart);
//                            System.out.println("Brace yourselves...");
//
////                            for (AbstractInsnNode node : nodes)
////                                method.instructions.remove(node);
//
//                            AbstractInsnNode[] block = new AbstractInsnNode[5];
//
//                            for (int i = 0; i < block.length; i++) block[i] = method.instructions.get(blockStart + i);
//
//                            AbstractInsnNode suffix = block[block.length - 1].getNext();
//
//                            for (AbstractInsnNode node : block)
//                                method.instructions.remove(node);
//
//                            System.out.println("Removed instructions");
//
//                            InsnList newInstructions = new InsnList();
//                            /*
//                             GETSTATIC dev/brella/khronus/TickDog.watchdog : Ldev/brella/khronus/watchdogs/KhronusWatchdog;
//                             ALOAD 1
//                             INVOKEINTERFACE dev/brella/khronus/watchdogs/KhronusWatchdog.updateEntities (Lnet/minecraft/world/World;)V (itf)
//                             */
//                            newInstructions.add(getWatchdog());
//                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //World
//                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); //Pos
//                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2)); //Tile Entity
//                            newInstructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "dev/brella/khronus/watchdogs/KhronusWatchdog", "removeTileEntity", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", true));
//
//                            method.instructions.insertBefore(suffix, newInstructions);
//                        }
//                    } else if (method.name.equals("<init>")) {
//                        /*
//                            ALOAD 0
//                            INVOKESTATIC com/google/common/collect/Maps.newConcurrentMap ()Ljava/util/concurrent/ConcurrentMap;
//                            PUTFIELD net/minecraft/world/World.loadedEntityList : Ljava/util/List;
//                         */
//                        InsnList newInstructions = new InsnList();
//                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //World
//                        newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/google/common/collect/Maps", "newConcurrentMap", "()Ljava/util/concurrent/ConcurrentMap;", false));
//                        newInstructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/World", "khronusTickableTileEntities", "Ljava/util/Map;"));
//
//                        method.instructions.insertBefore(method.instructions.getLast().getPrevious(), newInstructions);
//                    }
//                }
//            });
//        } else if (transformedName.equals("dev.brella.khronus.TickDog")) {
//            System.out.println("Transforming " + transformedName + "#" + "khronusTickableTileEntities");
//
//            return transform(basicClass, transformedName, classNode -> {
//                Iterator<MethodNode> methods = classNode.methods.iterator();
//                MethodNode method;
//
//                while (methods.hasNext()) {
//                    method = methods.next();
//                    AbstractInsnNode instruction;
//
//                    if (method.name.equals("khronusTickableTileEntities")) {
//                        System.out.println("Attempting to patch " + method.name + "; " + method.instructions.size() + " instructions");
//                        int blockStart = -1;
//
//                        for (int i = 0; i < method.instructions.size(); i++) {
//                            instruction = method.instructions.get(i);
//
//                            if (instruction.getOpcode() == Opcodes.NEW && ((TypeInsnNode) instruction).desc.equals("java/util/HashMap")) {
//                                if (method.instructions.get(i + 4).getOpcode() == Opcodes.ARETURN) {
//                                    blockStart = i;
//                                    break;
//                                }
//                            }
//                        }
//
//                        if (blockStart != -1) {
//                            System.out.println("Got our block: " + blockStart);
//                            System.out.println("Brace yourselves...");
//
//                            AbstractInsnNode[] block = new AbstractInsnNode[4];
//
//                            for (int i = 0; i < block.length; i++) block[i] = method.instructions.get(blockStart + i);
//
//                            AbstractInsnNode suffix = block[block.length - 1].getNext();
//
//                            for (AbstractInsnNode node : block)
//                                method.instructions.remove(node);
//
//                            System.out.println("Removed instructions");
//
//                            InsnList newInstructions = new InsnList();
//                            /*
//                                ALOAD 1
//                                GETFIELD net/minecraft/world/World.tickableTileEntities : Ljava/util/List;
//                                DUP
//                                LDC "world.tickableTileEntities"
//                             */
//                            newInstructions.add(getWatchdog());
//                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); //World
//                            newInstructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "khronusTickableTileEntities", "Ljava/util/Map;"));
////                            newInstructions.add(new InsnNode(Opcodes.DUP));
//                            method.instructions.insertBefore(suffix, newInstructions);
//                        }
//
//                        break;
//                    }
//                }
//            });
//        }

        Supplier<Consumer<ClassNode>> supplier = PATCHES.get(transformedName);

        if (supplier != null) {
            Consumer<ClassNode> transformer = supplier.get();
            if (transformer != null) {
                System.out.println("Transforming " + transformedName + " with " + transformer);

                return transform(basicClass, transformedName, transformer);
            }
        }

        return basicClass;
    }

    public byte[] transform(byte[] basicClass, String className, Consumer<ClassNode> transformer) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);

        transformer.accept(classNode);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        byte[] data = cw.toByteArray();

        try (FileOutputStream out = new FileOutputStream(className + ".class")) {
            out.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }
}
