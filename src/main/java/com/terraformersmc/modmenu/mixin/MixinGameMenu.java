package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.AbstractButtonWidget;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameMenuScreen.class)
public abstract class MixinGameMenu extends Screen {

	@Inject(method = "init", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	public void onInit(CallbackInfo ci) {
		final int spacing = 24;
		int buttonsY = this.height / 4 + 8;
		ModMenuConfig.GameMenuButtonStyle style = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
		int vanillaButtonsY = this.height / 4 + 72 - 16 + 1;
		final int fullWidthButton = 204;

		if (style == ModMenuConfig.GameMenuButtonStyle.INSERT) {
			buttons.add(new ModMenuButtonWidget(
				8,
				this.width / 2 - 102,
				buttonsY + spacing,
				fullWidthButton,
				20,
				ModMenuApi.createModsButtonText(),
				this
			));
		} else if (style == ModMenuConfig.GameMenuButtonStyle.ICON) {
			buttons.add(new UpdateCheckerTexturedButtonWidget(10,
				this.width / 2 + 4 + 100 + 2,
				vanillaButtonsY,
				20,
				20,
				0,
				0,
				ModMenuEventHandler.MODS_BUTTON_TEXTURE,
				32,
				64,
				ModMenuApi.createModsButtonText().asFormattedString(),
				button -> MinecraftClient.getInstance().setScreen(new ModsScreen(this)),
				AbstractButtonWidget.EMPTY
			));
		} else {
			buttons.add(new ModMenuButtonWidget(
				8,
				this.width / 2 - 102,
				buttonsY + spacing,
				fullWidthButton,
				20,
				ModMenuApi.createModsButtonText(),
				this
			));
		}
	}

	@Inject(method = "buttonClicked", at = @At("HEAD"), cancellable = true)
	private void onButtonClicked(ButtonWidget button, CallbackInfo ci) {
		if (button.id == 8) {
			this.client.setScreen(new ModsScreen(this));
			ci.cancel();
		}
	}
}
