package io.github.prospector.modmenu.util.mod;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.api.Mod;
import io.github.prospector.modmenu.gui.ModsScreen;
import net.minecraft.text.TranslatableText;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ModSearch {

	public static boolean validSearchQuery( String query ) {
		return query != null && !query.isEmpty();
	}

	public static List<Mod> search( ModsScreen screen, String query, List<Mod> candidates ) {
		if ( !validSearchQuery( query ) )
			return candidates;

		return candidates.stream()
			.filter( modContainer -> passesFilters( screen, modContainer, query.toLowerCase( Locale.ROOT ) ) )
			.collect( Collectors.toList() );
	}

	private static boolean passesFilters( ModsScreen screen, Mod mod, String query ) {
		String modId = mod.getId();
		String modDescription = mod.getDescription();
		String modSummary = mod.getSummary();

		String library = new TranslatableText( "modmenu.searchTerms.library" ).toString();
		String modpack = new TranslatableText( "modmenu.searchTerms.modpack" ).toString();
		String deprecated = new TranslatableText( "modmenu.searchTerms.deprecated" ).toString();
		String clientside = new TranslatableText( "modmenu.searchTerms.clientside" ).toString();
		String configurable = new TranslatableText( "modmenu.searchTerms.configurable" ).toString();

		// Some basic search, could do with something more advanced but this will do for now
		if (
			mod.getName().toLowerCase( Locale.ROOT ).contains( query ) // Search mod name
				|| modId.toLowerCase( Locale.ROOT ).contains( query ) // Search mod ID
				|| modDescription.toLowerCase( Locale.ROOT ).contains( query ) // Search mod description
				|| modSummary.toLowerCase( Locale.ROOT ).contains( query ) // Search mod summary
				|| authorMatches( mod, query ) // Search via author
				|| library.contains( query ) && mod.getBadges().contains( Mod.Badge.LIBRARY ) // Search for lib mods
				|| modpack.contains( query ) && mod.getBadges().contains( Mod.Badge.MODPACK ) // Search for modpack mods
				|| deprecated.contains( query ) && mod.getBadges().contains( Mod.Badge.DEPRECATED ) // Search for deprecated mods
				|| clientside.contains( query ) && mod.getBadges().contains( Mod.Badge.CLIENT ) // Search for clientside mods
				|| configurable.contains( query ) && screen.getModHasConfigScreen().get( modId ) // Search for mods that can be configured
		) return true;

		// Allow parent to pass filter if a child passes
		if ( ModMenu.PARENT_MAP.keySet().contains( mod ) ) {
			for ( Mod child : ModMenu.PARENT_MAP.get( mod ) ) {
				if ( passesFilters( screen, child, query ) )
					return true;
			}
		}
		return false;
	}

	private static boolean authorMatches( Mod mod, String query ) {
		return mod.getAuthors().stream()
			.map( s -> s.toLowerCase( Locale.ROOT ) )
			.anyMatch( s -> s.contains( query.toLowerCase( Locale.ROOT ) ) );
	}

}
