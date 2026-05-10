import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Enumeration;
import java.io.InputStream;
import java.util.Scanner;

public class FindTotemEvent {
    public static void main(String[] args) throws IOException {
        Files.walkFileTree(Paths.get(System.getProperty("user.home"), ".gradle", "caches"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith("-sources.jar") && file.toString().contains("minecraft") && file.toString().contains("1.21.1")) {
                    try (ZipFile zf = new ZipFile(file.toFile())) {
                        Enumeration<? extends ZipEntry> entries = zf.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            if (entry.getName().contains("world/entity/Entity.java.patch")) {
                                try (InputStream is = zf.getInputStream(entry); Scanner scanner = new Scanner(is)) {
                                    while (scanner.hasNextLine()) {
                                        String line = scanner.nextLine();
                                        if (line.contains("broadcastEntityEvent")) {
                                            System.out.println(line);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {}
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
