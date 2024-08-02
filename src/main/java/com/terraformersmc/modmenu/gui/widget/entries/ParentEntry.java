package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.platform.GlStateManager;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModSearch;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ParentEntry extends ModListEntry {
	private static final Identifier PARENT_MOD_TEXTURE = new Identifier(ModMenu.MOD_ID, "textures/gui/parent_mod.png");
	protected List<Mod> children;
	protected final ModListWidget list;
	protected boolean hoveringIcon = false;

	public ParentEntry(Mod parent, List<Mod> children, ModListWidget list) {
		super(parent, list);
		this.children = children;
		this.list = list;
	}

	@Override
	public void render(
		int index,
		int y,
		int x,
		int rowWidth,
		int rowHeight,
		int mouseX,
		int mouseY,
		boolean isSelected,
		float delta
	) {
		super.render(index, y, x, rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
		TextRenderer font = client.textRenderer;
		int childrenBadgeHeight = font.fontHeight;
		int childrenBadgeWidth = font.fontHeight;
		int shownChildren = ModSearch.search(list.getParent(), list.getParent().getSearchInput(), getChildren()).size();
		Text str = shownChildren == children.size() ?
			new LiteralText(String.valueOf(shownChildren)) :
			new LiteralText(shownChildren + "/" + children.size());
		int childrenWidth = font.getStringWidth(str.asFormattedString()) - 1;
		if (childrenBadgeWidth < childrenWidth + 4) {
			childrenBadgeWidth = childrenWidth + 4;
		}
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		int childrenBadgeX = x + iconSize - childrenBadgeWidth;
		int childrenBadgeY = y + iconSize - childrenBadgeHeight;
		int childrenOutlineColor = 0xff107454;
		int childrenFillColor = 0xff093929;
		DrawableHelper.fill(childrenBadgeX + 1,
			childrenBadgeY,
			childrenBadgeX + childrenBadgeWidth - 1,
			childrenBadgeY + 1,
			childrenOutlineColor
		);
		DrawableHelper.fill(childrenBadgeX,
			childrenBadgeY + 1,
			childrenBadgeX + 1,
			childrenBadgeY + childrenBadgeHeight - 1,
			childrenOutlineColor
		);
		DrawableHelper.fill(childrenBadgeX + childrenBadgeWidth - 1,
			childrenBadgeY + 1,
			childrenBadgeX + childrenBadgeWidth,
			childrenBadgeY + childrenBadgeHeight - 1,
			childrenOutlineColor
		);
		DrawableHelper.fill(childrenBadgeX + 1,
			childrenBadgeY + 1,
			childrenBadgeX + childrenBadgeWidth - 1,
			childrenBadgeY + childrenBadgeHeight - 1,
			childrenFillColor
		);
		DrawableHelper.fill(childrenBadgeX + 1,
			childrenBadgeY + childrenBadgeHeight - 1,
			childrenBadgeX + childrenBadgeWidth - 1,
			childrenBadgeY + childrenBadgeHeight,
			childrenOutlineColor
		);
		font.draw(
			str.asFormattedString(),
			(int) (childrenBadgeX + (float) childrenBadgeWidth / 2 - (float) childrenWidth / 2),
			childrenBadgeY + 1,
			0xCACACA,
			false
		);
		this.hoveringIcon = mouseX >= x - 1 && mouseX <= x - 1 + iconSize && mouseY >= y - 1 && mouseY <= y - 1 + iconSize;
		if (isMouseOver(mouseX, mouseY)) {
			DrawableHelper.fill(x, y, x + iconSize, y + iconSize, 0xA0909090);
			int xOffset = list.getParent().showModChildren.contains(getMod().getId()) ? iconSize : 0;
			int yOffset = hoveringIcon ? iconSize : 0;
			this.client.getTextureManager().bindTexture(PARENT_MOD_TEXTURE );
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			DrawableHelper.drawTexture(
				x,
				y,
				xOffset,
				yOffset,
				iconSize + xOffset,
				iconSize + yOffset,
				ModMenuConfig.COMPACT_LIST.getValue() ?
					(int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) :
					256,
				ModMenuConfig.COMPACT_LIST.getValue() ?
					(int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) :
					256
			);
		}
	}

	@Override
	public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		boolean quickConfigure = ModMenuConfig.QUICK_CONFIGURE.getValue();
		if (mouseX - list.getRowLeft() <= iconSize) {
			this.toggleChildren();
			return true;
		} else if (!quickConfigure && System.currentTimeMillis() - this.sinceLastClick < 250) {
			this.toggleChildren();
			return true;
		} else {
			return super.mouseClicked(index, mouseX, mouseY, button, x, y);
		}
	}

	private void toggleChildren() {
		String id = getMod().getId();
		if (list.getParent().showModChildren.contains(id)) {
			list.getParent().showModChildren.remove(id);
		} else {
			list.getParent().showModChildren.add(id);
		}
		list.filter(list.getParent().getSearchInput(), false);
	}

	public void setChildren(List<Mod> children) {
		this.children = children;
	}

	public void addChildren(List<Mod> children) {
		this.children.addAll(children);
	}

	public void addChildren(Mod... children) {
		this.children.addAll(Arrays.asList(children));
	}

	public List<Mod> getChildren() {
		return children;
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return Objects.equals(this.list.getEntryAtPos(mouseX, mouseY), this);
	}
}
