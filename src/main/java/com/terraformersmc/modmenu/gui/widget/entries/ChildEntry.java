package com.terraformersmc.modmenu.gui.widget.entries;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.gui.DrawableHelper;

public class ChildEntry extends ModListEntry {
	private final boolean bottomChild;

	public ChildEntry(Mod mod, ModListWidget list, boolean bottomChild) {
		super(mod, list);
		this.bottomChild = bottomChild;
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
		x += 4;
		int color = 0xFFA0A0A0;
		DrawableHelper.fill(x, y - 2, x + 1, y + (bottomChild ? rowHeight / 2 : rowHeight + 2), color);
		DrawableHelper.fill(x, y + rowHeight / 2, x + 7, y + rowHeight / 2 + 1, color);
	}

	/*@Override
	public boolean keyPressed(char char, int keyCode) {
		if (keyCode == GLFW.GLFW_KEY_LEFT) {
			list.setSelected(parent);
			list.ensureVisible(parent);
			return true;
		}
		return false;
	}*/

	@Override
	public int getXOffset() {
		return 13;
	}
}
