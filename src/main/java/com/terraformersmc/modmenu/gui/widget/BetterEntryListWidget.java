package com.terraformersmc.modmenu.gui.widget;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class BetterEntryListWidget<E extends BetterEntryListWidget.Entry<E>> extends EntryListWidget implements Renderable {
	private final List<E> children = new Entries();
	@Nullable
	private E selected;
	@Nullable
	private E hoveredEntry;
	@Nullable
	private E focused;

	public BetterEntryListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int itemHeight ) {
		super( minecraftClient, width, height, top, bottom, itemHeight );
	}

	@Override
	public void render( int mouseX, int mouseY, float delta ) {

		if (this.getEntryCount() > 0) {
			super.render( mouseX, mouseY, delta );
		}

		this.hoveredEntry = findEntryAt( mouseX, mouseY );
	}

	public void setSelected(@Nullable E entry ) {
		this.selected = entry;
	}

	@Nullable
	public E getSelectedOrNull() {
		return this.selected;
	}

	@Nullable
	public E getHoveredEntryOrNull() {
		return this.hoveredEntry;
	}

	@Nullable
	public E getFocused() {
		return focused;
	}

	public void setFocused( @Nullable E focused ) {
		this.focused = focused;
	}

	public final List<E> children() {
		return this.children;
	}

	protected final void clearEntries() {
		this.children.clear();
	}

	protected void replaceEntries( Collection<E> newEntries ) {
		this.children.clear();
		this.children.addAll( newEntries );
	}

	@Override
	public E getEntry( int index ) {
		return this.children().get( index );
	}

	protected int addEntry( E entry ) {
		this.children.add( entry );
		return this.children.size() - 1;
	}

	@Override
	protected int getEntryCount() {
		return this.children().size();
	}

	protected boolean isSelectedEntry( int index ) {
		return Objects.equals( this.getSelectedOrNull(), this.children().get( index ) );
	}

	public @Nullable E findEntryAt( int x, int y ) {
		int index = getEntryAt( x, y );
		return index != -1 ? this.getEntry( index ) : null;
	}

	@Nullable
	public E remove( int index ) {
		return this.removeEntry( this.children.get( index ) ) ?
			this.children.get( index ) :
			null;
	}

	protected boolean removeEntry( E entry ) {
		boolean bl = this.children.remove( entry );
		if ( bl && entry == this.getSelectedOrNull() )
			this.setSelected( null );

		return bl;
	}

	public boolean isMouseOver( double mouseX, double mouseY ) {
		return mouseY >= (double) this.yStart &&
			mouseY <= (double) this.yEnd &&
			mouseX >= (double) this.xStart &&
			mouseX <= (double) this.xStart;
	}

	void setEntryParentList( Entry<E> entry ) {
		entry.parentList = this;
	}

	class Entries extends AbstractList<E> {
		private final List<E> entries = Lists.newArrayList();

		Entries() {
		}

		@Override
		public E get( int i ) {
			return this.entries.get( i );
		}

		@Override
		public int size() {
			return this.entries.size();
		}

		@Override
		public E set( int i, E entry ) {
			E entry2 = this.entries.set( i, entry );
			BetterEntryListWidget.this.setEntryParentList( entry );
			return entry2;
		}

		@Override
		public void add( int i, E entry ) {
			this.entries.add( i, entry );
			BetterEntryListWidget.this.setEntryParentList( entry );
		}

		@Override
		public E remove( int i ) {
			return this.entries.remove( i );
		}
	}

	public static abstract class Entry<E extends Entry<E>> implements EntryListWidget.Entry {
		BetterEntryListWidget<E> parentList;

		public Entry() {
		}

		public abstract void render( int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta );

		public boolean isMouseOver( int mouseX, int mouseY ) {
			return Objects.equals( this.parentList.findEntryAt( mouseX, mouseY ), this );
		}
	}

}
