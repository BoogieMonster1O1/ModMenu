package io.github.prospector.modmenu;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.api.Mod;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.ModMenuApiMarker;
import io.github.prospector.modmenu.util.mod.fabric.FabricDummyParentMod;
import io.github.prospector.modmenu.util.mod.fabric.FabricMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.util.*;

import static io.github.prospector.modmenu.util.TranslationUtil.hasTranslation;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Logger LOGGER = LogManager.getLogger( "Mod Menu" );
	public static final Gson GSON = new GsonBuilder()
		.setFieldNamingPolicy( FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES )
		.setPrettyPrinting()
		.create();

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();

	private static final Map<String, ConfigScreenFactory<?>> configScreenFactories = new HashMap<>();
	private static final List<Map<String, ConfigScreenFactory<?>>> delayedScreenFactoryProviders = new ArrayList<>();

	private static int cachedDisplayedModCount = -1;

	public static boolean hasFactory( String modid ) {
		return configScreenFactories.containsKey( modid );
	}

	public static Screen getConfigScreen( String modid, Screen menuScreen ) {
		if ( !delayedScreenFactoryProviders.isEmpty() ) {
			delayedScreenFactoryProviders.forEach( map -> map.forEach( configScreenFactories::putIfAbsent ) );
			delayedScreenFactoryProviders.clear();
		}
		ConfigScreenFactory<?> factory = configScreenFactories.get( modid );
		if ( factory != null ) {
			return factory.create( menuScreen );
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onInitializeClient() {
		ModMenuConfigManager.initializeConfig();
		Set<String> modpackMods = new HashSet<>();
		Map<String, String> additionalParents = new HashMap<>();
		// find all entrypoints
		List<EntrypointContainer<ModMenuApiMarker>> entrypoints = FabricLoader.getInstance().getEntrypointContainers( "modmenu", ModMenuApiMarker.class );
		// badges should be loaded first, as the other things depend on them
		entrypoints.forEach( entrypoint -> {
			if ( entrypoint.getEntrypoint() instanceof io.github.prospector.modmenu.api.ModMenuApi ) {
				try {
					( (io.github.prospector.modmenu.api.ModMenuApi) entrypoint.getEntrypoint() ).onSetupBadges();
				} catch ( Throwable err ) {
					LOGGER.error(
						"Failed to setup badges from mod '{}': ",
						entrypoint.getProvider().getMetadata().getId(),
						err
					);
				}
			}
		} );
		// load everything else
		entrypoints.forEach( entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			String modId = metadata.getId();
			try {
				ModMenuApiMarker marker = entrypoint.getEntrypoint();
				if ( marker instanceof com.terraformersmc.modmenu.api.ModMenuApi ) {
					/* Current API */
					ModMenuApi api = (com.terraformersmc.modmenu.api.ModMenuApi) marker;
					configScreenFactories.put( modId, api.getModConfigScreenFactory() );
					delayedScreenFactoryProviders.add( api.getProvidedConfigScreenFactories() );
					api.attachModpackBadges( modpackMods::add );
				} else if ( marker instanceof io.github.prospector.modmenu.api.ModMenuApi ) {
					/* Legacy API */
					io.github.prospector.modmenu.api.ModMenuApi api = (io.github.prospector.modmenu.api.ModMenuApi) entrypoint.getEntrypoint();
					configScreenFactories.put( modId, screen -> api.getModConfigScreenFactory().create( screen ) );
					api.getAdditionalMods().forEach( mod -> MODS.put( mod.getId(), mod ) );
					additionalParents.putAll( api.getAdditionalParents() );
					api.getProvidedConfigScreenFactories().forEach( (id, legacyFactory) -> configScreenFactories.put( id, legacyFactory::create ));
				} else {
					throw new RuntimeException( modId + " is providing an invalid ModMenuApi implementation" );
				}
			} catch ( Throwable e ) {
				LOGGER.error( "Mod {} provides a broken implementation of ModMenuApi", modId, e );
			}
		} );

		// fill mods map
		for ( ModContainer modContainer : FabricLoader.getInstance().getAllMods() ) {
			if ( !ModMenuConfig.HIDDEN_MODS.getValue().contains( modContainer.getMetadata().getId() ) ) {
				Mod mod = new FabricMod( modContainer, modpackMods );
				MODS.put( mod.getId(), mod );
			}
		}

		Map<String, Mod> dummyParents = new HashMap<>();
		// Init parents map
		for ( Mod mod : MODS.values() ) {
			String parentId = mod.getParent();
			if ( parentId != null ) {
				Mod parent = MODS.getOrDefault( parentId, dummyParents.get( parentId ) );
				if ( parent != null ) {
					if ( mod instanceof FabricMod ) {
						parent = new FabricDummyParentMod( (FabricMod) mod, parentId );
						dummyParents.put( parentId, parent );
					}
				}
				PARENT_MAP.put( parent, mod );
			} else {
				ROOT_MODS.put( mod.getId(), mod );
			}
		}
		for ( Map.Entry<String, String> entry : additionalParents.entrySet() ) {
			// get both the parentId and the child mod object
			String parentId = entry.getValue();
			Mod mod = MODS.get( entry.getKey() );
			// if neither of them are null
			if ( parentId != null && mod != null ) {
				// get the parent mod obj
				Mod parent = MODS.getOrDefault( parentId, dummyParents.get( parentId ) );
				if ( parent != null ) {
					// if it isn't null, add the child to it
					if ( mod instanceof FabricMod ) {
						parent = new FabricDummyParentMod( (FabricMod) entry, parentId );
						dummyParents.put( parentId, parent );
					}
				}
				PARENT_MAP.put( parent, MODS.get( entry.getKey() ) );
				ROOT_MODS.remove( entry.getKey() );
			}
		}

		MODS.putAll( dummyParents );
	}

	public static void clearModCountCache() {
		cachedDisplayedModCount = -1;
	}

	public static String getDisplayedModCount() {
		if ( cachedDisplayedModCount == -1 ) {
			// listen, if you have >= 2^32 mods then that's on you
			cachedDisplayedModCount = Math.toIntExact(
				MODS.values().stream().filter( mod ->
					( ModMenuConfig.COUNT_CHILDREN.getValue() || mod.getParent() == null ) &&
						( ModMenuConfig.COUNT_LIBRARIES.getValue() || !mod.getBadges().contains( Mod.Badge.LIBRARY ) ) &&
						( ModMenuConfig.COUNT_HIDDEN_MODS.getValue() || !ModMenuConfig.HIDDEN_MODS.getValue().contains( mod.getId() ) )
				).count()
			);
		}
		return NumberFormat.getInstance().format( cachedDisplayedModCount );
	}

	public static Text createModsButtonText() {
		TranslatableText modsText = new TranslatableText( "modmenu.title" );
		if ( ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnModsButton() && ModMenuConfig.MODS_BUTTON_STYLE.getValue() != ModMenuConfig.ModsButtonStyle.ICON ) {
			String count = ModMenu.getDisplayedModCount();
			if ( ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.SHRINK ) {
				modsText.append( new LiteralText( " " ) ).append( new TranslatableText( "modmenu.loaded.short", count ) );
			} else {
				String specificKey = "modmenu.loaded." + count;
				String key = hasTranslation( specificKey ) ? specificKey : "modmenu.loaded";
				if ( ModMenuConfig.EASTER_EGGS.getValue() && hasTranslation( specificKey + ".secret" ) )
					key = specificKey + ".secret";
				modsText.append( new LiteralText( " " ) ).append( new TranslatableText( key, count ) );
			}
		}
		return modsText;
	}

}
