import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class Test {
    public static void main(String[] args) {
        Component c = Component.literal("Test");
        MutableComponent mc = c.copy();
        mc.withStyle(s -> s.withColor(net.minecraft.ChatFormatting.RED));
    }
}
