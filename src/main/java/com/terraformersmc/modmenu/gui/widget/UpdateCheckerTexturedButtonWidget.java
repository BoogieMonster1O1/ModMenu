package com.terraformersmc.modmenu.gui.widget;

import net.minecraft.util.Identifier;

public class UpdateCheckerTexturedButtonWidget extends LegacyTexturedButtonWidget {
	public UpdateCheckerTexturedButtonWidget( int id, int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, String message, PressAction onPress, TooltipSupplier tooltipSupplier ) {
		super(id, x, y, width, height, u, v, texture, uWidth, vHeight, message, onPress, tooltipSupplier);
	}
}
