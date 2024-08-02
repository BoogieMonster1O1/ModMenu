package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class LegacyTexturedButtonWidget extends AbstractButtonWidget {
	private final Identifier texture;
	private final int u;
	private final int v;
	private final int uWidth;
	private final int vHeight;

	protected LegacyTexturedButtonWidget( int id, int x, int y, int width, int height, int u, int v, Identifier texture, PressAction onPress ) {
		this( id, x, y, width, height, u, v, texture, 256, 256, onPress );
	}

	protected LegacyTexturedButtonWidget( int id, int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress ) {
		this( id, x, y, width, height, u, v, texture, uWidth, vHeight, "", onPress, EMPTY );
	}

	public LegacyTexturedButtonWidget( int id, int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, String message, PressAction onPress, TooltipSupplier tooltipSupplier ) {
		super( id, x, y, width, height, new LiteralText( message ), onPress, tooltipSupplier );
		this.uWidth = uWidth;
		this.vHeight = vHeight;
		this.u = u;
		this.v = v;
		this.texture = texture;
	}

	protected void setPos( int x, int y ) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void render(MinecraftClient client, int mouseX, int mouseY ) {
		GlStateManager.color( 1, 1, 1, 1f );
		client.getTextureManager().bindTexture( this.texture );
		GlStateManager.disableDepthTest();
		int adjustedV = this.v;
		if ( !this.active )
			adjustedV += this.height * 2;
		else if ( this.isMouseOver( client, mouseX, mouseY ) )
			adjustedV += this.height;

		drawTexture( this.x, this.y, this.u, adjustedV, this.width, this.height, this.uWidth, this.vHeight );
		GlStateManager.enableDepthTest();

		if ( this.isMouseOver( client, mouseX, mouseY ) )
			this.renderToolTip( mouseX, mouseY );
	}

	public boolean isJustHovered() {
		return this.hovered;
	}
}
