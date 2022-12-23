package io.github.prospector.modmenu.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.input.Mouse;

import java.util.function.Consumer;

public class AbstractButtonWidget extends ButtonWidget {
	public static final TooltipSupplier EMPTY = ( button, mouseX, mouseY ) -> { };

	private final TooltipSupplier tooltipSupplier;
	private PressAction onPress;
	private Text text;

	public AbstractButtonWidget( int id, int x, int y, int width, int height, Text text, PressAction onPress ) {
		this( id, x, y, width, height, text, onPress, EMPTY );
	}

	public AbstractButtonWidget( int id, int x, int y, int width, int height, Text text, PressAction onPress, TooltipSupplier tooltipSupplier ) {
		super(
			id,
			x,
			y,
			width,
			height,
			"PLACEHOLDER"
		);
		this.onPress = onPress;
		this.tooltipSupplier = tooltipSupplier;
		this.text = text;
	}

	// renderButton
	@Override
	public void render( MinecraftClient client, int mouseX, int mouseY ) {
		if ( this.visible ) {
			TextRenderer textRenderer = client.textRenderer;
			client.getTextureManager().bindTexture( WIDGETS_LOCATION );
			GlStateManager.color4f( 1.0F, 1.0F, 1.0F, 1.0F );

			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			int yOffset = this.getYImage( this.hovered );
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate( 770, 771, 1, 0 );
			GlStateManager.blendFunc( 770, 771 );
			this.drawTexture( this.x, this.y, 0, 46 + yOffset * 20, this.width / 2, this.height );
			this.drawTexture( this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + yOffset * 20, this.width / 2, this.height );
			this.mouseDragged( client, mouseX, mouseY ); //renderBg is wrong name

			int color = 16777215;
			if ( !this.active )
				color = 10526880;
			else if ( this.isHovered() )
				color = 16777120;

			this.drawCenteredString( textRenderer, this.getMessage(), this.x + this.width / 2, this.y + ( this.height - 8 ) / 2, color );
		}
		if ( this.isHovered() )
			this.renderToolTip( mouseX, mouseY );
	}

	@Override
	public void renderToolTip( int mouseX, int mouseY ) {
		this.tooltipSupplier.onTooltip( this, mouseX, mouseY );
	}

	public void setHeight( int height ) {
		this.height = height;
	}

	public int getHeight() {
		return this.height;
	}

	@Override
	public void mouseReleased( int mouseX, int mouseY ) {
		if ( this.onPress != null )
			this.onPress.onPress( this );
	}

	public void mouseScrolled( int mouseX, int mouseY, int amount ) {
	}

	public void handleMouse() {
		if ( this.isMouseOver( MinecraftClient.getInstance(), this.x, this.y ) ) {
			// scrolling
			int scroll = Mouse.getEventDWheel();
			if ( scroll != 0 ) {
				this.mouseScrolled( Mouse.getX(), Mouse.getY(), scroll > 0 ? -1 : 1 );
			}
		}
	}

	public String getMessage() {
		return this.text.asFormattedString();
	}

	public void setMessage( Text text ) {
		this.text = text;
	}

	protected void setOnPress( PressAction action ) {
		this.onPress = action;
	}

	public void setVisible( boolean visible ) {
		this.visible = visible;
	}

	@FunctionalInterface
	public interface PressAction {
		void onPress( AbstractButtonWidget button );
	}

	@FunctionalInterface
	public interface TooltipSupplier {
		void onTooltip( AbstractButtonWidget button, int mouseX, int mouseY );

		default void supply( Consumer<Text> consumer ) {
		}
	}
}
