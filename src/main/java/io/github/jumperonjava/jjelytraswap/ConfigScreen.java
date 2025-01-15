package io.github.jumperonjava.jjelytraswap;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

//THIS CLASS IS UNUSED

public class ConfigScreen extends Screen {

    public ConfigScreen(Screen parent) {
        super(Text.empty());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(client.textRenderer,
                "Hello, world",
                width / 2,
                height / 2,
                0xFFFFFFFF);
    }

    public static ConfigScreen createConfigScreen(Screen parent) {
        return new ConfigScreen(parent);
    }
}
