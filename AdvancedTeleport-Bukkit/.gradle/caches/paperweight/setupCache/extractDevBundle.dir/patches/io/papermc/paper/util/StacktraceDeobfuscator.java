package io.papermc.paper.util;

import io.papermc.paper.configuration.GlobalConfiguration;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@DefaultQualifier(NonNull.class)
public enum StacktraceDeobfuscator {
    INSTANCE;

    private final Map<Class<?>, Map<String, IntList>> lineMapCache = Collections.synchronizedMap(new LinkedHashMap<>(128, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<Class<?>, Map<String, IntList>> eldest) {
            return this.size() > 127;
        }
    });

    public void deobfuscateThrowable(final Throwable throwable) {
        if (GlobalConfiguration.get() != null && !GlobalConfiguration.get().logging.deobfuscateStacktraces) { // handle null as true
            return;
        }

        throwable.setStackTrace(this.deobfuscateStacktrace(throwable.getStackTrace()));
        final Throwable cause = throwable.getCause();
        if (cause != null) {
            this.deobfuscateThrowable(cause);
        }
        for (final Throwable suppressed : throwable.getSuppressed()) {
            this.deobfuscateThrowable(suppressed);
        }
    }

    public StackTraceElement[] deobfuscateStacktrace(final StackTraceElement[] traceElements) {
        if (GlobalConfiguration.get() != null && !GlobalConfiguration.get().logging.deobfuscateStacktraces) { // handle null as true
            return traceElements;
        }

        final @Nullable Map<String, ObfHelper.ClassMapping> mappings = ObfHelper.INSTANCE.mappingsByObfName();
        if (mappings == null || traceElements.length == 0) {
            return traceElements;
        }
        final StackTraceElement[] result = new StackTraceElement[traceElements.length];
        for (int i = 0; i < traceElements.length; i++) {
            final StackTraceElement element = traceElements[i];

            final String className = element.getClassName();
            final String methodName = element.getMethodName();

            final ObfHelper.ClassMapping classMapping = mappings.get(className);
            if (classMapping == null) {
                result[i] = element;
                continue;
            }

            final Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (final ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            final @Nullable String methodKey = this.determineMethodForLine(clazz, element.getLineNumber());
            final @Nullable String mappedMethodName = methodKey == null ? null : classMapping.methodsByObf().get(methodKey);

            result[i] = new StackTraceElement(
                element.getClassLoaderName(),
                element.getModuleName(),
                element.getModuleVersion(),
                classMapping.mojangName(),
                mappedMethodName != null ? mappedMethodName : methodName,
                sourceFileName(classMapping.mojangName()),
                element.getLineNumber()
            );
        }
        return result;
    }

    private @Nullable String determineMethodForLine(final Class<?> clazz, final int lineNumber) {
        final Map<String, IntList> lineMap = this.lineMapCache.computeIfAbsent(clazz, StacktraceDeobfuscator::buildLineMap);
        for (final var entry : lineMap.entrySet()) {
            final String methodKey = entry.getKey();
            final IntList lines = entry.getValue();
            for (int i = 0, linesSize = lines.size(); i < linesSize; i++) {
                final int num = lines.getInt(i);
                if (num == lineNumber) {
                    return methodKey;
                }
            }
        }
        return null;
    }

    private static String sourceFileName(final String fullClassName) {
        final int dot = fullClassName.lastIndexOf('.');
        final String className = dot == -1
            ? fullClassName
            : fullClassName.substring(dot + 1);
        final String rootClassName = className.split("\\$")[0];
        return rootClassName + ".java";
    }

    private static Map<String, IntList> buildLineMap(final Class<?> key) {
        final Map<String, IntList> lineMap = new HashMap<>();
        final class LineCollectingMethodVisitor extends MethodVisitor {
            private final IntList lines = new IntArrayList();
            private final String name;
            private final String descriptor;

            LineCollectingMethodVisitor(String name, String descriptor) {
                super(Opcodes.ASM9);
                this.name = name;
                this.descriptor = descriptor;
            }

            @Override
            public void visitLineNumber(int line, Label start) {
                super.visitLineNumber(line, start);
                this.lines.add(line);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                lineMap.put(ObfHelper.methodKey(this.name, this.descriptor), this.lines);
            }
        }
        final ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new LineCollectingMethodVisitor(name, descriptor);
            }
        };
        try {
            final @Nullable InputStream inputStream = StacktraceDeobfuscator.class.getClassLoader()
                .getResourceAsStream(key.getName().replace('.', '/') + ".class");
            if (inputStream == null) {
                throw new IllegalStateException("Could not find class file: " + key.getName());
            }
            final byte[] classData;
            try (inputStream) {
                classData = inputStream.readAllBytes();
            }
            final ClassReader reader = new ClassReader(classData);
            reader.accept(classVisitor, 0);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        return lineMap;
    }
}
