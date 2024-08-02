package com.terraformersmc.modmenu;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.util.EnumToLowerCaseJsonConverter;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricDummyParentMod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.util.*;

import static com.terraformersmc.modmenu.util.TranslationUtil.hasTranslation;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Logger LOGGER = LogManager.getLogger("Mod Menu");
	public static final Gson GSON;
	public static final Gson GSON_MINIFIED;

	static {
		GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(Enum.class, new EnumToLowerCaseJsonConverter())
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		GSON = builder.setPrettyPrinting().create();
		GSON_MINIFIED = builder.create();
	}

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();

	private static final Map<String, ConfigScreenFactory<?>> configScreenFactories = new HashMap<>();
	private static final List<ModMenuApi> apiImplementations = new ArrayList<>();

	private static int cachedDisplayedModCount = -1;

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		for (ModMenuApi api : apiImplementations) {
			Map<String, ConfigScreenFactory<?>> factoryProviders = api.getProvidedConfigScreenFactories();
			if (!factoryProviders.isEmpty()) {
				factoryProviders.forEach(configScreenFactories::putIfAbsent);
			}
		}
		if (ModMenuConfig.HIDDEN_CONFIGS.getValue().contains(modid)) {
			return null;
		}
		ConfigScreenFactory<?> factory = configScreenFactories.get(modid);
		if (factory != null) {
			return factory.create(menuScreen);
		}
		return null;
	}

	@Override
	public void onInitializeClient() {
		ModMenuConfigManager.initializeConfig();
		Set<String> modpackMods = new HashSet<>();
		Map<String, UpdateChecker> updateCheckers = new HashMap<>();
		Map<String, UpdateChecker> providedUpdateCheckers = new HashMap<>();

		FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class).forEach(entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			String modId = metadata.getId();
			try {
				ModMenuApi api = entrypoint.getEntrypoint();
				configScreenFactories.put(modId, api.getModConfigScreenFactory());
				apiImplementations.add(api);
				updateCheckers.put(modId, api.getUpdateChecker());
				providedUpdateCheckers.putAll(api.getProvidedUpdateCheckers());
				api.attachModpackBadges(modpackMods::add);
			} catch (Throwable e) {
				LOGGER.error("Mod {} provides a broken implementation of ModMenuApi", modId, e);
			}
		});

		// Fill mods map
		for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
			Mod mod = new FabricMod(modContainer, modpackMods);

			UpdateChecker updateChecker = updateCheckers.get(mod.getId());

			if (updateChecker == null) {
				updateChecker = providedUpdateCheckers.get(mod.getId());
			}

			MODS.put(mod.getId(), mod);
			mod.setUpdateChecker(updateChecker);
		}

		Map<String, Mod> dummyParents = new HashMap<>();

		// Initialize parent map
		for (Mod mod : MODS.values()) {
			String parentId = mod.getParent();
			if (parentId != null) {
				Mod parent = MODS.getOrDefault(parentId, dummyParents.get(parentId));
				if (parent == null) {
					if (mod instanceof FabricMod) {
						parent = new FabricDummyParentMod((FabricMod) mod, parentId);
						dummyParents.put(parentId, parent);
					}
				}
				if (parent != null) {
					PARENT_MAP.put(parent, mod);
				}
			} else {
				ROOT_MODS.put(mod.getId(), mod);
			}
		}
		MODS.putAll(dummyParents);
		ModMenuEventHandler.register();
	}

	public static void clearModCountCache() {
		cachedDisplayedModCount = -1;
	}

	public static boolean areModUpdatesAvailable() {
		if (!ModMenuConfig.UPDATE_CHECKER.getValue()) {
			return false;
		}

		for (Mod mod : MODS.values()) {
			if (mod.isHidden()) {
				continue;
			}

			if (!ModMenuConfig.SHOW_LIBRARIES.getValue() && mod.getBadges().contains(Mod.Badge.LIBRARY)) {
				continue;
			}

			if (mod.hasUpdate() || mod.getChildHasUpdate()) {
				return true; // At least one currently visible mod has an update
			}
		}

		return false;
	}

	public static String getDisplayedModCount() {
		if (cachedDisplayedModCount == -1) {
			boolean includeChildren = ModMenuConfig.COUNT_CHILDREN.getValue();
			boolean includeLibraries = ModMenuConfig.COUNT_LIBRARIES.getValue();
			boolean includeHidden = ModMenuConfig.COUNT_HIDDEN_MODS.getValue();

			// listen, if you have >= 2^32 mods then that's on you
			cachedDisplayedModCount = Math.toIntExact(MODS.values().stream().filter(mod -> {
				boolean isChild = mod.getParent() != null;
				if (!includeChildren && isChild) {
					return false;
				}
				boolean isLibrary = mod.getBadges().contains(Mod.Badge.LIBRARY);
				if (!includeLibraries && isLibrary) {
					return false;
				}
				return includeHidden || !mod.isHidden();
			}).count());
		}
		return NumberFormat.getInstance().format(cachedDisplayedModCount);
	}

	public static Text createModsButtonText(boolean title) {
		ModMenuConfig.TitleMenuButtonStyle titleStyle = ModMenuConfig.MODS_BUTTON_STYLE.getValue();
		ModMenuConfig.GameMenuButtonStyle gameMenuStyle = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
		boolean isIcon = title ?
			titleStyle == ModMenuConfig.TitleMenuButtonStyle.ICON :
			gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.ICON;
		boolean isShort = title ?
			titleStyle == ModMenuConfig.TitleMenuButtonStyle.SHRINK :
			gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.REPLACE;
		Text modsText = ModMenuScreenTexts.TITLE.copy();
		if (ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnModsButton() && !isIcon) {
			String count = ModMenu.getDisplayedModCount();
			if (isShort) {
				modsText.append(new LiteralText(" ")).append(new TranslatableText("modmenu.loaded.short", count));
			} else {
				String specificKey = "modmenu.loaded." + count;
				String key = hasTranslation(specificKey) ? specificKey : "modmenu.loaded";
				if (ModMenuConfig.EASTER_EGGS.getValue() && hasTranslation(specificKey + ".secret")) {
					key = specificKey + ".secret";
				}
				modsText.append(new LiteralText(" ")).append(new TranslatableText(key, count));
			}
		}
		return modsText;
	}

}
