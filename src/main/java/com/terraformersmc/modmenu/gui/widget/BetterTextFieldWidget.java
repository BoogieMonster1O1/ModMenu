package com.terraformersmc.modmenu.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class BetterTextFieldWidget extends TextFieldWidget implements Renderable {
	public BetterTextFieldWidget(int id, TextRenderer textRenderer, int x, int y, int width, int height ) {
		super( id, textRenderer, x, y, width, height );
	}

	@Override
	public void render( int mouseX, int mouseY, float delta ) {
		this.render();
	}
}
