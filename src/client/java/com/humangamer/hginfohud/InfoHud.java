package com.humangamer.hginfohud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class InfoHud {

    private final MinecraftClient client;
    private final TextRenderer textRenderer;
    @Nullable
    private ChunkPos pos;
    @Nullable
    private WorldChunk chunk;
    private int windowWidth;
    private boolean showInfoHud;

    public InfoHud(MinecraftClient client) {
        this.client = client;
        this.textRenderer = client.textRenderer;
        this.showInfoHud = false;
        HGInfoHudMod.infoHud = this;
    }

    @SuppressWarnings("deprecation")
    public void render(DrawContext context) {
        this.client.getProfiler().push("infoHud");

        context.draw(() -> {
            drawInfo(context);
        });

        this.client.getProfiler().pop();
    }

    private void drawInfo(DrawContext context)
    {
        if (this.client.getCameraEntity() == null) {
            return;
        }

        int spaceWidth = textRenderer.getWidth(" ");

        int padding = 5;
        int outerPadding = 2;
        int width = Math.max(
                Math.max(measurePositionWidth(context, spaceWidth * 2), measureDirectionWidth(context)),
                measureBiomeWidth(context)
        ) + padding * 2;
        int x = context.getScaledWindowWidth() - outerPadding - width;
        int y = outerPadding;

        windowWidth = width;


        int height = padding * 4 + textRenderer.fontHeight * 3;

        drawBackground(context, context.getScaledWindowWidth() - outerPadding - width, outerPadding, width, height);

        // == Position ==
        drawPosition(context, x, y + padding, spaceWidth * 2, 0xFF00FF00, false);

        // == Direction ==
        drawDirection(context, x, y + padding * 2 + textRenderer.fontHeight, 0xFFFFFF00, false);

        // == Biome ==
        drawBiome(context, x, y + padding * 3 + textRenderer.fontHeight * 2, 0xFF55FFFF, false);
    }

    private void drawPosition(DrawContext context, int x, int y, int padding, int color, boolean shadow)
    {
        Entity entity = this.client.getCameraEntity();
        if (entity == null) {
            return;
        }

        String xText = String.format(Locale.ROOT, "%.0f", entity.getX());
        String yText = String.format(Locale.ROOT, "%.0f", entity.getY());
        String zText = String.format(Locale.ROOT, "%.0f", entity.getZ());

        // Center the text
        int fullWidth = padding + textRenderer.getWidth("X: " + xText + " Y: " + yText + " Z: " + zText);
        int newX = x + (windowWidth - fullWidth) / 2;

        int off = drawComponent(context, "X", xText, newX, y, false, color, shadow);
        off += drawComponent(context, "Y", yText, newX + padding + off, y,  false, color, shadow);
        off += drawComponent(context, "Z", zText, newX + padding * 2 + off, y,  false, color, shadow);
    }

    private int measurePositionWidth(DrawContext context, int padding)
    {
        Entity entity = this.client.getCameraEntity();
        if (entity == null) {
            return 0;
        }

        String xText = String.format(Locale.ROOT, "%.0f", entity.getX());
        String yText = String.format(Locale.ROOT, "%.0f", entity.getY());
        String zText = String.format(Locale.ROOT, "%.0f", entity.getZ());

        return padding * 2 + textRenderer.getWidth("X: " + xText + " Y: " + yText + " Z: " + zText);
    }

    private void drawDirection(DrawContext context, int x, int y, int color, boolean shadow)
    {
        String dirString = getDirectionText();

        drawComponent(context, "Direction", dirString, x, y, color, shadow);
    }

    private int measureDirectionWidth(DrawContext context)
    {
        String dirString = getDirectionText();

        return textRenderer.getWidth("Direction: " + dirString);
    }

    private void drawBiome(DrawContext context, int x, int y, int color, boolean shadow)
    {
        String biomeText = getBiomeText();
        drawComponent(context, "Biome", biomeText, x, y, color, shadow);
    }

    private int measureBiomeWidth(DrawContext context)
    {
        String biomeText = getBiomeText();
        return textRenderer.getWidth("Biome: " + biomeText);
    }

    private int drawComponent(DrawContext context, String label, String value, int x, int y, int color, boolean shadow)
    {
        return this.drawComponent(context, label, value, x, y, true, color, shadow);
    }

    private int drawComponent(DrawContext context, String label, String value, int x, int y, boolean centerText, int color, boolean shadow)
    {
        String labelText = label + ": ";
        int xOffset = x;
        int yOffset = y;

        if (centerText) {
            xOffset += (windowWidth - textRenderer.getWidth(labelText + value)) / 2;
        }

        context.drawText(this.textRenderer, labelText, xOffset, yOffset, 0xFFE0E0E0, shadow);
        xOffset += this.textRenderer.getWidth(labelText);
        context.drawText(this.textRenderer, value, xOffset, yOffset, color, shadow);

        return this.textRenderer.getWidth(labelText + value);
    }

    private void drawBackground(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, 0x90505050);
    }

    private String getDirectionText()
    {
        Entity entity = this.client.getCameraEntity();
        if (entity == null)
        {
            return "Invalid";
        }
        Direction direction = entity.getHorizontalFacing();
        return switch (direction) {
            case NORTH -> "North (-Z)";
            case SOUTH -> "South (+Z)";
            case WEST -> "West (-X)";
            case EAST -> "East (+X)";
            default -> "Invalid";
        };
    }

    private String getBiomeText()
    {
        Entity entity = this.client.getCameraEntity();
        if (entity == null)
        {
            return "Invalid";
        }
        BlockPos blockPos = entity.getBlockPos();
        ChunkPos chunkPos = new ChunkPos(blockPos);
        if (!Objects.equals(this.pos, chunkPos))
        {
            this.pos = chunkPos;
            this.resetChunk();
        }
        WorldChunk worldChunk = this.getClientChunk();
        if (worldChunk.isEmpty())
        {
            return "Waiting for chunk...";
        }
        else {
            if (blockPos.getY() >= this.client.world.getBottomY() && blockPos.getY() <= this.client.world.getTopY())
            {
                RegistryEntry<Biome> biome = this.client.world.getBiome(blockPos);
                return this.getBiomeString(biome);
            }
            else
            {
                return "Out of world";
            }
        }
    }

    private String getBiomeString(RegistryEntry<Biome> biome)
    {
        Identifier identifier = ((RegistryEntry.Reference<Biome>) biome).registryKey().getValue();
        String name = identifier.toTranslationKey("biome");

        String biomeName;
        if( Language.getInstance().hasTranslation(name))
            biomeName = Text.translatable((String)name).getString();
        else
            biomeName = identifier.toString();

        return biomeName;
    }

    private WorldChunk getClientChunk()
    {
        if (this.chunk == null)
        {
            this.chunk = this.client.world.getChunk(this.pos.x, this.pos.z);
        }
        return this.chunk;
    }

    private void resetChunk()
    {
        this.chunk = null;
    }

    public void clear() {
        this.showInfoHud = false;
    }

    public boolean shouldShowInfoHud()
    {
        return this.showInfoHud && !this.client.options.hudHidden && !this.client.getDebugHud().shouldShowDebugHud();
    }

    public void toggleInfoHud() {
        this.showInfoHud = !this.showInfoHud;
    }
}
