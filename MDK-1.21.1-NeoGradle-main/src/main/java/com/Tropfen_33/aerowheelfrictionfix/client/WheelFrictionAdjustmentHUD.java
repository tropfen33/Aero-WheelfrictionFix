package com.Tropfen_33.aerowheelfrictionfix.client;

import com.Tropfen_33.aerowheelfrictionfix.network.UpdateWheelFrictionPayload;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class WheelFrictionAdjustmentHUD extends Screen {

    private final BlockPos blockPos;

    /*
     * The friction value currently displayed by the slider.
     */
    private double currentFriction;

    /*
     * The friction value that was loaded when the HUD opened.
     *
     * This is kept separately so nothing can accidentally
     * overwrite it when the screen first opens.
     */
    private final double startingFriction;

    private int sliderX;
    private int sliderY;
    private int sliderWidth;
    private int sliderHeight;

    /*
     * Right mouse is considered held because the HUD was
     * opened by holding right mouse.
     */
    private boolean isDragging = true;

    /*
     * Used to detect real mouse movement.
     */
    private double lastMouseX;

    public WheelFrictionAdjustmentHUD(
            BlockPos blockPos,
            double initialFriction
    ) {
        super(Component.literal("Wheel Friction Adjuster"));

        this.blockPos = blockPos;

        /*
         * Save the original value.
         */
        this.startingFriction = Math.clamp(
                initialFriction,
                0.0,
                1.0
        );

        /*
         * The slider starts exactly at the saved value.
         */
        this.currentFriction = this.startingFriction;
    }

    @Override
    protected void init() {
        super.init();

        this.sliderWidth = 160;
        this.sliderHeight = 20;

        this.sliderX =
                (this.width - this.sliderWidth) / 2;

        this.sliderY =
                (this.height - this.sliderHeight) / 2;

        /*
         * Put the cursor at the position represented by
         * the saved friction value.
         */
        positionMouseAtCurrentFriction();

        /*
         * Remember the mouse position that we just set.
         *
         * This is important:
         * the first frame will NOT count as mouse movement.
         */
        Minecraft minecraft =
                Minecraft.getInstance();

        Window window =
                minecraft.getWindow();

        this.lastMouseX =
                minecraft.mouseHandler.xpos();
    }

    private void positionMouseAtCurrentFriction() {

        Minecraft minecraft =
                Minecraft.getInstance();

        Window window =
                minecraft.getWindow();

        /*
         * Calculate the desired cursor position in GUI
         * coordinates.
         */
        double guiMouseX =
                sliderX
                        + currentFriction * sliderWidth;

        double guiMouseY =
                sliderY
                        + sliderHeight / 2.0;

        /*
         * Convert GUI coordinates to actual window pixels.
         */
        double windowX =
                guiMouseX
                        * window.getScreenWidth()
                        / (double) window.getGuiScaledWidth();

        double windowY =
                guiMouseY
                        * window.getScreenHeight()
                        / (double) window.getGuiScaledHeight();

        /*
         * Move the actual GLFW cursor.
         */
        GLFW.glfwSetCursorPos(
                window.getWindow(),
                windowX,
                windowY
        );
    }

    @Override
    public void renderBackground(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        /*
         * No background blur.
         */
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        /*
         * Get the actual mouse position.
         */
        double actualMouseX =
                minecraft.mouseHandler.xpos();

        /*
         * Only update the friction if the mouse has ACTUALLY
         * moved since the HUD was opened.
         *
         * This prevents the initial cursor positioning from
         * changing the value.
         */
        if (isDragging
                && minecraft.mouseHandler.isRightPressed()
                && Math.abs(actualMouseX - lastMouseX) > 0.01) {

            Window window =
                    minecraft.getWindow();

            double scaledMouseX =
                    actualMouseX
                            * window.getGuiScaledWidth()
                            / (double) window.getScreenWidth();

            updateFrictionFromMouse(
                    scaledMouseX
            );
        }

        /*
         * Remember the current mouse position.
         */
        lastMouseX = actualMouseX;

        /*
         * Transparent overlay.
         */
        guiGraphics.fill(
                0,
                0,
                this.width,
                this.height,
                0x30000000
        );

        int panelX =
                sliderX - 10;

        int panelY =
                sliderY - 25;

        int panelWidth =
                sliderWidth + 20;

        int panelHeight =
                65;

        /*
         * Panel background.
         */
        guiGraphics.fill(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight,
                0xEA101010
        );

        /*
         * Borders.
         */
        guiGraphics.fill(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + 1,
                0xFF555555
        );

        guiGraphics.fill(
                panelX,
                panelY + panelHeight - 1,
                panelX + panelWidth,
                panelY + panelHeight,
                0xFF555555
        );

        guiGraphics.fill(
                panelX,
                panelY,
                panelX + 1,
                panelY + panelHeight,
                0xFF555555
        );

        guiGraphics.fill(
                panelX + panelWidth - 1,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight,
                0xFF555555
        );

        /*
         * Title.
         */
        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                sliderY - 18,
                0xFFFFFF
        );

        /*
         * Slider track.
         */
        guiGraphics.fill(
                sliderX,
                sliderY,
                sliderX + sliderWidth,
                sliderY + sliderHeight,
                0xFF333333
        );

        /*
         * Slider handle.
         *
         * The handle is positioned from currentFriction.
         *
         * currentFriction is initialized from the saved
         * wheel value, so it should be in the correct place
         * immediately when the HUD appears.
         */
        int handleX =
                sliderX
                        + (int) (
                        currentFriction
                                * sliderWidth
                )
                        - 4;

        /*
         * Keep the handle inside the slider.
         */
        handleX =
                Math.clamp(
                        handleX,
                        sliderX,
                        sliderX + sliderWidth - 8
                );

        guiGraphics.fill(
                handleX,
                sliderY,
                handleX + 8,
                sliderY + sliderHeight,
                0xFFE08030
        );

        /*
         * Current friction value.
         */
        String valueStr =
                String.format(
                        "Friction: %.3f",
                        currentFriction
                );

        guiGraphics.drawCenteredString(
                this.font,
                valueStr,
                this.width / 2,
                sliderY + 24,
                0xCCCCCC
        );

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    private void updateFrictionFromMouse(
            double mouseX
    ) {

        double relativeX =
                mouseX - sliderX;

        double ratio =
                relativeX
                        / (double) sliderWidth;

        this.currentFriction =
                Math.clamp(
                        ratio,
                        0.0,
                        1.0
                );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        /*
         * The original right-click is already being held.
         * No second click is required.
         */
        if (button == 1) {

            isDragging = true;

            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {

        /*
         * We handle the actual mouse position ourselves.
         */
        if (isDragging && button == 1) {
            return true;
        }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {

        /*
         * Releasing right mouse saves the final friction.
         */
        if (button == 1 && isDragging) {

            isDragging = false;

            PacketDistributor.sendToServer(
                    new UpdateWheelFrictionPayload(
                            blockPos,
                            currentFriction
                    )
            );

            onClose();

            return true;
        }

        return super.mouseReleased(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}