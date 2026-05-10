import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Enumeration;

public class ListClasses {
    public static void main(String[] args) throws Exception {
        String path = System.getProperty("user.home") + "/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.21.1-52.1.0_mapped_official_1.21.1/forge-1.21.1-52.1.0_mapped_official_1.21.1-srg.jar";
        File jarFile = new File(path);
        if (!jarFile.exists()) {
            System.err.println("Could not find jar at: " + path);
            return;
        }
        try (ZipFile zf = new ZipFile(jarFile)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith("net/minecraft/world/entity/E")) {
                    System.out.println(entry.getName());
                }
            }
        }
    }
}
