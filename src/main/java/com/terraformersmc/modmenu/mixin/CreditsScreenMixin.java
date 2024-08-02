package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreditsScreen.class)
public class CreditsScreenMixin extends Screen {

	@Inject(method = "close", at = @At("HEAD"), cancellable = true)
	public void close(CallbackInfo ci) {
		if (this.client.player == null) {
			this.client.setScreen(new ModsScreen(null));
			ci.cancel();
		}
	}

}
