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

        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));

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
