package com.terraformersmc.modmenu.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.Texts;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class DrawingUtil {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static void drawRandomVersionBackground(
		Mod mod,
		int x,
		int y,
		int width,
		int height
	) {
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
		Random random = new Random(seed);
		int color = 0xFF000000 | MathHelper.hsvToRgb(random.nextFloat(), random.nextFloat() * 0.1f + 0.7f, 0.9f);
		if (!ModMenuConfig.RANDOM_JAVA_COLORS.getValue()) {
			color = 0xFFDD5656;
		}
		GlStateManager.color(1f, 1f, 1f, 1f);
		DrawableHelper.fill(x, y, x + width, y + height, color);
	}

	public static void drawWrappedString(
		String string,
		int x,
		int y,
		int wrapWidth,
		int lines,
		int color
	) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<Text> strings = Texts.wrapLines(new LiteralText(string), wrapWidth, MinecraftClient.getInstance().textRenderer, true, true);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			Text renderable = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				renderable = strings.get(i).append(new LiteralText("..."));
			}
			int x1 = x;
			if (CLIENT.textRenderer.isRightToLeft()) {
				int width = CLIENT.textRenderer.getStringWidth(renderable.asFormattedString());
				x1 += wrapWidth - width;
			}
			CLIENT.textRenderer.draw(renderable.asFormattedString(), x1, y + i * CLIENT.textRenderer.fontHeight, color);
		}
	}

	public static void drawBadge(
		int x,
		int y,
		int tagWidth,
		String text,
		int outlineColor,
		int fillColor,
		int textColor
	) {
		DrawableHelper.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
		DrawableHelper.fill(x, y, x + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		DrawableHelper.fill(x + 1,
			y + 1 + CLIENT.textRenderer.fontHeight - 1,
			x + tagWidth,
			y + CLIENT.textRenderer.fontHeight + 1,
			outlineColor
		);
		DrawableHelper.fill(x + tagWidth, y, x + tagWidth + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		DrawableHelper.fill(x + 1, y, x + tagWidth, y + CLIENT.textRenderer.fontHeight, fillColor);
		CLIENT.textRenderer.draw(
			text,
			(int) (x + 1 + (tagWidth - CLIENT.textRenderer.getStringWidth(text)) / (float) 2),
			y + 1,
			textColor,
			false
		);
	}
}
