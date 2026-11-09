package com.nightmarecore.client;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.menu.NightmareForgeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class NightmareForgeScreen extends AbstractContainerScreen<NightmareForgeMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NightmareCore.MOD_ID, "textures/gui/nightmare_forge.png");

    public NightmareForgeScreen(NightmareForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int progress = this.menu.getProgress();
        int max = Math.max(1, this.menu.getMaxProgress());
        int width = Math.max(0, Math.min(22, progress * 22 / max));
        if (width > 0) {
            graphics.blit(TEXTURE, this.leftPos + 86, this.topPos + 35, 176, 0, width, 16);
        }
        graphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 53, 176, 16, 16, 16);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
