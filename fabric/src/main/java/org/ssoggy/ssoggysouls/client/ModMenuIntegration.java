package org.ssoggy.ssoggysouls.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.ssoggy.ssoggysouls.util.ConfigManager;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new SimpleConfigScreen(parent);
    }

    public static class SimpleConfigScreen extends Screen {
        private final Screen parent;

        protected SimpleConfigScreen(Screen parent) {
            super(Text.literal("SSoggySouls Configuration"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int y = this.height / 4;
            
            // Toggle HRM
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("HRM Enabled: " + ConfigManager.getConfig().isHrmEnabled()),
                button -> {
                    ConfigManager.getConfig().setHrmEnabled(!ConfigManager.getConfig().isHrmEnabled());
                    button.setMessage(Text.literal("HRM Enabled: " + ConfigManager.getConfig().isHrmEnabled()));
                    ConfigManager.save();
                }
            ).dimensions(this.width / 2 - 100, y, 200, 20).build());

            // Default Lives
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Default Lives: " + ConfigManager.getConfig().getDefaultLives()),
                button -> {
                    int nextLives = (ConfigManager.getConfig().getDefaultLives() % 10) + 1;
                    ConfigManager.getConfig().setDefaultLives(nextLives);
                    button.setMessage(Text.literal("Default Lives: " + ConfigManager.getConfig().getDefaultLives()));
                    ConfigManager.save();
                }
            ).dimensions(this.width / 2 - 100, y + 25, 200, 20).build());

            // Back button
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), button -> {
                this.client.setScreen(this.parent);
            }).dimensions(this.width / 2 - 100, this.height - 40, 200, 20).build());
        }

        @Override
        public void close() {
            this.client.setScreen(this.parent);
        }
    }
}
