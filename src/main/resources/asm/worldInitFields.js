function initializeCoreMod() {
	return {
		'worldInitFields': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.world.World',
				'methodName': '<init>',
				'methodDesc': '(Lnet/minecraft/world/storage/ISpawnWorldInfo;Lnet/minecraft/util/RegistryKey<Lnet/minecraft/world/World;>;Lnet/minecraft/util/RegistryKey<Lnet/minecraft/world/DimensionType;>;Lnet/minecraft/world/DimensionType;Ljava/util/function/Supplier<Lnet/minecraft/profiler/IProfiler;>;ZZJ)V'
			},
			'transformer': function(method) {
				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                ASMAPI.log('INFO', 'Adding \'World#init\' ASM patch...');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

                var worldReturn = method.instructions.getLast().getPrevious();

                /**
                new("java/util/WeakHashMap")
                        dup()
                        invokeSpecial("java/util/WeakHashMap", "<init>", "()V")
                        */

                var newInstructions = new InsnList();

                var fields = ["khronusTickableTileEntities", "tickAcceleration", "tickLength", "tickCheckups"];

                for (var i = 0; i < fields.length; i++) {
                    newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //World

                    newInstructions.add(new TypeInsnNode(Opcodes.NEW, "java/util/WeakHashMap"));
                    newInstructions.add(new InsnNode(Opcodes.DUP));
                    newInstructions.add(ASMAPI.buildMethodCall("java/util/WeakHashMap", "<init>", "()V", ASMAPI.MethodType.SPECIAL));

                    newInstructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/World", fields[i], "Ljava/util/Map;"));
                }

                method.instructions.insertBefore(worldReturn, newInstructions);

                ASMAPI.log('INFO', 'Added \'World#init\' ASM patch!');
                return method;
			}
		}
	}
}
