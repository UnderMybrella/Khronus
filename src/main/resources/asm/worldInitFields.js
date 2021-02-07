function initializeCoreMod() {
    var fields = ["khronusTickableTileEntities", "tickAcceleration", "tickLength", "tickCheckups"];

	return {
	    'worldAddFields': {
	        'target': {
	            'type': 'CLASS',
	            'name': 'net.minecraft.world.World'
	        },
	        'transformer': function(classNode) {
    	        var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
	            ASMAPI.log('INFO', '{Khronus} Adding \'World\' ASM fields...');

	            var Opcodes = Java.type('org.objectweb.asm.Opcodes');

                for (var i = 0; i < fields.length; i++) {
	                classNode.visitField(Opcodes.ACC_PUBLIC, fields[i], "Ljava/util/Map;", "Ljava/util/Map<Lnet/minecraft/tileentity/TileEntity;Ldev/brella/khronus/api/TemporalBounds;>;", null).visitEnd();
	            }

	            ASMAPI.log('INFO', '{Khronus} Added \'World\' ASM fields...');

	            return classNode;
	        }
	    },
		'worldInitFields': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.world.World',
				'methodName': '<init>',
				'methodDesc': '(Lnet/minecraft/world/storage/ISpawnWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Ljava/util/function/Supplier;ZZJ)V'
			},
			'transformer': function(method) {
				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                ASMAPI.log('INFO', '{Khronus} Adding \'World#init\' ASM patch...');

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

                for (var i = 0; i < fields.length; i++) {
                    newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //World

                    newInstructions.add(new TypeInsnNode(Opcodes.NEW, "java/util/WeakHashMap"));
                    newInstructions.add(new InsnNode(Opcodes.DUP));
                    newInstructions.add(ASMAPI.buildMethodCall("java/util/WeakHashMap", "<init>", "()V", ASMAPI.MethodType.SPECIAL));

                    newInstructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/World", fields[i], "Ljava/util/Map;"));
                }

                method.instructions.insertBefore(worldReturn, newInstructions);

                ASMAPI.log('INFO', '{Khronus} Added \'World#init\' ASM patch!');
                return method;
			}
		}
	}
}
