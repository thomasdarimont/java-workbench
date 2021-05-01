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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Compile:
 * <pre>javac --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED  -d target/classes src/main/java/wb/java17/MainMethodFinder.java</pre>
 * <p>
 * Run:
 * <pre>java -DshowCommand=true --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED -cp target/classes wb.java17.MainMethodFinder</pre>
 * <p>
 * Run with different Java Home:
 * <pre>java -DshowCommand=true --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED -cp target/classes wb.java17.MainMethodFinder ~/.sdkman/candidates/java/8.0.282.hs-adpt</pre>
 * <p>
 * Compile with GraalVM Native Image:
 * <pre>native-image -cp target/classes wb.java17.MainMethodFinder MainMethodFinder</pre>
 * <p>
 * Run GraalVM Native Image
 * <pre>./MainMethodFinder -DshowCommand=true ~/.sdkman/candidates/java/8.0.282.hs-adpt</pre>
 * <pre>./MainMethodFinder -DshowCommand=true ~/.sdkman/candidates/java/11.0.10.hs-adpt</pre>
 */
public class MainMethodFinder {

    private static final boolean VERBOSE = Boolean.getBoolean("verbose");

    private static final boolean IS_NATIVE_IMAGE = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    private static final boolean SHOW_COMMAND = Boolean.getBoolean("showCommand");

    public static void main(String[] args) throws IOException {

        long time = System.nanoTime();
        try {

            boolean customJdkPathProvided = args.length > 0;
            if (!customJdkPathProvided && IS_NATIVE_IMAGE) {
                System.err.println("MainMethodFinder: Missing path operand.");
                System.err.println("Usage: MainMethodFinder /path/to/jdk");
                System.exit(-1);
                return;
            }

            var jdkHomePath = customJdkPathProvided
                    ? Path.of(args[0])
                    : detectCurrentJdkPath();

            if (VERBOSE) {
                System.out.printf("Scanning Java Installation: %s%n", jdkHomePath);
            }

            MainMethodReportingVisitor visitor = new MainMethodReportingVisitor();
            Files.walkFileTree(jdkHomePath, visitor);

            Consumer<MainMethod> printMainMethodInfo = mainMethod -> {

                String libraryFileName = mainMethod.library.getName();
                if (libraryFileName.endsWith(".jar")) {
                    libraryFileName = jdkHomePath.relativize(mainMethod.library.toPath()).toString();
                }
                String mainClassName = mainMethod.className;
                String libraryName = libraryFileName.replaceAll("\\.jmod", "");

                if (SHOW_COMMAND) {
                    String launchCommand = generateLaunchCommand(!customJdkPathProvided, jdkHomePath, libraryFileName, mainClassName, libraryName);
                    System.out.printf("%s%n", launchCommand);
                } else {
                    System.out.printf("%s %s%n", libraryName, mainClassName);
                }
            };

            var mainMethods = visitor.waitForCompletionAndReturnMainMethods();
            mainMethods.forEach(printMainMethodInfo);
        } finally {
            time = System.nanoTime() - time;
            if (VERBOSE) {
                System.out.printf("time = %dms%n", TimeUnit.NANOSECONDS.toMillis(time));
                // System.out.printf("ForkJoin Pool Stats: %s%n", ForkJoinPool.commonPool());
            }
        }
    }

    private static String generateLaunchCommand(boolean useCurrentJdk, Path jdkHomePath, String libraryFileName, String mainClassName, String libraryName) {

        var launchCommand = (useCurrentJdk ? "" : jdkHomePath.toFile().getAbsolutePath() + "/bin/") + "java";

        if (libraryFileName.endsWith(".jmod")) {
            launchCommand += (useCurrentJdk ? "" : " --module-path " + jdkHomePath.toFile().getAbsolutePath() + "/jmods");
            launchCommand += " -m " + libraryName + "/" + mainClassName;
        } else if (libraryFileName.endsWith(".jar")) {
            launchCommand += " -cp " + jdkHomePath.toFile().getAbsolutePath() + "/" + libraryFileName;
            launchCommand += " " + mainClassName;
        } else {
            launchCommand = String.format("Could not generate launch command for: %s %s", libraryFileName, mainClassName);
        }

        return launchCommand;
    }

    private static Path detectCurrentJdkPath() {
        return Paths.get(ProcessHandle.current().info().command().orElseThrow()).resolve("../..").normalize();
    }

    static class MainMethodReportingVisitor extends SimpleFileVisitor<Path> {

        private final CopyOnWriteArrayList<MainMethod> mainMethods = new CopyOnWriteArrayList<>();

        private final Queue<RecursiveAction> outstanding = new ConcurrentLinkedQueue<>();

        @Override
        public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
            var maybeJdkLibrary = filePath.toFile();
            if (isJdkLibrary(maybeJdkLibrary.getName())) {
                var action = new RecursiveAction() {
                    protected void compute() {
                        scanLibraryForMainClasses(maybeJdkLibrary);
                    }
                };
                action.fork();
                outstanding.add(action);
            }
            return FileVisitResult.CONTINUE;
        }

        public List<MainMethod> waitForCompletionAndReturnMainMethods() {

            for (RecursiveAction action; (action = outstanding.poll()) != null; ) {
                action.join();
            }

            List<MainMethod> result = new ArrayList<>(this.mainMethods);
            this.mainMethods.clear();

            var cmp = Comparator
                    .comparing((MainMethod left) -> left.library.getName())
                    .thenComparing((MainMethod left) -> left.className);
            result.sort(cmp);

            return result;
        }

        private boolean isJdkLibrary(String maybeJdkLibraryName) {
            return maybeJdkLibraryName.endsWith(".jar") || maybeJdkLibraryName.endsWith(".jmod");
        }

        private void scanLibraryForMainClasses(File library) {
            // Using FileSystems.newFileSystem(library.toPath(), (ClassLoader)null) for Java 11 compatibility
            try {
                try (var fileSystem = FileSystems.newFileSystem(library.toPath(), (ClassLoader) null)) {
                    var root = fileSystem.getRootDirectories().iterator().next();
                    var visitor = new MainMethodVisitor(library, mainMethods::add, fileSystem);
                    Files.walk(root).parallel().filter(this::isClassFile).forEach(visitor::scanClassForMainMethod);
                }
            } catch (Exception e) {
                System.err.println("Could not scan library: " + library.getAbsolutePath());
                e.printStackTrace();
            }
        }

        private boolean isClassFile(Path nestedFilePath) {
            return nestedFilePath.toString().endsWith(".class");
        }
    }

    static class MainMethod {

        File library;

        String className;

        public MainMethod(File library, String className) {
            this.library = library;
            this.className = className;
        }
    }

    static class MainMethodVisitor extends ClassVisitor {

        // Adapted from Opcodes.ASM*
        private static final int ASM6 = 6 << 16;
        private static final int ASM7 = 7 << 16;
        private static final int ASM8 = 8 << 16;

        private static final int ASM_API_VERSION =
                // Opcodes.ASM8 // not using this constant to support running on Java11-17
                System.getProperty("java.version").startsWith("11.")
                        ? ASM6
                        : System.getProperty("java.version").startsWith("15.")
                        ? ASM7
                        : ASM8;

        private final File library;
        private final FileSystem fileSystem;
        private final Consumer<MainMethod> mainMethodConsumer;
        private final ThreadLocal<String> currentInternalClassName;

        public MainMethodVisitor(File library, Consumer<MainMethod> mainMethodConsumer, FileSystem fileSystem) {
            super(ASM_API_VERSION);
            this.library = library;
            this.fileSystem = fileSystem;
            this.mainMethodConsumer = mainMethodConsumer;
            this.currentInternalClassName = new ThreadLocal<>();
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            currentInternalClassName.set(name);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            currentInternalClassName.remove();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (isRunnableMainMethod(access, name, descriptor)) {
                mainMethodConsumer.accept(new MainMethod(library, currentInternalClassName.get().replace('/', '.')));
            }
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        private boolean isRunnableMainMethod(int access, String name, String descriptor) {
            return "main".equals(name)
                    && (access & Opcodes.ACC_STATIC) != 0
                    && "([Ljava/lang/String;)V".equals(descriptor);
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
