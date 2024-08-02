package com.terraformersmc.modmenu.util;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public final class ModMenuScreenTexts {
	public static final Text CONFIGURE = new TranslatableText("modmenu.configure");
	public static final Text DROP_CONFIRM = new TranslatableText("modmenu.dropConfirm");
	public static final Text DROP_INFO_LINE_1 = new TranslatableText("modmenu.dropInfo.line1");
	public static final Text DROP_INFO_LINE_2 = new TranslatableText("modmenu.dropInfo.line2");
	public static final Text DROP_SUCCESSFUL_LINE_1 = new TranslatableText("modmenu.dropSuccessful.line1");
	public static final Text DROP_SUCCESSFUL_LINE_2 = new TranslatableText("modmenu.dropSuccessful.line2");
	public static final Text ISSUES = new TranslatableText("modmenu.issues");
	public static final Text MODS_FOLDER = new TranslatableText("modmenu.modsFolder");
	public static final Text SEARCH = new TranslatableText("modmenu.search");
	public static final Text TITLE = new TranslatableText("modmenu.title");
	public static final Text TOGGLE_FILTER_OPTIONS = new TranslatableText("modmenu.toggleFilterOptions");
	public static final Text WEBSITE = new TranslatableText("modmenu.website");

	private ModMenuScreenTexts() {
	}

	public static Text modIdTooltip(String modId) {
		return new TranslatableText("modmenu.modIdToolTip", modId);
	}

	public static Text configureError(String modId, Throwable e) {
		return new TranslatableText("modmenu.configure.error", modId, modId)
			.append(ScreenTexts.LINE_BREAK)
			.append(ScreenTexts.LINE_BREAK)
			.append(e.toString())
			.setStyle(new Style().setFormatting(Formatting.RED));
	}
}
