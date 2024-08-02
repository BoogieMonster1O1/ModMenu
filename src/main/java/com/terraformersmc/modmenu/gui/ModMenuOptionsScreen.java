package com.terraformersmc.modmenu.gui;

import com.google.common.collect.ImmutableList;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.gui.widget.AbstractButtonWidget;
import com.terraformersmc.modmenu.gui.widget.ButtonListWidget;
import com.terraformersmc.modmenu.gui.widget.CyclingButtonWidget;
import com.terraformersmc.modmenu.util.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModMenuOptionsScreen extends AbstractScreen {
	private ButtonListWidget list;

	public ModMenuOptionsScreen(Screen previous) {
		super(previous);
	}

	@Override
	public void init() {
		super.init();

		this.list = new ButtonListWidget( this.client, this.width, this.height, 32, this.height - 32, 25 );
		this.list.addAll(ModMenuConfig.asOptions(), 998);
		this.addChild(this.list);
		this.buttons.add( new AbstractButtonWidget(
			999,
			this.width / 2 - 100,
			this.height - 27,
			200,
			20,
			ScreenTexts.DONE,
			( button ) -> {
				ModMenuConfigManager.save();
				this.client.setScreen( this.getPreviousScreen() );
			}
		) {
		} );
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		this.list.mouseClicked( mouseX, mouseY, button );
		super.mouseClicked( mouseX, mouseY, button );
	}

	@Override
	public void render( int mouseX, int mouseY, float delta ) {
		this.renderBackground();
		super.render(mouseX, mouseY, delta);
		drawCenteredString(this.textRenderer, new TranslatableText("modmenu.options").asFormattedString(), this.width / 2, 5, 0xffffff);
		List<String> list = getHoveredButtonTooltip(this.list, mouseX, mouseY);
		if (list != null)
			this.renderTooltip(list, mouseX, mouseY);
	}

	@Override
	public void removed() {
		ModMenuConfigManager.save();
	}

	public static List<String> getHoveredButtonTooltip(ButtonListWidget buttonList, int mouseX, int mouseY ) {
		Optional<AbstractButtonWidget> optional = buttonList.getHoveredButton( mouseX, mouseY );
		return optional.isPresent() && optional.get() instanceof CyclingButtonWidget
			? ( (CyclingButtonWidget<?>) optional.get() ).getTooltip()
			.stream()
			.map( Text::asFormattedString )
			.collect( Collectors.toList() )
			: ImmutableList.of();
	}
}
