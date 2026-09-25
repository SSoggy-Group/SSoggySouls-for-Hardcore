package org.ssoggy.ssoggysouls.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.ssoggy.ssoggysouls.util.ConfigManager;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SimpleConfigScreen::new;
    }

    public static class SimpleConfigScreen extends Screen {
        private final Screen parent;

        protected SimpleConfigScreen(Screen parent) {
            super(Component.literal("SSoggySouls Configuration"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int y = this.height / 4;
            
            // Toggle HRM
            this.addRenderableWidget(Button.builder(
                Component.literal("HRM Enabled: " + ConfigManager.getConfig().isHrmEnabled()),
                button -> {
                    ConfigManager.getConfig().setHrmEnabled(!ConfigManager.getConfig().isHrmEnabled());
                    button.setMessage(Component.literal("HRM Enabled: " + ConfigManager.getConfig().isHrmEnabled()));
                    ConfigManager.save();
                }
            ).bounds(this.width / 2 - 100, y, 200, 20)
            .tooltip(Tooltip.create(Component.literal("Toggles Hardcore Revive Mode. When enabled, players drop heads and can be revived via rituals.")))
            .build());

            // Default Lives
            this.addRenderableWidget(Button.builder(
                Component.literal("Default Lives: " + ConfigManager.getConfig().getDefaultLives()),
                button -> {
                    int nextLives = (ConfigManager.getConfig().getDefaultLives() % 10) + 1;
                    ConfigManager.getConfig().setDefaultLives(nextLives);
                    button.setMessage(Component.literal("Default Lives: " + ConfigManager.getConfig().getDefaultLives()));
                    ConfigManager.save();
                }
            ).bounds(this.width / 2 - 100, y + 25, 200, 20)
            .tooltip(Tooltip.create(Component.literal("Sets the default number of lives a new player starts with (1-10).")))
            .build());

            // Back button
            this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), ignored -> this.minecraft.setScreenAndShow(this.parent))
                .bounds(this.width / 2 - 100, this.height - 40, 200, 20).build());
        }

        @Override
        public void onClose() {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }
}
