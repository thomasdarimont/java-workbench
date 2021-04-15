package wb.java17;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * <pre>javac --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED  -d target/classes src/main/java/wb/java17/MainMethodFinder.java </pre>
 * <pre>java --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED -cp target/classes wb.java17.MainMethodFinder</pre>
 */
public class MainMethodFinder {

    public static void main(String[] args) throws IOException {

        System.out.printf("Java Version: %s%n", Runtime.version());

        var javaCommandPath = ProcessHandle.current().info().command().orElseThrow();
        var jdkHomePath = Paths.get(javaCommandPath).resolve("../..").normalize();

        BiConsumer<File, String> mainMethodReporter = (libraryFile, mainClassName) -> {
            System.out.printf("Found main in %s: %s %n", libraryFile.getName(), mainClassName);
        };

        Files.walkFileTree(jdkHomePath, new MainMethodReportingVisitor(mainMethodReporter));
    }

    static class MainMethodReportingVisitor extends SimpleFileVisitor<Path> {

        private final BiConsumer<File, String> consumer;

        public MainMethodReportingVisitor(BiConsumer<File, String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
            var maybeJdkLibrary = filePath.toFile();
            if (isJdkLibrary(maybeJdkLibrary.getName())) {
                scanLibraryForMainClasses(maybeJdkLibrary);
            }
            return FileVisitResult.CONTINUE;
        }

        private boolean isJdkLibrary(String maybeJdkLibraryName) {
            return maybeJdkLibraryName.endsWith(".jar") || maybeJdkLibraryName.endsWith(".jmod");
        }

        private void scanLibraryForMainClasses(File library) {
            try (var fileSystem = FileSystems.newFileSystem(library.toPath())) {
                var root = fileSystem.getRootDirectories().iterator().next();
                var visitor = new MainMethodVisitor(library, consumer, fileSystem);
                Files.walk(root).filter(this::isClassFile).forEach(visitor::scanClassForMainMethod);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isClassFile(Path nestedFilePath) {
            return nestedFilePath.toString().endsWith(".class");
        }
    }

    static class MainMethodVisitor extends ClassVisitor {

        private String currentInternalClassName;

        private final File library;
        private final BiConsumer<File, String> mainMethodConsumer;
        private final FileSystem fileSystem;

        public MainMethodVisitor(File library, BiConsumer<File, String> mainMethodConsumer, FileSystem fileSystem) {
            super(Opcodes.ASM8);
            this.library = library;
            this.mainMethodConsumer = mainMethodConsumer;
            this.fileSystem = fileSystem;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            currentInternalClassName = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (name.equals("main")) {
                mainMethodConsumer.accept(library, currentInternalClassName.replace('/', '.'));
            }
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        private void scanClassForMainMethod(Path pathToLibraryClass) {
            tryGetClassBytes(pathToLibraryClass, fileSystem).ifPresent(this::visitClassBytes);
        }

        private void visitClassBytes(byte[] classBytes) {
            new ClassReader(classBytes).accept(this, 0);
        }

        private Optional<byte[]> tryGetClassBytes(Path nestedFilePath, FileSystem fileSystem) {
            try (var is = new BufferedInputStream(fileSystem.provider().newInputStream(nestedFilePath))) {
                return Optional.of(is.readAllBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }
    }
}
