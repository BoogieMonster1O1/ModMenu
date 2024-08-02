package com.terraformersmc.modmenu.config.option;

import com.terraformersmc.modmenu.gui.widget.AbstractButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public abstract class Option {
	private final Text key;

	public Option(String key ) {
		this.key = new TranslatableText( key );
	}

	public abstract AbstractButtonWidget createButton(GameOptions options, int id, int x, int y, int width );

	protected Text getDisplayPrefix() {
		return this.key;
	}

	protected Text getGenericLabel( Text value ) {
		return this.getDisplayPrefix().append(": ").append(value);
	}
}
