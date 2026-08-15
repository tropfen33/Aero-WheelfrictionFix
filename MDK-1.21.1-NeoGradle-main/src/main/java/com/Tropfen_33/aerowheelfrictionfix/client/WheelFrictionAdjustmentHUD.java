package com.Tropfen_33.aerowheelfrictionfix.client;

import com.Tropfen_33.aerowheelfrictionfix.network.UpdateWheelFrictionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class WheelFrictionAdjustmentHUD extends Screen {
    private final BlockPos blockPos;
    private double currentFriction;
    private int sliderX, sliderY, sliderWidth, sliderHeight;
    // Start as true immediately so holding right-click from the block translates straight into dragging
    private boolean isDragging = true;

    public WheelFrictionAdjustmentHUD(BlockPos blockPos, double initialFriction) {
        super(Component.literal("Wheel Friction Adjuster"));
        this.blockPos = blockPos;
        this.currentFriction = initialFriction;
    }

    @Override
    protected void init() {
        super.init();
        this.sliderWidth = 160;
        this.sliderHeight = 20;
        this.sliderX = (this.width - sliderWidth) / 2;
        this.sliderY = (this.height - sliderHeight) / 2;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Keeps background clear and unblurred
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // If we are dragging right from opening, update position based on current mouse coordinates
        if (isDragging) {
            updateFrictionFromMouse(mouseX);
        }

        guiGraphics.fill(0, 0, this.width, this.height, 0x30000000);
        
        int panelX = sliderX - 10;
        int panelY = sliderY - 25;
        int panelWidth = sliderWidth + 20;
        int panelHeight = 65;

        // Draw panel background and manual borders
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xEA101010);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF555555); 
        guiGraphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, 0xFF555555); 
        guiGraphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, 0xFF555555); 
        guiGraphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF555555); 

        // Title text
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, sliderY - 18, 0xFFFFFF);

        // Slider Track
        guiGraphics.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + sliderHeight, 0xFF333333);

        // Slider Handle
        int handleX = sliderX + (int) (currentFriction * (sliderWidth - 8));
        guiGraphics.fill(handleX, sliderY, handleX + 8, sliderY + sliderHeight, 0xFFE08030);

        // Value display text
        String valueStr = String.format("Friction: %.3f", currentFriction);
        guiGraphics.drawCenteredString(this.font, valueStr, this.width / 2, sliderY + 24, 0xCCCCCC);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            isDragging = true;
            updateFrictionFromMouse(mouseX);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            updateFrictionFromMouse(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // When right mouse button is let go, save and close
        if (isDragging) {
            isDragging = false;
            PacketDistributor.sendToServer(new UpdateWheelFrictionPayload(blockPos, currentFriction));
            onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateFrictionFromMouse(double mouseX) {
        double relativeX = mouseX - sliderX;
        double ratio = relativeX / (double) sliderWidth;
        this.currentFriction = Math.clamp(ratio, 0.0, 1.0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}