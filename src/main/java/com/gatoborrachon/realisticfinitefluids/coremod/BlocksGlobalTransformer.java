package com.gatoborrachon.realisticfinitefluids.coremod;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import com.gatoborrachon.realisticfinitefluids.init.EarlyConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BlocksGlobalTransformer implements IClassTransformer {

    private static final String BLOCKS_OWNER = "net/minecraft/init/Blocks";
    private static final String MODBLOCKS_OWNER = "com/gatoborrachon/realisticfinitefluids/init/ModBlocks";

    private static final Set<String> BLOCKS_TO_REPLACE = new HashSet<>(Arrays.asList(
        "field_150358_i", // FLOWING_WATER
        "field_150355_j", // WATER
        "field_150356_k", // FLOWING_LAVA
        "field_150353_l"  // LAVA
    ));

    private static final String DESC_BLOCK = "Lnet/minecraft/block/Block;";
    
    List<String> exclusions = EarlyConfig.readExclusions();
    boolean debugUniversalCompatLog = EarlyConfig.readUniversalCompatDebug();
    private static final Logger LOGGER = LogManager.getLogger("RealisticFiniteFluids");

    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;


        //LOGGER.info("EXCLUSIONES USADAS AL FINAL: {}", exclusions);
        //LOGGER.info("DEBUG USADO AL FINAL: {}", debugUniversalCompatLog);

        
        for (String prefix : exclusions) {
            if (transformedName.startsWith(prefix)) return basicClass;
        }

        if (debugUniversalCompatLog) System.out.println("[RFF] Revisando clase: " + transformedName);

        try {
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.EXPAND_FRAMES);

            boolean modified = false;

            for (MethodNode mn : cn.methods) {
                if (debugUniversalCompatLog) System.out.println("  [RFF] Analizando metodo: " + mn.name);

                Iterator<AbstractInsnNode> it = mn.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode insn = it.next();

                    // === REEMPLAZO DE CAMPOS BLOCKS ===
                    if (insn instanceof FieldInsnNode) {
                        FieldInsnNode fin = (FieldInsnNode) insn;

                        if (BLOCKS_OWNER.equals(fin.owner) && BLOCKS_TO_REPLACE.contains(fin.name)) {
                            if (debugUniversalCompatLog) System.out.println("    [RFF] Detectando: " + fin.owner + "." + fin.name);

                            // Cambiar owner y nombre
                            fin.owner = MODBLOCKS_OWNER;
                            switch (fin.name) {
                                case "field_150358_i": fin.name = "FINITE_WATER_FLOWING"; break;
                                case "field_150355_j": fin.name = "FINITE_WATER_STILL"; break;
                                case "field_150356_k": fin.name = "FINITE_LAVA_FLOWING"; break;
                                case "field_150353_l": fin.name = "FINITE_LAVA_STILL"; break;
                            }

                            // Descriptor a Block
                            fin.desc = DESC_BLOCK;
                            modified = true;

                            if (debugUniversalCompatLog) System.out.println("    [RFF] Reemplazado por: " + fin.owner + "." + fin.name);
                        }
                    }

                    // === REEMPLAZO DE INVOKEVIRTUAL getDefaultState ===
                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode min = (MethodInsnNode) insn;
                        //System.out.println("    [DEBUG_VERGA] -- OWNER: " + min.owner+"NAME: "+min.name+"DESC: "+min.desc);

                        // Si es BlockDynamicLiquid.getDefaultState() -> cambiar owner a Block
                        if (min.getOpcode() == Opcodes.INVOKEVIRTUAL
                                && (min.owner.equals("net/minecraft/block/BlockDynamicLiquid") || min.owner.equals("net/minecraft/block/BlockStaticLiquid"))
                                && min.name.equals("func_176223_P") //func_176223_P --> getDefaultState
                                && min.desc.equals("()Lnet/minecraft/block/state/IBlockState;")) {


                            if (debugUniversalCompatLog)
                                System.out.println("    [RFF] Forzando getDefaultState() de BlockDynamicLiquid -> Block en " + mn.name);

                            min.owner = "net/minecraft/block/Block"; // ahora invoca Block.getDefaultState
                            modified = true;
                        }
                    }
                }
            }

            if (modified) {
                if (debugUniversalCompatLog) System.out.println("[RFF] Clase modificada: " + transformedName);
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                cn.accept(cw);
                return cw.toByteArray();
            } else {
                return basicClass;
            }

        } catch (Throwable t) {
            t.printStackTrace();
            return basicClass;
        }
    }
}
