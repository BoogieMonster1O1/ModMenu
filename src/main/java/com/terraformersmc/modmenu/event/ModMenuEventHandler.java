package com.terraformersmc.modmenu.event;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.AbstractButtonWidget;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import com.terraformersmc.modmenu.util.Screens;
import net.legacyfabric.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.legacyfabric.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModMenuEventHandler {
	public static final Identifier MODS_BUTTON_TEXTURE = new Identifier(ModMenu.MOD_ID, "textures/gui/mods_button.png");
	private static KeyBinding MENU_KEY_BIND;

	public static void register() {
		MENU_KEY_BIND = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.modmenu.open_menu",
			-1,
			"key.categories.misc"
		));
		ClientTickEvents.END_CLIENT_TICK.register(ModMenuEventHandler::onClientEndTick);

		//ScreenEvents.AFTER_INIT.register(ModMenuEventHandler::afterScreenInit);
	}

	public static void afterScreenInit(Screen screen) {
		if (screen instanceof TitleScreen) {
			afterTitleScreenInit(screen);
		}
	}

	private static void afterTitleScreenInit(Screen screen) {
		final List<ButtonWidget> buttons = Screens.getButtons(screen);
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue()) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = screen.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				ButtonWidget widget = buttons.get(i);
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
					if (widget.visible) {
						shiftButtons(widget, modsButtonIndex == -1, spacing);
						if (modsButtonIndex == -1) {
							buttonsY = widget.y;
						}
					}
				}
				if (buttonHasText(widget, "menu.online")) {
					if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS) {
						buttons.set(i, new ModMenuButtonWidget(
							993,
							widget.x,
							widget.y,
							widget.getWidth(),
							20,
							ModMenuApi.createModsButtonText(),
							screen
						));
					} else {
						if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
							widget.setWidth(98);
						}
						modsButtonIndex = i + 1;
						if (widget.visible) {
							buttonsY = widget.y;
						}
					}
				}

			}
			if (modsButtonIndex != -1) {
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(
						994,
						screen.width / 2 - 100,
						buttonsY + spacing,
						200,
						20,
						ModMenuApi.createModsButtonText(),
						screen
					));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
					buttons.add(modsButtonIndex,
						new ModMenuButtonWidget(
							995,
							screen.width / 2 + 2,
							buttonsY,
							98,
							20,
							ModMenuApi.createModsButtonText(),
							screen
						)
					);
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.ICON) {
					buttons.add(modsButtonIndex, new UpdateCheckerTexturedButtonWidget(996,
						screen.width / 2 + 104,
						buttonsY,
						20,
						20,
						0,
						0,
						MODS_BUTTON_TEXTURE,
						32,
						64,
						ModMenuApi.createModsButtonText().asFormattedString(),
						button -> MinecraftClient.getInstance().setScreen(new ModsScreen(screen)),
						AbstractButtonWidget.EMPTY
					));
				}
			}
		}
	}

	private static void onClientEndTick(MinecraftClient client) {
		while (MENU_KEY_BIND.wasPressed()) {
			client.setScreen(new ModsScreen(client.currentScreen));
		}
	}

	public static boolean buttonHasText(ButtonWidget widget, String... translationKeys) {
		String text = widget.message;
		return Arrays.asList(translationKeys).contains(text) || Arrays.stream(translationKeys).map(I18n::translate).collect(Collectors.toList()).contains(text);
	}

	public static void shiftButtons(ButtonWidget widget, boolean shiftUp, int spacing) {
		if (shiftUp) {
			widget.y = (widget.y - spacing / 2);
		} else if (widget.message.equals("title.credits")) {
			widget.y = (widget.y + spacing / 2);
		}
	}
}
