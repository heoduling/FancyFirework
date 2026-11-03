package ffprobe;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;

/** Test-only observer: counts real core snapshots without changing their result. */
public final class SnapshotAgent {
    public static void premain(String args, Instrumentation instrumentation) throws Exception {
        instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(args));
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(Module module, ClassLoader loader, String name,
                                    Class<?> previous, ProtectionDomain domain, byte[] bytes) {
                if (!name.equals("org/bukkit/craftbukkit/block/CraftBlockEntityState")) return null;
                byte[] transformed = ClassFile.of().transformClass(ClassFile.of().parse(bytes),
                        ClassTransform.transformingMethodBodies(
                                method -> method.methodName().equalsString("createSnapshot"),
                                new CodeTransform() {
                                    @Override public void atStart(CodeBuilder builder) {
                                        builder.invokestatic(ClassDesc.of("ffprobe.SnapshotCounter"), "hit",
                                                MethodTypeDesc.of(ConstantDescs.CD_void));
                                    }
                                    @Override public void accept(CodeBuilder builder, CodeElement element) {
                                        builder.with(element);
                                    }
                                }));
                System.setProperty("ffprobe.snapshot.instrumented", "true");
                System.out.println("HOLDER_AGENT instrumented CraftBlockEntityState.createSnapshot");
                return transformed;
            }
        });
    }
}
