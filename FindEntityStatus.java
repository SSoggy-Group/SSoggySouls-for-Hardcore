import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Enumeration;

public class FindEntityStatus {
    public static void main(String[] args) throws IOException {
        Files.walkFileTree(Paths.get(System.getProperty("user.home"), ".gradle", "caches"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".jar") && file.toString().contains("minecraft")) {
                    try (ZipFile zf = new ZipFile(file.toFile())) {
                        Enumeration<? extends ZipEntry> entries = zf.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            if (entry.getName().endsWith("EntityStatus.class") || entry.getName().endsWith("EntityEvent.class") || entry.getName().endsWith("EntityStatuses.class") || entry.getName().contains("USE_TOTEM")) {
                                System.out.println("Found in: " + file.toString() + " -> " + entry.getName());
                            }
                        }
                    } catch (Exception e) {}
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
