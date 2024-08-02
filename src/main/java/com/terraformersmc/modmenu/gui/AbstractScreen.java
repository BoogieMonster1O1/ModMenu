package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.gui.widget.Renderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.LabelWidget;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScreen extends Screen {
	private final List<Renderable> children = new ArrayList<>();
	private final Screen previous;

	public AbstractScreen( Screen previous ) {
		this.previous = previous;
	}

	@Override
	public void init() {
		super.init();
		this.children.clear();
	}

	protected void addChild(Renderable renderable ) {
		this.children.add( renderable );
	}

	@Override
	public void render( int mouseX, int mouseY, float tickDelta ) {
		this.renderChildren( mouseX, mouseY, tickDelta );
		this.renderButtons( mouseX, mouseY, tickDelta );
		this.renderLabels( mouseX, mouseY );
	}

	protected void renderChildren( int mouseX, int mouseY, float tickDelta ) {
		for ( Renderable widget : this.children )
			widget.render( mouseX, mouseY, tickDelta );
	}

	protected void renderButtons( int mouseX, int mouseY, float tickDelta ) {
		for ( ButtonWidget widget : this.buttons ) {
			widget.render( this.client, mouseX, mouseY );
			if ( widget.isMouseOver( this.client, mouseX, mouseY ) ) {
				widget.renderToolTip( mouseX, mouseY );
			}
		}
	}

	protected void renderLabels( int mouseX, int mouseY ) {
		for ( LabelWidget widget : this.labels )
			widget.render( this.client, mouseX, mouseY );
	}

	public Screen getPreviousScreen() {
		return this.previous;
	}
}
