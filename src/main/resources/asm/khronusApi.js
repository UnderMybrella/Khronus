function initializeCoreMod() {
    var fields = [, , , ];


	return {
        'getKhronusTileEntities': {
            'target': {
                'type': 'METHOD',
                'class': 'dev.brella.khronus.api.KhronusApi',
                'methodName': 'getKhronusTileEntities',
                'methodDesc': '(Lnet/minecraft/world/World;)Ljava/util/Map;'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                ASMAPI.log('INFO', '{Khronus} Returning \'KhronusApi#getKhronusTileEntities\' via ASM...');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

                method.instructions.clear();
                method.localVariables.clear();
                method.maxLocals = 0;

                method.localVariables.clear();

                //aload(0) //World
                  //        add(FieldInsnNode(Opcodes.GETFIELD,
                  //            "net/minecraft/world/World",
                  //            fieldName,
                  //            "Ljava/util/Map;"))

                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //World
                method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "khronusTickableTileEntities", "Ljava/util/Map;"));
                method.instructions.add(new InsnNode(Opcodes.ARETURN));

                ASMAPI.log('INFO', '{Khronus} Added \'KhronusApi#getKhronusTileEntities\' ASM patch!');
                return method;
            }
        },
        'getTickAcceleration': {
            'target': {
                'type': 'METHOD',
                'class': 'dev.brella.khronus.api.KhronusApi',
                'methodName': 'getTickAcceleration',
                'methodDesc': '(Lnet/minecraft/world/World;)Ljava/util/Map;'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                ASMAPI.log('INFO', '{Khronus} Returning \'KhronusApi#getTickAcceleration\' via ASM...');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

                method.instructions.clear();
                method.localVariables.clear();
                method.maxLocals = 0;

                method.localVariables.clear();

                //aload(0) //World
                  //        add(FieldInsnNode(Opcodes.GETFIELD,
                  //            "net/minecraft/world/World",
                  //            fieldName,
                  //            "Ljava/util/Map;"))

                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //World
                method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "tickAcceleration", "Ljava/util/Map;"));
                method.instructions.add(new InsnNode(Opcodes.ARETURN));

                ASMAPI.log('INFO', '{Khronus} Added \'KhronusApi#getTickAcceleration\' ASM patch!');
                return method;
            }
        },
        'getTickLength': {
            'target': {
                'type': 'METHOD',
                'class': 'dev.brella.khronus.api.KhronusApi',
                'methodName': 'getTickLength',
                'methodDesc': '(Lnet/minecraft/world/World;)Ljava/util/Map;'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                ASMAPI.log('INFO', '{Khronus} Returning \'KhronusApi#getTickLength\' via ASM...');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

                method.instructions.clear();
                method.localVariables.clear();
                method.maxLocals = 0;

                method.localVariables.clear();

                //aload(0) //World
                  //        add(FieldInsnNode(Opcodes.GETFIELD,
                  //            "net/minecraft/world/World",
                  //            fieldName,
                  //            "Ljava/util/Map;"))

                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //World
                method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "tickLength", "Ljava/util/Map;"));
                method.instructions.add(new InsnNode(Opcodes.ARETURN));

                ASMAPI.log('INFO', '{Khronus} Added \'KhronusApi#getTickLength\' ASM patch!');
                return method;
            }
        },
        'getTickCheckup': {
            'target': {
                'type': 'METHOD',
                'class': 'dev.brella.khronus.api.KhronusApi',
                'methodName': 'getTickCheckup',
                'methodDesc': '(Lnet/minecraft/world/World;)Ljava/util/Map;'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                ASMAPI.log('INFO', '{Khronus} Returning \'KhronusApi#getTickCheckup\' via ASM...');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

                method.instructions.clear();
                method.localVariables.clear();
                method.maxLocals = 0;

                method.localVariables.clear();

                //aload(0) //World
                  //        add(FieldInsnNode(Opcodes.GETFIELD,
                  //            "net/minecraft/world/World",
                  //            fieldName,
                  //            "Ljava/util/Map;"))

                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //World
                method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "tickCheckups", "Ljava/util/Map;"));
                method.instructions.add(new InsnNode(Opcodes.ARETURN));

                ASMAPI.log('INFO', '{Khronus} Added \'KhronusApi#getTickCheckup\' ASM patch!');
                return method;
            }
        },
        'isApiInitialised': {
            'target': {
                'type': 'METHOD',
                'class': 'dev.brella.khronus.api.KhronusApi',
                'methodName': 'isApiInitialised',
                'methodDesc': '()Z'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                ASMAPI.log('INFO', '{Khronus} Returning \'KhronusApi#isApiInitialised\' via ASM...');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

                method.instructions.clear();
                method.localVariables.clear();
                method.maxLocals = 0;

                method.localVariables.clear();

                method.instructions.add(new InsnNode(Opcodes.ICONST_1));
                method.instructions.add(new InsnNode(Opcodes.IRETURN));

                ASMAPI.log('INFO', '{Khronus} Added \'KhronusApi#getKhronusTileEntities\' ASM patch!');
                return method;
            }
        }
	}
}
