package wb.java17;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class ModuleFileAccessExample {

    public static void main(String[] args) throws Exception {

        Map<String, String> env = new HashMap<>();
        // set java.home property to be underlying java.home
        // so that jrt-fs.jar loading is exercised.
        env.put("java.home", System.getProperty("java.home"));
        FileSystem fs = FileSystems.newFileSystem(URI.create("jrt:/"), env);

        fs.getRootDirectories().forEach(rootPath -> {

            System.out.println("Analyzing rootPath: " + rootPath);

            try {
                Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                        System.out.println("Visit file: " + file);

                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
