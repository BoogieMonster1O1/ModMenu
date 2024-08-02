package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.DrawingUtil;
import net.minecraft.client.MinecraftClient;

import java.util.Set;

public class ModBadgeRenderer {
	protected final int startX, startY, badgeMax;
	protected int badgeX, badgeY;
	protected final Mod mod;
	protected final MinecraftClient client;
	protected final ModsScreen screen;

	public ModBadgeRenderer(int startX, int startY, int endX, Mod mod, ModsScreen screen) {
		this.startX = startX;
		this.startY = startY;
		this.badgeMax = endX;
		this.mod = mod;
		this.screen = screen;
		this.client = MinecraftClient.getInstance();
	}

	public void draw(int mouseX, int mouseY) {
		this.badgeX = startX;
		this.badgeY = startY;
		Set<Mod.Badge> badges = mod.getBadges();
		badges.forEach(badge -> drawBadge(badge, mouseX, mouseY));
	}

	public void drawBadge(Mod.Badge badge, int mouseX, int mouseY) {
		this.drawBadge(
			badge.getText().asFormattedString(),
			badge.getOutlineColor(),
			badge.getFillColor(),
			mouseX,
			mouseY
		);
	}

	public void drawBadge(
		String text,
		int outlineColor,
		int fillColor,
		int mouseX,
		int mouseY
	) {
		int width = client.textRenderer.getStringWidth(text) + 6;
		if (badgeX + width < badgeMax) {
			DrawingUtil.drawBadge(badgeX, badgeY, width, text, outlineColor, fillColor, 0xCACACA);
			badgeX += width + 3;
		}
	}

	public Mod getMod() {
		return mod;
	}
}
