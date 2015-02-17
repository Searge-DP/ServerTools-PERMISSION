/*
 * Copyright 2014 ServerTools
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.servertools.permission.asm;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class STPClassTransformer implements IClassTransformer {

    private static final Logger log = LogManager.getLogger();

    private static final FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
    private static final Set<PatchNote> patches = new HashSet<>();

    static {
        PatchNote chPatch = new PatchNote("net.minecraft.command.CommandHandler", "info.servertools.permission.STPCommandHandler");
        chPatch.addMethodToPatch(new MethodNote("executeCommand", "func_71556_a", "(Lnet/minecraft/command/ICommandSender;Ljava/lang/String;)I"));
        chPatch.addMethodToPatch(new MethodNote("getPossibleCommands", "func_71558_b", "(Lnet/minecraft/command/ICommandSender;Ljava/lang/String;)Ljava/util/List;"));
        chPatch.addMethodToPatch(new MethodNote("getPossibleCommands", "func_71557_a", "(Lnet/minecraft/command/ICommandSender;)Ljava/util/List;"));
        addPatch(chPatch);

        PatchNote playerPatch = new PatchNote("net.minecraft.entity.player.EntityPlayerMP", "info.servertools.permission.STPEntityPlayer");
        playerPatch.addMethodToPatch(new MethodNote("canCommandSenderUseCommand", "func_70003_b", "(ILjava/lang/String;)Z"));
        addPatch(playerPatch);
    }


    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {

        if (bytes == null)
            return null;

        log.trace("Class: {} | Transformed: {}", name, transformedName);

        for (PatchNote patchNote : patches) {
            if (patchNote.sourceClass.equals(transformedName)) {
                log.info("Found Class To Patch, Name:{}, TransformedName:{}", name, transformedName);
                return transform(name, patchNote, bytes);
            }
        }

        return bytes;
    }

    private static byte[] transform(String obfName, PatchNote patchNote, byte[] bytes) {

        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        if (patchNote.methodsToPatch.isEmpty())
            return bytes;

        for (MethodNote methodNote : patchNote.methodsToPatch) {

            MethodNode sourceMethod = null;
            MethodNode replacementMethod = null;

            try {

                for (MethodNode method : classNode.methods) {
                    if (methodNote.srgMethodName.equals(remapper.mapMethodName(obfName, method.name, method.desc))) {
                        log.trace("Found Method to Patch: {}@{}", method.name, method.desc);
                        sourceMethod = method;
                        break;
                    } else if (methodNote.methodName.equals(method.name) && methodNote.deobfDesc.equals(method.desc)) {
                        log.trace("Found Deobfuscated Method to Patch: {}@{}", method.name, method.desc);
                        sourceMethod = method;
                    }
                }


                ClassNode replacementClass = loadClass(patchNote.replacementClass);
                for (MethodNode method : replacementClass.methods) {
                    if (methodNote.srgMethodName.equals(remapper.mapMethodName(patchNote.replacementClass, method.name, method.desc))) {
                        log.trace("Found Replacement Method: {}@{}", method.name, method.desc);
                        replacementMethod = method;
                        break;
                    } else if (methodNote.methodName.equals(method.name) && methodNote.deobfDesc.equals(method.desc)) {
                        log.trace("Found Deobfuscated Replacement Method: {}@{}", method.name, method.desc);
                        replacementMethod = method;
                        break;
                    }
                }
            } catch (Throwable t) {
                log.warn("Failed to Map Replacement Method: {}", methodNote.methodName, t);
            }

            if (sourceMethod != null && replacementMethod != null) {
                log.info("Successfully Mapped Method to be Replaced");
                log.debug("  Source: {}@{} Replacement: {}@{}", sourceMethod.name, sourceMethod.desc, replacementMethod.name, replacementMethod.desc);
                classNode.methods.remove(sourceMethod);
                classNode.methods.add(replacementMethod);

            } else {
                log.info("Couldn't match methods to patch, skipping");
                return bytes;
            }
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    public static void addPatch(PatchNote patchNote) {

        log.trace("Registering ASM Patch: {}", patchNote.sourceClass);
        log.trace("  Replacement: {}", patchNote.replacementClass);

        for (MethodNote note : patchNote.methodsToPatch) {
            log.trace("  Method: {}", note.methodName);
            log.trace("  SRG: {}", note.srgMethodName);
        }

        patches.add(patchNote);
    }

    private static ClassNode loadClass(String className) throws IOException {

        LaunchClassLoader loader = (LaunchClassLoader) STPClassTransformer.class.getClassLoader();
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(loader.getClassBytes(className));
        classReader.accept(classNode, 0);
        return classNode;
    }

    public static class PatchNote {

        public final String sourceClass;
        public final String replacementClass;

        public final Set<MethodNote> methodsToPatch = new HashSet<>();

        public PatchNote(String sourceClass, String replacementClass) {
            this.sourceClass = sourceClass;
            this.replacementClass = replacementClass;
        }

        public void addMethodToPatch(MethodNote methodNote) {

            methodsToPatch.add(methodNote);
        }
    }

    public static class MethodNote {

        public final String methodName;
        public final String srgMethodName;
        public final String deobfDesc;

        public MethodNote(String methodName, String srgMethodName, String deobfDesc) {

            this.methodName = methodName;
            this.srgMethodName = srgMethodName;
            this.deobfDesc = deobfDesc;
        }
    }
}
