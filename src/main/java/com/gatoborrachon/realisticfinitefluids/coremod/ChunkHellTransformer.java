package com.gatoborrachon.realisticfinitefluids.coremod;

import org.objectweb.asm.*;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.Launch;

public class ChunkHellTransformer implements IClassTransformer {


	public String getCorrectLAVAField() {
	    Boolean isDev = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	    return (isDev != null && isDev) ? "LAVA" : "field_185943_d";
	}
	
    private static final String TARGET_CLASS = "net.minecraft.world.gen.ChunkGeneratorHell";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!TARGET_CLASS.equals(transformedName)) {
            return basicClass;
        }

        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                // Remap dinámico del campo LAVA
                String mappedLavaName = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(
                        TARGET_CLASS, getCorrectLAVAField(), "Lnet/minecraft/block/state/IBlockState;" //field_185943_d --> LAVA
                );

                if (name.equals(mappedLavaName)) {
                    return super.visitField(access, name, desc, signature, null);
                }
                return super.visitField(access, name, desc, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

                if ("<clinit>".equals(name)) {
                    return new MethodVisitor(Opcodes.ASM5, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == Opcodes.RETURN) {
                                // Inyectar llamada a LavaBlockHelper
                                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                                        "com/gatoborrachon/realisticfinitefluids/coremod/utils/LavaBlockHelper",
                                        "getCustomLava",
                                        "()Lnet/minecraft/block/state/IBlockState;",
                                        false);

                                // Remap dinámico del campo LAVA
                                String owner = TARGET_CLASS;
                                String originalName = getCorrectLAVAField(); //field_185943_d --> LAVA
                                String desc = "Lnet/minecraft/block/state/IBlockState;";
                                String mappedName = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(owner, originalName, desc);

                                mv.visitFieldInsn(
                                        Opcodes.PUTSTATIC,
                                        owner.replace('.', '/'),
                                        mappedName,
                                        desc
                                );
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
                return mv;
            }
        }, 0);

        return cw.toByteArray();
    }
}

/*public class ChunkHellTransformer implements IClassTransformer {

    private static final String TARGET_CLASS = "net.minecraft.world.gen.ChunkGeneratorHell";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!TARGET_CLASS.equals(transformedName)) {
            return basicClass;
        }

        //System.out.println("[COREMOD] Transformando clase: " + transformedName);

        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {

            // Parche opcional: sobrescribir valor inicial de LAVA a null (aunque real asignación se hace en <clinit>)
            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                if ("field_150353_l".equals(name)) { // field_150353_l --> LAVA
                    //System.out.println("[COREMOD] Detectado campo LAVA, sobrescribiendo valor inicial a null");
                    return super.visitField(access, name, desc, signature, null);
                }
                return super.visitField(access, name, desc, signature, value);
            }
            
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

                // 1. Parche para <clinit>: asignar nuestro bloque custom al campo LAVA
                if ("<clinit>".equals(name)) {
                    return new MethodVisitor(Opcodes.ASM5, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == Opcodes.RETURN) {
                                //System.out.println("[COREMOD] Inyectando reemplazo de LAVA antes de RETURN en <clinit>");

                                // Llamar a Helper.getCustomLava()
                                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                                        "com/gatoborrachon/realisticfinitefluids/coremod/LavaBlockHelper",
                                        "getCustomLava",
                                        "()Lnet/minecraft/block/state/IBlockState;",
                                        false);
                                String owner = "net/minecraft/world/gen/ChunkGeneratorHell";
                                String originalName = "LAVA";
                                String desc = "Lnet/minecraft/block/state/IBlockState;";

                                // Obtener el nombre correcto según el entorno (MCP --> SRG --> obfuscated)
                                String mappedName = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(owner, originalName, desc);
                                
                                // Asignar al campo LAVA
                                mv.visitFieldInsn(
                                	    Opcodes.PUTSTATIC,
                                	    owner.replace('.', '/'), // ASM usa el formato con /
                                	    mappedName,
                                	    desc
                                );
                                /*mv.visitFieldInsn(Opcodes.PUTSTATIC,
                                        "net/minecraft/world/gen/ChunkGeneratorHell",
                                        "field_150353_l", //field_150353_l --> LAVA
                                        "Lnet/minecraft/block/state/IBlockState;");*/
                            /*}
                            super.visitInsn(opcode);
                        }
                    };
                }
                return mv;
            }
        }, 0);

        return cw.toByteArray();
    }
}*/

