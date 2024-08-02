package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.platform.GlStateManager;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.BetterEntryListWidget;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModListEntry extends BetterEntryListWidget.Entry<ModListEntry> {
	public static final Identifier UNKNOWN_ICON = new Identifier("textures/misc/unknown_pack.png");
	private static final Identifier MOD_CONFIGURATION_ICON = new Identifier(ModMenu.MOD_ID,
		"textures/gui/mod_configuration.png"
	);
	private static final Identifier ERROR_ICON = new Identifier("world_list/error");
	private static final Identifier ERROR_HIGHLIGHTED_ICON = new Identifier("world_list/error_highlighted");

	protected final MinecraftClient client;
	public final Mod mod;
	protected final ModListWidget list;
	protected Identifier iconLocation;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;
	protected long sinceLastClick;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = MinecraftClient.getInstance();
	}

	@Override
	public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		this.render(index, x, y, entryWidth, entryHeight, mouseX, mouseY, hovered);
	}

	@Override
	public void render(int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered) {
		x += getXOffset();
		rowWidth -= getXOffset();
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		String modId = mod.getId();
		if ("java".equals(modId)) {
			DrawingUtil.drawRandomVersionBackground(mod, x, y, iconSize, iconSize);
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		this.client.getTextureManager().bindTexture(this.getIconTexture());
		DrawableHelper.drawTexture(x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
		GlStateManager.disableBlend();
		Text name = new LiteralText(mod.getTranslatedName());
		String trimmedName = name.asFormattedString();
		int maxNameWidth = rowWidth - iconSize - 3;
		TextRenderer font = this.client.textRenderer;
		if (font.getStringWidth(name.asFormattedString()) > maxNameWidth) {
			trimmedName = font.trimToWidth(name.asFormattedString(), maxNameWidth - font.getStringWidth("...")) + "...";
		}
		font.draw(
			(trimmedName), //Language.getInstance().reorder
			x + iconSize + 3,
			y + 1,
			0xFFFFFF,
			true
		);
		int updateBadgeXOffset = 0;
		if (!ModMenuConfig.HIDE_BADGES.getValue()) {
			new ModBadgeRenderer(x + iconSize + 3 + font.getStringWidth(name.asFormattedString()) + 2 + updateBadgeXOffset,
				y,
				x + rowWidth,
				mod,
				list.getParent()
			).draw(mouseX, mouseY);
		}
		if (!ModMenuConfig.COMPACT_LIST.getValue()) {
			String summary = mod.getSummary();
			DrawingUtil.drawWrappedString(
				summary,
				(x + iconSize + 3 + 4),
				(y + client.textRenderer.fontHeight + 2),
				rowWidth - iconSize - 7,
				2,
				0x808080
			);
		} else {
			DrawingUtil.drawWrappedString(
				mod.getPrefixedVersion(),
				(x + iconSize + 3),
				(y + client.textRenderer.fontHeight + 2),
				rowWidth - iconSize - 7,
				2,
				0x808080
			);
		}

		if (!(this instanceof ParentEntry) && ModMenuConfig.QUICK_CONFIGURE.getValue() && (this.list.getParent()
			.getModHasConfigScreen()
			.get(modId) || this.list.getParent().modScreenErrors.containsKey(modId))) {
			final int textureSize = ModMenuConfig.COMPACT_LIST.getValue() ?
				(int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) :
				256;
			if (this.client.options.touchscreen || hovered) {
				DrawableHelper.fill(x, y, x + iconSize, y + iconSize, -1601138544);
				boolean hoveringIcon = mouseX - x < iconSize;
				if (this.list.getParent().modScreenErrors.containsKey(modId)) {
					MinecraftClient.getInstance().getTextureManager().bindTexture(hoveringIcon ? ERROR_HIGHLIGHTED_ICON : ERROR_ICON);
					DrawableHelper.drawTexture(
						x,
						y,
						0,
						0,
						iconSize,
						iconSize,
						iconSize,
						iconSize
					);
					if (hoveringIcon) {
						Throwable e = this.list.getParent().modScreenErrors.get(modId);
						//this.list.getParent().setTooltip(this.client.textRenderer.wrapLines(ModMenuScreenTexts.configureError(modId, e).asFormattedString(), 175));
					}
				} else {
					int v = hoveringIcon ? iconSize : 0;
					MinecraftClient.getInstance().getTextureManager().bindTexture(MOD_CONFIGURATION_ICON);
					DrawableHelper.drawTexture(
						x,
						y,
						0.0F,
						(float) v,
						iconSize,
						iconSize,
						textureSize,
						textureSize
					);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
		list.select(this);
		if (ModMenuConfig.QUICK_CONFIGURE.getValue() && this.list.getParent()
			.getModHasConfigScreen()
			.get(this.mod.getId())) {
			int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
			if (mouseX - list.getRowLeft() <= iconSize) {
				this.openConfig();
			} else if (System.currentTimeMillis() - this.sinceLastClick < 250) {
				this.openConfig();
			}
		}
		this.sinceLastClick = System.currentTimeMillis();
		return true;
	}

	public void openConfig() {
		MinecraftClient.getInstance().setScreen(ModMenu.getConfigScreen(mod.getId(), list.getParent()));
	}

	public Mod getMod() {
		return mod;
	}

	public Identifier getIconTexture() {
		if (this.iconLocation == null) {
			this.iconLocation = new Identifier(ModMenu.MOD_ID, mod.getId() + "_icon");
			NativeImageBackedTexture icon = mod.getIcon(list.getFabricIconHandler(),
				64 * this.client.options.guiScale
			);
			if (icon != null) {
				this.client.getTextureManager().loadTexture(this.iconLocation, icon);
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		return iconLocation;
	}

	public int getXOffset() {
		return 0;
	}

	@Override
	public void updatePosition(int index, int x, int y) {}

	@Override
	public void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y) {}
}
