package io.github.prospector.modmenu.api;

import io.github.prospector.modmenu.util.mod.ModIconHandler;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public interface Mod {
	@NotNull
	String getId();

	@NotNull
	String getName();

	@NotNull
	NativeImageBackedTexture getIcon( ModIconHandler iconHandler, int i );

	@NotNull
	String getSummary();

	@NotNull
	String getDescription();

	@NotNull
	String getVersion();

	@NotNull
	String getPrefixedVersion();

	@NotNull
	List<String> getAuthors();

	@NotNull
	List<String> getContributors();

	@NotNull
	Set<Badge> getBadges();

	@Nullable
	String getWebsite();

	@Nullable
	String getIssueTracker();

	@Nullable
	String getSource();

	@Nullable
	String getParent();

	@NotNull
	Set<String> getLicense();

	@NotNull
	Map<String, String> getLinks();

	boolean isReal();

	final class Badge {
		private static final Map<String, Badge> KEY_MAP = new HashMap<>();
		public static final Badge LIBRARY = register( "modmenu.badge.library", 0xff107454, 0xff093929, "library" );
		public static final Badge CLIENT = register( "modmenu.badge.clientsideOnly", 0xff2b4b7c, 0xff0e2a55, null );
		public static final Badge DEPRECATED = register( "modmenu.badge.deprecated", 0xff841426, 0xff530C17, "deprecated" );
		public static final Badge FORGE = register( "modmenu.badge.forge", 0xff1f2d42, 0xff101721, "forge" );
		public static final Badge MODPACK = register( "modmenu.badge.modpack", 0xff7a2b7c, 0xff510d54, null );
		public static final Badge MINECRAFT = register( "modmenu.badge.minecraft", 0xff6f6c6a, 0xff31302f, null );

		private final Text text;
		private final int outlineColor, fillColor;

		private Badge( String translationKey, int outlineColor, int fillColor ) {
			this.text = new TranslatableText( translationKey );
			this.outlineColor = outlineColor;
			this.fillColor = fillColor;
		}

		public Text getText() {
			return this.text;
		}

		public int getOutlineColor() {
			return this.outlineColor;
		}

		public int getFillColor() {
			return this.fillColor;
		}

		public static Set<Badge> convert( Set<String> badgeKeys ) {
			return badgeKeys.stream()
				.map( KEY_MAP::get )
				.filter( Objects::nonNull )
				.collect( Collectors.toSet() );
		}

		public static Badge register( String translationKey, int outlineColor, int fillColor, String key ) {
			Badge badge = new Badge( translationKey, outlineColor, fillColor );
			KEY_MAP.putIfAbsent( key, badge );
			return badge;
		}
	}
}
