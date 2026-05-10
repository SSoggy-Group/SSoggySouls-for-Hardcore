import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Enumeration;

public class FindClass {
    public static void main(String[] args) throws IOException {
        Files.walkFileTree(Paths.get(System.getProperty("user.home"), ".gradle", "caches"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".jar") && file.toString().contains("minecraft") && file.toString().contains("1.21.1")) {
                    try (ZipFile zf = new ZipFile(file.toFile())) {
                        Enumeration<? extends ZipEntry> entries = zf.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            if (entry.getName().contains("net/minecraft/world/entity/EntityEvent") || entry.getName().contains("net/minecraft/world/entity/EntityStatus")) {
                               System.out.println("Found: " + file.toString() + " -> " + entry.getName());
                            }
                        }
                    } catch (Exception e) {}
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
