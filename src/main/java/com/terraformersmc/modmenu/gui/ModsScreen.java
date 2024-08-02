package com.terraformersmc.modmenu.gui;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.platform.GlStateManager;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.gui.widget.*;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.*;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.PagedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ModsScreen extends AbstractScreen {
	private static final Identifier FILTERS_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID,
		"textures/gui/filters_button.png"
	);
	private static final Identifier CONFIGURE_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID,
		"textures/gui/configure_button.png"
	);

	private static final Logger LOGGER = LogManager.getLogger("Mod Menu | ModsScreen");
	private ModListEntry selected;
	private ModBadgeRenderer modBadgeRenderer;
	private boolean keepFilterOptionsShown = false;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private static final int RIGHT_PANE_Y = 48;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();

	private BetterTextFieldWidget searchBox;
	private @Nullable AbstractButtonWidget filtersButton;
	private AbstractButtonWidget sortingButton;
	private AbstractButtonWidget librariesButton;
	private ModListWidget modList;
	private @Nullable AbstractButtonWidget configureButton;
	private AbstractButtonWidget websiteButton;
	private AbstractButtonWidget issuesButton;
	private DescriptionListWidget descriptionListWidget;

	public final Map<String, Boolean> modHasConfigScreen = new HashMap<>();
	public final Map<String, Throwable> modScreenErrors = new HashMap<>();

	private static final Text SEND_FEEDBACK_TEXT = new TranslatableText("modmenu.sendFeedback");
	private static final Text REPORT_BUGS_TEXT = new TranslatableText("modmenu.reportBugs");

	public ModsScreen(Screen previousScreen) {
		super(previousScreen);
	}

	@Override
	public void handleMouse() {
		super.handleMouse();
		this.modList.handleMouse();
		this.descriptionListWidget.handleMouse();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		this.modList.mouseClicked(mouseX, mouseY, button);
		this.searchBox.mouseClicked(mouseX, mouseY, button);
		this.descriptionListWidget.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void init() {
		super.init();

		for (Mod mod : ModMenu.MODS.values()) {
			String id = mod.getId();
			if (!modHasConfigScreen.containsKey(id)) {
				try {
					Screen configScreen = ModMenu.getConfigScreen(id, this);
					modHasConfigScreen.put(id, configScreen != null);
				} catch (NoClassDefFoundError e) {
					LOGGER.warn(
						"The '" + id + "' mod config screen is not available because " + e.getLocalizedMessage() +
							" is missing.");
					modScreenErrors.put(id, e);
					modHasConfigScreen.put(id, false);
				} catch (Throwable e) {
					LOGGER.error("Error from mod '" + id + "'", e);
					modScreenErrors.put(id, e);
					modHasConfigScreen.put(id, false);
				}
			}
		}

		int paneY = ModMenuConfig.CONFIG_MODE.getValue() ? 48 : 48 + 19;
		this.paneWidth = this.width / 2 - 8;
		this.rightPaneX = this.width - this.paneWidth;

		// Mod list (initialized early for updateFiltersX)
		this.modList = new ModListWidget(this.client,
			this.paneWidth,
			this.height,
			paneY,
			this.height - 36,
			ModMenuConfig.COMPACT_LIST.getValue() ? 23 : 36,
			this.modList,
			this
		);
		this.modList.setXPos(0);

		// Search box
		int filtersButtonSize = (ModMenuConfig.CONFIG_MODE.getValue() ? 0 : 22);
		int searchWidthMax = this.paneWidth - 32 - filtersButtonSize;
		int searchBoxWidth = ModMenuConfig.CONFIG_MODE.getValue() ? Math.min(200, searchWidthMax) : searchWidthMax;

		this.searchBoxX = this.paneWidth / 2 - searchBoxWidth / 2 - filtersButtonSize / 2;

		this.searchBox = new BetterTextFieldWidget(0, this.textRenderer,
			this.searchBoxX,
			22,
			searchBoxWidth,
			20
		);
		//this.searchBox.setText(ModMenuScreenTexts.SEARCH.asFormattedString());
		this.searchBox.setListener(new PagedEntryListWidget.Listener() {
			@Override
			public void setBooleanValue(int id, boolean value) {}

			@Override
			public void setFloatValue(int id, float value) {}

			@Override
			public void setStringValue(int id, String text) {
				modList.filter(text, false);
			}
		});

		// Filters button
		Text sortingText = ModMenuConfig.SORTING.getButtonText();
		Text librariesText = ModMenuConfig.SHOW_LIBRARIES.getButtonText();

		int sortingWidth = textRenderer.getStringWidth(sortingText.asFormattedString()) + 20;
		int librariesWidth = textRenderer.getStringWidth(librariesText.asFormattedString()) + 20;

		this.filtersWidth = librariesWidth + sortingWidth + 2;
		this.searchRowWidth = this.searchBoxX + searchBoxWidth + 22;

		this.updateFiltersX();

		if (!ModMenuConfig.CONFIG_MODE.getValue()) {
			this.filtersButton = new LegacyTexturedButtonWidget(2, this.paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22,
				20, 20, 0, 0, FILTERS_BUTTON_LOCATION, 32, 64, ModMenuScreenTexts.TOGGLE_FILTER_OPTIONS.asFormattedString(),
					button -> this.setFilterOptionsShown(!this.filterOptionsShown),
					AbstractButtonWidget.EMPTY //(AbstractButtonWidget button, int mouseX, int mouseY ) -> this.setTooltip( ModMenuScreenTexts.TOGGLE_FILTER_OPTIONS )
				);
		}

		// Sorting button
		this.sortingButton = new AbstractButtonWidget(1, this.filtersX, 45, sortingWidth, 20, sortingText, button -> {
			ModMenuConfig.SORTING.cycleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
			button.setMessage(ModMenuConfig.SORTING.getButtonText());
		});

		// Show libraries button
		this.librariesButton = new AbstractButtonWidget(2, this.filtersX + sortingWidth + 2, 45, librariesWidth, 20, librariesText, button -> {
			ModMenuConfig.SHOW_LIBRARIES.toggleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
			button.setMessage(ModMenuConfig.SHOW_LIBRARIES.getButtonText());
		});

		// Configure button
		if (!ModMenuConfig.HIDE_CONFIG_BUTTONS.getValue()) {
			this.configureButton = new LegacyTexturedButtonWidget(0, width - 24, RIGHT_PANE_Y, 20, 20, 0, 0, CONFIGURE_BUTTON_LOCATION, 32, 64, "", button -> {
					final String id = Objects.requireNonNull(selected).getMod().getId();
					if (modHasConfigScreen.get(id)) {
						Screen configScreen = ModMenu.getConfigScreen(id, this);
						client.setScreen(configScreen);
					} else {
						button.active = false;
					}
				}, AbstractButtonWidget.EMPTY);
		}

		// Website button
		int urlButtonWidths = this.paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);

		this.websiteButton = new AbstractButtonWidget(3, this.rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), RIGHT_PANE_Y + 36,
			Math.min(urlButtonWidths, 200), 20, ModMenuScreenTexts.WEBSITE, button -> {
				final Mod mod = Objects.requireNonNull(selected).getMod();
				boolean isMinecraft = selected.getMod().getId().equals("minecraft");

				if (isMinecraft) {
					this.client.setScreen(new ConfirmChatLinkScreen(
						(bool, id) -> {
							if (bool)
								UrlUtil.getOperatingSystem().open(Urls.JAVA_FEEDBACK);
							this.client.setScreen(this);
						},
						Urls.JAVA_FEEDBACK.toString(),
						999,
						false
					));
				} else {
					String url = mod.getWebsite();
					this.client.setScreen(new ConfirmChatLinkScreen(
						(bool, id) -> {
							if (bool)
								UrlUtil.getOperatingSystem().open(url);
							this.client.setScreen(this);
						},
						url,
						999,
						false
					));
				}
			});

		// Issues button
		this.issuesButton = new AbstractButtonWidget(4, this.rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2),
			RIGHT_PANE_Y + 36, Math.min(urlButtonWidths, 200), 20, ModMenuScreenTexts.ISSUES, button -> {
				final Mod mod = Objects.requireNonNull(selected).getMod();
				boolean isMinecraft = selected.getMod().getId().equals("minecraft");

				if (isMinecraft) {
					this.client.setScreen(new ConfirmChatLinkScreen(
						(bool, id) -> {
							if (bool)
								UrlUtil.getOperatingSystem().open(Urls.SNAPSHOT_BUGS);
							this.client.setScreen(this);
						},
						Urls.SNAPSHOT_BUGS.toString(),
						999,
						false
					));
				} else {
					String url = mod.getIssueTracker();
					this.client.setScreen(new ConfirmChatLinkScreen(
						(bool, id) -> {
							if (bool)
								UrlUtil.getOperatingSystem().open(url);
							this.client.setScreen(this);
						},
						url,
						999,
						false
					));
				}
			});

		// Description list
		this.descriptionListWidget = new DescriptionListWidget(this.client,
			this.paneWidth,
			this.height,
			RIGHT_PANE_Y + 60,
			this.height - 36,
			textRenderer.fontHeight + 1,
			this
		);
		this.descriptionListWidget.setXPos(this.rightPaneX);

		// Mods folder button
		AbstractButtonWidget modsFolderButton = new AbstractButtonWidget(5, this.width / 2 - 154, this.height - 28, 150, 20, ModMenuScreenTexts.MODS_FOLDER,
			button -> UrlUtil.getOperatingSystem().open(FabricLoader.getInstance().getGameDir().resolve("mods").toUri()));

		// Done button
		AbstractButtonWidget doneButton = new AbstractButtonWidget(6, this.width / 2 + 4, this.height - 28, 150, 20, ScreenTexts.DONE,
			button -> client.setScreen(this.getPreviousScreen()));

		// Initialize data
		modList.reloadFilters();
		this.setFilterOptionsShown(this.keepFilterOptionsShown && this.filterOptionsShown);

		// Add children
		if (this.filtersButton != null) {
			this.buttons.add(this.filtersButton);
		}

		this.buttons.add(this.sortingButton);
		this.buttons.add(this.librariesButton);
		this.addChild(this.modList);
		this.addChild(this.searchBox);

		if (this.configureButton != null) {
			this.buttons.add(this.configureButton);
		}

		this.buttons.add(this.websiteButton);
		this.buttons.add(this.issuesButton);
		this.addChild(this.descriptionListWidget);
		this.buttons.add(modsFolderButton);
		this.buttons.add(doneButton);

		this.init = true;
		this.keepFilterOptionsShown = true;
	}

	@Override
	public void keyPressed(char id, int keyCode) {
		this.searchBox.keyPressed(id, keyCode);
		super.keyPressed(id, keyCode);
	}

	@Override
	public void tick() {
		this.searchBox.tick();
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		this.renderDirtBackground(1);

		ModListEntry selectedEntry = selected;
		super.renderChildren(mouseX, mouseY, tickDelta);
		if (selectedEntry != null) {
			this.descriptionListWidget.render(mouseX, mouseY, tickDelta);
		}
		this.renderLabels(mouseX, mouseY);
		this.renderButtons(mouseX, mouseY, tickDelta);
		/*textRenderer.draw(  // very useful debug text
			String.format( "X: %s Y %s OnEntry: %s", mouseX, mouseY, this.modList.getEntryAt( mouseX, mouseY ) ),
			10, 10, 0xF0F0F0
		);*/

		GlStateManager.disableBlend();
		textRenderer.drawWithShadow(ModMenuScreenTexts.TITLE.asFormattedString(), this.modList.getWidth() / 2f - (textRenderer.getStringWidth(ModMenuScreenTexts.TITLE.asFormattedString()) / 2f), 8, 16777215);

		if (!ModMenuConfig.DISABLE_DRAG_AND_DROP.getValue()) {
			String line1 = ModMenuScreenTexts.DROP_INFO_LINE_1.setStyle(new Style().setFormatting(Formatting.GRAY)).asFormattedString();
			textRenderer.drawWithShadow(
				line1,
				this.width - this.modList.getWidth() / 2f - textRenderer.getStringWidth(line1) / 2f,
				RIGHT_PANE_Y / 2f - client.textRenderer.fontHeight - 1,
				0xFFFFFFFF
			);
			String line2 = ModMenuScreenTexts.DROP_INFO_LINE_2.setStyle(new Style().setFormatting(Formatting.GRAY)).asFormattedString();
			textRenderer.drawWithShadow(
				line2,
				this.width - this.modList.getWidth() / 2f - textRenderer.getStringWidth(line2) / 2f,
				RIGHT_PANE_Y / 2f + 1,
				0xFFFFFFFF
			);
		}
		if (!ModMenuConfig.CONFIG_MODE.getValue()) {
			Text fullModCount = this.computeModCountText(true);
			if (!ModMenuConfig.CONFIG_MODE.getValue() && this.updateFiltersX()) {
				if (!this.filterOptionsShown) {
					if (!ModMenuConfig.SHOW_LIBRARIES.getValue() ||
						textRenderer.getStringWidth(fullModCount.asFormattedString()) <= modList.getWidth() - 5) {
						textRenderer.draw(
							fullModCount.asFormattedString(),
							this.searchBoxX,
							52,
							0xFFFFFF,
							true
						);
					} else {
						textRenderer.draw(
							computeModCountText(false).asFormattedString(),
							this.searchBoxX,
							46,
							0xFFFFFF,
							true
						);
						textRenderer.draw(
							computeLibraryCountText().asFormattedString(),
							this.searchBoxX,
							57,
							0xFFFFFF,
							true
						);
					}
				}
			}
		}
		if (selectedEntry != null) {
			Mod mod = selectedEntry.getMod();
			int x = this.rightPaneX;
			if ("java".equals(mod.getId())) {
				DrawingUtil.drawRandomVersionBackground(mod, x, RIGHT_PANE_Y, 32, 32);
			}

		 	MinecraftClient.getInstance().getTextureManager().bindTexture(this.selected.getIconTexture());
			GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
			GlStateManager.enableBlend();
			drawTexture(x, RIGHT_PANE_Y, 0.0F, 0.0F, 32, 32, 32, 32);
			GlStateManager.disableBlend();

			int lineSpacing = textRenderer.fontHeight + 1;
			int imageOffset = 36;
			Text name = new LiteralText(mod.getTranslatedName());
			String trimmedName = name.asFormattedString();
			int maxNameWidth = this.width - (x + imageOffset);
			if (textRenderer.getStringWidth(name.asFormattedString()) > maxNameWidth) {
				trimmedName = textRenderer.trimToWidth(name.asFormattedString(), maxNameWidth - textRenderer.getStringWidth("...")) + "...";
			}
			textRenderer.draw(
				(trimmedName), //Language.getInstance().reorder
				x + imageOffset,
				RIGHT_PANE_Y + 1,
				0xFFFFFF,
				true
			);
			if (this.init || modBadgeRenderer == null || modBadgeRenderer.getMod() != mod) {
				modBadgeRenderer = new ModBadgeRenderer(
					x + imageOffset + this.client.textRenderer.getStringWidth(trimmedName) + 2,
					RIGHT_PANE_Y,
					width - 28,
					selectedEntry.mod,
					this
				);
				this.init = false;
			}
			if (!ModMenuConfig.HIDE_BADGES.getValue()) {
				modBadgeRenderer.draw(mouseX, mouseY);
			}
			if (mod.isReal()) {
				textRenderer.draw(
					mod.getPrefixedVersion(),
					x + imageOffset,
					RIGHT_PANE_Y + 2 + lineSpacing,
					0x808080,
					true
				);
			}
			String authors;
			List<String> names = mod.getAuthors();

			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.get(0);
				}
				DrawingUtil.drawWrappedString(
					I18n.translate("modmenu.authorPrefix", authors),
					x + imageOffset,
					RIGHT_PANE_Y + 2 + lineSpacing * 2,
					this.paneWidth - imageOffset - 4,
					1,
					0x808080
				);
			}
		}
	}

	private Text computeModCountText(boolean includeLibs) {
		int[] rootMods = formatModCount(ModMenu.ROOT_MODS.values()
			.stream()
			.filter(mod -> !mod.isHidden() && !mod.getBadges().contains(Mod.Badge.LIBRARY))
			.map(Mod::getId)
			.collect(Collectors.toSet()));

		if (includeLibs && ModMenuConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values()
				.stream()
				.filter(mod -> !mod.isHidden() && mod.getBadges().contains(Mod.Badge.LIBRARY))
				.map(Mod::getId)
				.collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modmenu.showingModsLibraries", rootMods, rootLibs);
		} else {
			return TranslationUtil.translateNumeric("modmenu.showingMods", rootMods);
		}
	}

	private Text computeLibraryCountText() {
		if (ModMenuConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values()
				.stream()
				.filter(mod -> !mod.isHidden() && mod.getBadges().contains(Mod.Badge.LIBRARY))
				.map(Mod::getId)
				.collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modmenu.showingLibraries", rootLibs);
		} else {
			return new LiteralText(null);
		}
	}

	private int[] formatModCount(Set<String> set) {
		int visible = this.modList.getDisplayedCountFor(set);
		int total = set.size();
		if (visible == total) {
			return new int[]{ total };
		}
		return new int[]{ visible, total };
	}

	private void setFilterOptionsShown(boolean filterOptionsShown) {
		this.filterOptionsShown = filterOptionsShown;

		this.sortingButton.visible = filterOptionsShown;
		this.librariesButton.visible = filterOptionsShown;
	}

	public ModListEntry getSelectedEntry() {
		return selected;
	}

	public void updateSelectedEntry(ModListEntry entry) {
		if (entry != null) {
			this.selected = entry;
			String modId = selected.getMod().getId();

			if (this.configureButton != null) {

				this.configureButton.active = modHasConfigScreen.get(modId);
				this.configureButton.visible = selected != null && modHasConfigScreen.get(modId) || modScreenErrors.containsKey(modId);
			}

			boolean isMinecraft = modId.equals("minecraft");

			if (isMinecraft) {
				this.websiteButton.setMessage(SEND_FEEDBACK_TEXT);
				this.issuesButton.setMessage(REPORT_BUGS_TEXT);
			} else {
				this.websiteButton.setMessage(ModMenuScreenTexts.WEBSITE);
				this.issuesButton.setMessage(ModMenuScreenTexts.ISSUES);
			}

			this.websiteButton.visible = true;
			this.websiteButton.active = isMinecraft || selected.getMod().getWebsite() != null;

			this.issuesButton.visible = true;
			this.issuesButton.active = isMinecraft || selected.getMod().getIssueTracker() != null;
		}
	}

	public String getSearchInput() {
		return this.searchBox.getText();
	}

	private boolean updateFiltersX() {
		if ((this.filtersWidth + textRenderer.getStringWidth(this.computeModCountText(true).asFormattedString()) + 20) >= this.searchRowWidth &&
			((this.filtersWidth + textRenderer.getStringWidth(computeModCountText(false).asFormattedString()) + 20) >= this.searchRowWidth ||
				(this.filtersWidth + textRenderer.getStringWidth(this.computeLibraryCountText().asFormattedString()) + 20) >= this.searchRowWidth
			)) {
			this.filtersX = this.paneWidth / 2 - this.filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			this.filtersX = this.searchRowWidth - this.filtersWidth + 1;
			return true;
		}
	}

	public Map<String, Boolean> getModHasConfigScreen() {
		return this.modHasConfigScreen;
	}
}
