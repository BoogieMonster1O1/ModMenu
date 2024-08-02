package com.terraformersmc.modmenu.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public final class ScreenTexts {
	public static final Text ON = new TranslatableText( "options.on" );
	public static final Text OFF = new TranslatableText( "options.off" );
	public static final Text DONE = new TranslatableText( "gui.done" );
	public static final Text LINE_BREAK = new LiteralText( "\n" );

	private ScreenTexts() { }

	public static Text composeGenericOptionText( Text text, Text value ) {
		return text.append(": ").append(value);
	}
}

