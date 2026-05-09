import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Enumeration;

public class ListClasses {
    public static void main(String[] args) throws Exception {
        ZipFile zf = new ZipFile("/home/jules/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.21.1-52.1.0_mapped_official_1.21.1/forge-1.21.1-52.1.0_mapped_official_1.21.1-srg.jar");
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().startsWith("net/minecraft/world/entity/E")) {
                System.out.println(entry.getName());
            }
        }
    }
}
