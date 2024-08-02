package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.UrlUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.*;

import static com.terraformersmc.modmenu.util.TranslationUtil.hasTranslation;

public class DescriptionListWidget extends BetterEntryListWidget<DescriptionListWidget.DescriptionEntry> {

	private static final Text HAS_UPDATE_TEXT = new TranslatableText("modmenu.hasUpdate");
	private static final Text EXPERIMENTAL_TEXT = new TranslatableText("modmenu.experimental")
		.setStyle(new Style().setFormatting(Formatting.GOLD));
	private static final Text DOWNLOAD_TEXT = new TranslatableText("modmenu.downloadLink")
		.setStyle(new Style().setFormatting(Formatting.BLUE).setUnderline(true));
	private static final Text CHILD_HAS_UPDATE_TEXT = new TranslatableText("modmenu.childHasUpdate");
	private static final Text LINKS_TEXT = new TranslatableText("modmenu.links");
	private static final Text SOURCE_TEXT = new TranslatableText("modmenu.source")
		.setStyle(new Style().setFormatting(Formatting.BLUE).setUnderline(true));
	private static final Text LICENSE_TEXT = new TranslatableText("modmenu.license");
	private static final Text VIEW_CREDITS_TEXT = new TranslatableText("modmenu.viewCredits")
		.setStyle(new Style().setFormatting(Formatting.BLUE).setUnderline(true));
	private static final Text CREDITS_TEXT = new TranslatableText("modmenu.credits");

	private final ModsScreen parent;
	private final TextRenderer textRenderer;
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(
		MinecraftClient client,
		int width,
		int height,
		int y,
		int y2,
		int itemHeight,
		ModsScreen parent
	) {
		super(client, width, height, y, y2, itemHeight);
		this.parent = parent;
		this.textRenderer = client.textRenderer;
	}

	@Override
	public DescriptionEntry getSelectedOrNull() {
		return null;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6 + this.xStart;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clearEntries();
			scrollAmount = -Float.MAX_VALUE;
			if (lastSelected != null) {
				DescriptionEntry emptyEntry = new DescriptionEntry("");
				int wrapWidth = getRowWidth() - 5;

				Mod mod = lastSelected.getMod();
				Text description = mod.getFormattedDescription();

				if (description != null) {
					for (String line : textRenderer.wrapLines(description.asFormattedString(), wrapWidth)) {
						addEntry(new DescriptionEntry(line));
					}
				}

				if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue()
					.contains(mod.getId())) {
					UpdateInfo updateInfo = mod.getUpdateInfo();
					if (updateInfo != null && updateInfo.isUpdateAvailable()) {
						addEntry(emptyEntry);

						int index = 0;
						for (String line : textRenderer.wrapLines(HAS_UPDATE_TEXT.asFormattedString(), wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry(line);
							if (index == 0) {
								entry.setUpdateTextEntry();
							}

							addEntry(entry);
							index += 1;
						}

						for (String line : textRenderer.wrapLines(EXPERIMENTAL_TEXT.asFormattedString(), wrapWidth - 16)) {
							addEntry(new DescriptionEntry(line, 8));
						}


						Text updateMessage = updateInfo.getUpdateMessage();
						String downloadLink = updateInfo.getDownloadLink();
						if (updateMessage == null) {
							updateMessage = DOWNLOAD_TEXT;
						} else {
							if (downloadLink != null) {
								updateMessage = updateMessage.copy()
									.setStyle(new Style().setFormatting(Formatting.BLUE).setUnderline(true));
							}
						}
						for (String line : textRenderer.wrapLines(updateMessage.asFormattedString(), wrapWidth - 16)) {
							if (downloadLink != null) {
								addEntry(new LinkEntry(line, downloadLink, 8));
							} else {
								addEntry(new DescriptionEntry(line, 8));

							}
						}
					}
					if (mod.getChildHasUpdate()) {
						addEntry(emptyEntry);

						int index = 0;
						for (String line : textRenderer.wrapLines(CHILD_HAS_UPDATE_TEXT.asFormattedString(), wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry(line);
							if (index == 0) {
								entry.setUpdateTextEntry();
							}

							addEntry(entry);
							index += 1;
						}
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ((!links.isEmpty() || sourceLink != null) && !ModMenuConfig.HIDE_MOD_LINKS.getValue()) {
					addEntry(emptyEntry);

					for (String line : textRenderer.wrapLines(LINKS_TEXT.asFormattedString(), wrapWidth)) {
						addEntry(new DescriptionEntry(line));
					}

					if (sourceLink != null) {
						int indent = 8;
						for (String line : textRenderer.wrapLines(SOURCE_TEXT.asFormattedString(), wrapWidth - 16)) {
							addEntry(new LinkEntry(line, sourceLink, indent));
							indent = 16;
						}
					}

					links.forEach((key, value) -> {
						int indent = 8;
						for (String line : textRenderer.wrapLines(new TranslatableText(key)
								.setStyle(new Style().setFormatting(Formatting.BLUE).setUnderline(true))
								.asFormattedString(),
							wrapWidth - 16
						)) {
							addEntry(new LinkEntry(line, value, indent));
							indent = 16;
						}
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
					addEntry(emptyEntry);

					for (String line : textRenderer.wrapLines(LICENSE_TEXT.asFormattedString(), wrapWidth)) {
						addEntry(new DescriptionEntry(line));
					}

					for (String license : licenses) {
						int indent = 8;
						for (String line : textRenderer.wrapLines(new LiteralText(license).asFormattedString(), wrapWidth - 16)) {
							addEntry(new DescriptionEntry(line, indent));
							indent = 16;
						}
					}
				}

				if (!ModMenuConfig.HIDE_MOD_CREDITS.getValue()) {
					if ("minecraft".equals(mod.getId())) {
						addEntry(emptyEntry);

						for (String line : textRenderer.wrapLines(VIEW_CREDITS_TEXT.asFormattedString(), wrapWidth)) {
							addEntry(new MojangCreditsEntry(line));
						}
					} else if (!"java".equals(mod.getId())) {
						SortedMap<String, Set<String>> credits = mod.getCredits();

						if (!credits.isEmpty()) {
							addEntry(emptyEntry);

							for (String line : textRenderer.wrapLines(CREDITS_TEXT.asFormattedString(), wrapWidth)) {
								addEntry(new DescriptionEntry(line));
							}

							Iterator<Map.Entry<String, Set<String>>> iterator = credits.entrySet().iterator();

							while (iterator.hasNext()) {
								int indent = 8;

								Map.Entry<String, Set<String>> role = iterator.next();
								String roleName = role.getKey();

								for (String line : textRenderer.wrapLines(this.creditsRoleText(roleName).asFormattedString(),
									wrapWidth - 16
								)) {
									addEntry(new DescriptionEntry(line, indent));
									indent = 16;
								}

								for (String contributor : role.getValue()) {
									indent = 16;

									for (String line : textRenderer.wrapLines(new LiteralText(contributor).asFormattedString(), wrapWidth - 24)) {
										addEntry(new DescriptionEntry(line, indent));
										indent = 24;
									}
								}

								if (iterator.hasNext()) {
									addEntry(emptyEntry);
								}
							}
						}
					}
				}
			}
		}

		super.render(mouseX, mouseY, delta);
	}

	private Text creditsRoleText(String roleName) {
		// Replace spaces and dashes in role names with underscores if they exist
		// Notably Quilted Fabric API does this with FabricMC as "Upstream Owner"
		String translationKey = roleName.replaceAll("[ -]", "_").toLowerCase();

		// Add an s to the default untranslated string if it ends in r since this
		// Fixes common role names people use in English (e.g. Author -> Authors)
		if (hasTranslation("modmenu.credits.role." + translationKey)) {
			return new TranslatableText("modmenu.credits.role." + translationKey).append(new LiteralText(":"));
		}

		return roleName.endsWith("r") ? new LiteralText(roleName + "s") : new LiteralText(roleName);
	}

	protected class DescriptionEntry extends Entry<DescriptionEntry> {
		protected final String text;
		protected final int indent;
		public boolean updateTextEntry = false;

		public DescriptionEntry(String text, int indent) {
			this.text = text;
			this.indent = indent;
		}

		public DescriptionEntry(String text) {
			this(text, 0);
		}

		public void setUpdateTextEntry() {
			this.updateTextEntry = true;
		}

		@Override
		public void render( int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta ) {
			this.render( index, x, y, entryWidth, entryHeight, mouseX, mouseY, hovered );
		}

		@Override
		public void render( int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered ) {
			/*if (updateTextEntry) {
				UpdateAvailableBadge.renderBadge(x + indent, y);
				x += 11;
			}*/
			textRenderer.drawWithShadow(text, x + indent, y, 0xAAAAAA);
		}

		@Override
		public boolean mouseClicked( int index, int mouseX, int mouseY, int button, int x, int y ) {
			return false;
		}

		@Override
		public void mouseReleased( int index, int mouseX, int mouseY, int button, int x, int y ) { }

		@Override
		public void updatePosition( int index, int x, int y ) { }
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(String text) {
			super(text);
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
			if (isMouseOver(mouseX, mouseY)) {
				client.setScreen(new CreditsScreen());
			}
			return super.mouseClicked(index, mouseX, mouseY, button, x, y);
		}
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry(String text, String link, int indent) {
			super(text, indent);
			this.link = link;
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
			if (isMouseOver(mouseX, mouseY)) {
				client.setScreen(new ConfirmChatLinkScreen((open, c) -> {
					if (open) {
						UrlUtil.getOperatingSystem().open(link);
					}
					client.setScreen(parent);
				}, link, 9999, false));
			}
			return super.mouseClicked(index, mouseX, mouseY, button, x, y);
		}
	}

}
