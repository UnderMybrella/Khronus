function initializeCoreMod() {
	return {
		'worldUpdateTick': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.world.World',
				'methodName': 'func_217391_K',
				'methodDesc': '()V'
			},
			'transformer': function(method) {
				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                ASMAPI.log('INFO', 'Adding \'tickBlockEntities\' ASM patch...');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

                method.instructions.clear();
                method.tryCatchBlocks.clear();

                while (method.maxLocals > 1) {
                    method.localVariables.remove(1);
                    method.maxLocals--;
                }

                method.localVariables.clear();
                method.instructions.add(ASMAPI.buildMethodCall('dev/brella/khronus/TickDog',
                                                               'getWatchdog',
                                                               '()Ldev/brella/khronus/watchdogs/KhronusWatchdog;',
                                                               ASMAPI.MethodType.STATIC));
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //World
                method.instructions.add(ASMAPI.buildMethodCall('dev/brella/khronus/watchdogs/KhronusWatchdog',
                                                               'tickBlockEntities',
                                                               '(Lnet/minecraft/world/World;)V',
                                                               ASMAPI.MethodType.INTERFACE));

                method.instructions.add(new InsnNode(Opcodes.RETURN));

                ASMAPI.log('INFO', 'Added \'tickBlockEntities\' ASM patch!');
                return method;
			}
		}
	}
}
