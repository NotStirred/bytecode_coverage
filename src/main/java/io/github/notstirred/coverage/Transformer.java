package io.github.notstirred.coverage;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;

import static org.objectweb.asm.Opcodes.ASM9;

public class Transformer implements ClassFileTransformer {
    public static int idx = 0;

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] clazz) {
        try {
            if (!(className.startsWith("net/minecraft") || className.startsWith("io/github/opencubicchunks"))) { // FIXME: replace this with filter passed in through system properties or javaagent arguments.
                return clazz;
            }

            ClassReader classReader = new ClassReader(clazz);
            ClassNode classNode = new ClassNode(ASM9);

            classReader.accept(classNode, 0);

            List<MethodNode> methods = classNode.methods;
            for (int i = 0, methodsSize = methods.size(); i < methodsSize; i++) {
                MethodNode methodNode = methods.get(i);

                if (methodNode.instructions.size() == 0) {
                    // method is abstract or an interface, don't track it to avoid showing in coverage output.
                    continue;
                }

                int startIdx = idx;
                MethodNode dstMethodNode = new MethodNode(ASM9, methodNode.access, methodNode.name, methodNode.desc, methodNode.signature, methodNode.exceptions.toArray(new String[0]));
                CoverageAddingMethodVisitor methodVisitor = new CoverageAddingMethodVisitor(dstMethodNode);
                methodNode.accept(methodVisitor);

                int packageEndIdx = classNode.name.lastIndexOf('/');
                String packageName = classNode.name.substring(0, packageEndIdx);
                String simpleClassName = classNode.name.substring(packageEndIdx + 1);

                Attach.METHOD_INDICES.computeIfAbsent(packageName, n -> new Object2ObjectOpenHashMap<>())
                        .computeIfAbsent(simpleClassName, n -> new Object2ObjectOpenHashMap<>())
                        .put(new IntPair(startIdx, idx), new Attach.MethodData(methodNode, methodVisitor.getLines()));

                methods.set(i, dstMethodNode);
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Throwable t) {
            System.out.println("Failed to transform class " + className + "\nCoverage will be unavailable for this class.");
            t.printStackTrace();
            return clazz;
        }
    }
}
