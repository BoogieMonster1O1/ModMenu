package io.github.prospector.modmenu.util.mod.fabric;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.api.Mod;
import io.github.prospector.modmenu.util.mod.ModIconHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FabricDummyParentMod implements Mod {
	private final String id;
	private final FabricMod host;

	public FabricDummyParentMod( FabricMod host, String id ) {
		this.host = host;
		this.id = id;
	}

	@Override
	public @NotNull String getId() {
		return id;
	}

	@Override
	public @NotNull String getName() {
		FabricMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		if ( parentData != null ) {
			return parentData.getName().orElse( "" );
		}
		if ( id.equals( "fabric" ) || id.equals( "legacy-fabric-api" ) ) {
			return "Legacy Fabric API";
		}
		return id;
	}

	@Override
	public @NotNull NativeImageBackedTexture getIcon( ModIconHandler iconHandler, int i ) {
		String iconSourceId = host.getId();
		FabricMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		String iconPath = null;
		if ( parentData != null ) {
			iconPath = parentData.getIcon().orElse( null );
		}
		if ( "inherit".equals( iconPath ) ) {
			return host.getIcon( iconHandler, i );
		}
		if ( iconPath == null ) {
			iconSourceId = ModMenu.MOD_ID;
			iconPath = "assets/" + ModMenu.MOD_ID + "/icons/unknown_parent.png";
		}
		final String finalIconSourceId = iconSourceId;
		ModContainer iconSource = FabricLoader.getInstance()
			.getModContainer( iconSourceId )
			.orElseThrow( () -> new RuntimeException( "Cannot get ModContainer for Fabric mod with id " + finalIconSourceId ) );
		return Objects.requireNonNull(
			iconHandler.createIcon( iconSource, iconPath ),
			"Mod icon for " + getId() + " is null somehow (should be filled with default in this case)"
		);
	}

	@Override
	public @NotNull String getSummary() {
		return getDescription();
	}

	@Override
	public @NotNull String getDescription() {
		FabricMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		if ( parentData != null ) {
			return parentData.getDescription().orElse( "" );
		}
		return "";
	}

	@Override
	public @NotNull String getVersion() {
		return "";
	}

	@Override
	public @NotNull String getPrefixedVersion() {
		return "";
	}

	@Override
	public @NotNull List<String> getAuthors() {
		return new ArrayList<>();
	}

	@Override
	public @NotNull List<String> getContributors() {
		return new ArrayList<>();
	}

	@Override
	public @NotNull Set<Badge> getBadges() {
		FabricMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		if ( parentData != null ) {
			return parentData.getBadges();
		}
		Set<Badge> badges = new HashSet<>();
		if ( id.equals( "fabric" ) || id.equals( "legacy-fabric-api" ) ) {
			badges.add( Badge.LIBRARY );
		}

		boolean modpackChildren = true;
		for ( Mod mod : ModMenu.PARENT_MAP.get( this ) ) {
			if ( !mod.getBadges().contains( Badge.MODPACK ) ) {
				modpackChildren = false;
			}
		}
		if ( modpackChildren ) {
			badges.add( Badge.MODPACK );
		}

		return badges;
	}

	@Override
	public @Nullable String getWebsite() {
		return null;
	}

	@Override
	public @Nullable String getIssueTracker() {
		return null;
	}

	@Override
	public @Nullable String getSource() {
		return null;
	}

	@Override
	public @Nullable String getParent() {
		return null;
	}

	@Override
	public @NotNull Set<String> getLicense() {
		return new HashSet<>();
	}

	@Override
	public @NotNull Map<String, String> getLinks() {
		return new HashMap<>();
	}

	@Override
	public boolean isReal() {
		return false;
	}
}
