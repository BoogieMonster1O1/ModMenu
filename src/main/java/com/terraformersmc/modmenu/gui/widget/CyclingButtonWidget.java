package com.terraformersmc.modmenu.gui.widget;

import com.google.common.collect.ImmutableList;
import com.terraformersmc.modmenu.util.ScreenTexts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class CyclingButtonWidget<T> extends AbstractButtonWidget {
	static final BooleanSupplier HAS_ALT_DOWN = Screen::hasAltDown;
	private static final List<Boolean> BOOLEAN_VALUES = ImmutableList.of( Boolean.TRUE, Boolean.FALSE );
	private final Text optionText;
	private int index;
	private T value;
	private final Values<T> values;
	private final Function<T, Text> valueToText;
	private final UpdateCallback<T> callback;
	private final TooltipFactory<T> tooltipFactory;
	private final boolean optionTextOmitted;

	CyclingButtonWidget(
		int id,
		int x,
		int y,
		int width,
		int height,
		Text message,
		Text optionText,
		int index,
		T value,
		Values<T> values,
		Function<T, Text> valueToText,
		UpdateCallback<T> callback,
		TooltipFactory<T> tooltipFactory,
		boolean optionTextOmitted
	) {
		super( id, x, y, width, height, optionText, null );
		this.setOnPress( this::onPress );
		this.optionText = optionText;
		this.index = index;
		this.value = value;
		this.values = values;
		this.valueToText = valueToText;
		this.callback = callback;
		this.tooltipFactory = tooltipFactory;
		this.optionTextOmitted = optionTextOmitted;
	}

	public void onPress( AbstractButtonWidget button ) {
		if ( Screen.hasShiftDown() ) {
			this.cycle( -1 );
		} else {
			this.cycle( 1 );
		}

	}

	private void cycle( int amount ) {
		List<T> list = this.values.getCurrent();
		this.index = MathHelper.floorMod( this.index + amount, list.size() );
		T object = list.get( this.index );
		this.internalSetValue( object );
		this.callback.onValueChange( this, object );
	}

	@SuppressWarnings("SameParameterValue")
	private T getValue( int offset ) {
		List<T> list = this.values.getCurrent();
		return list.get( MathHelper.floorMod( this.index + offset, list.size() ) );
	}

	@Override
	public void mouseScrolled( int mouseX, int mouseY, int amount ) {
		// amount is either -1 or 1
		this.cycle( amount );
	}

	@Override
	public String getMessage() {
		String name = I18n.translate( super.getMessage() );
		String value = I18n.translate(
			( (TranslatableText) this.optionText ).getKey() + "." +
				this.getValue().toString().toLowerCase( Locale.ROOT )
		);
		return name + ": " + value;
	}

	public void setValue( T value ) {
		List<T> list = this.values.getCurrent();
		int i = list.indexOf( value );
		if ( i != -1 )
			this.index = i;

		this.internalSetValue( value );
	}

	private void internalSetValue( T value ) {
		this.value = value;
	}

	private Text composeText( T value ) {
		return this.optionTextOmitted ? this.valueToText.apply( value ) : this.composeGenericOptionText( value );
	}

	private Text composeGenericOptionText( T value ) {
		return ScreenTexts.composeGenericOptionText( this.optionText, this.valueToText.apply( value ) );
	}

	public T getValue() {
		return this.value;
	}

	public List<Text> getTooltip() {
		return this.tooltipFactory.apply( this.value );
	}

	public static <T> Builder<T> builder( Function<T, Text> valueToText ) {
		return new Builder<>( valueToText );
	}

	public static Builder<Boolean> onOffBuilder( Text on, Text off ) {
		return new Builder<Boolean>( value -> value ? on : off ).values( BOOLEAN_VALUES );
	}

	public static Builder<Boolean> onOffBuilder() {
		return new Builder<Boolean>( value -> value ? ScreenTexts.ON : ScreenTexts.OFF ).values( BOOLEAN_VALUES );
	}

	public static Builder<Boolean> onOffBuilder( boolean initialValue ) {
		return onOffBuilder().initially( initialValue );
	}

	public static class Builder<T> {
		private int initialIndex;
		@Nullable
		private T value;
		private final Function<T, Text> valueToText;
		private TooltipFactory<T> tooltipFactory = value -> ImmutableList.of();
		private Values<T> values = Values.of( ImmutableList.of() );
		private boolean optionTextOmitted;

		public Builder( Function<T, Text> valueToText ) {
			this.valueToText = valueToText;
		}

		public Builder<T> values( List<T> values ) {
			this.values = Values.of( values );
			return this;
		}

		@SafeVarargs
		public final Builder<T> values( T... values ) {
			return this.values( ImmutableList.copyOf( values ) );
		}

		public Builder<T> values( List<T> defaults, List<T> alternatives ) {
			this.values = Values.of( CyclingButtonWidget.HAS_ALT_DOWN, defaults, alternatives );
			return this;
		}

		public Builder<T> values( BooleanSupplier alternativeToggle, List<T> defaults, List<T> alternatives ) {
			this.values = Values.of( alternativeToggle, defaults, alternatives );
			return this;
		}

		public Builder<T> tooltip( TooltipFactory<T> tooltipFactory ) {
			this.tooltipFactory = tooltipFactory;
			return this;
		}

		public Builder<T> initially( T value ) {
			this.value = value;
			int i = this.values.getDefaults().indexOf( value );
			if ( i != -1 ) {
				this.initialIndex = i;
			}

			return this;
		}

		public Builder<T> omitKeyText() {
			this.optionTextOmitted = true;
			return this;
		}

		public CyclingButtonWidget<T> build( int id, int x, int y, int width, int height, Text optionText ) {
			return this.build( id, x, y, width, height, optionText, ( button, value ) -> {
			} );
		}

		public CyclingButtonWidget<T> build( int id, int x, int y, int width, int height, Text optionText, UpdateCallback<T> callback ) {
			List<T> list = this.values.getDefaults();
			if ( list.isEmpty() ) {
				throw new IllegalStateException( "No values for cycle button" );
			} else {
				T object = this.value != null ? this.value : list.get( this.initialIndex );
				Text text = this.valueToText.apply( object );
				Text text2 = this.optionTextOmitted ? text : ScreenTexts.composeGenericOptionText( optionText, text );
				return new CyclingButtonWidget<>(
					id,
					x,
					y,
					width,
					height,
					text2,
					optionText,
					this.initialIndex,
					object,
					this.values,
					this.valueToText,
					callback,
					this.tooltipFactory,
					this.optionTextOmitted
				);
			}
		}
	}

	@FunctionalInterface
	public interface TooltipFactory<T> extends Function<T, List<Text>> {
	}

	@FunctionalInterface
	public interface UpdateCallback<T> {
		void onValueChange( CyclingButtonWidget<T> button, T value );
	}

	interface Values<T> {
		List<T> getCurrent();

		List<T> getDefaults();

		static <T> Values<T> of( List<T> values ) {
			final List<T> list = ImmutableList.copyOf( values );
			return new Values<T>() {
				@Override
				public List<T> getCurrent() {
					return list;
				}

				@Override
				public List<T> getDefaults() {
					return list;
				}
			};
		}

		static <T> Values<T> of( BooleanSupplier alternativeToggle, List<T> defaults, List<T> alternatives ) {
			final List<T> list = ImmutableList.copyOf( defaults );
			final List<T> list2 = ImmutableList.copyOf( alternatives );
			return new Values<T>() {
				@Override
				public List<T> getCurrent() {
					return alternativeToggle.getAsBoolean() ? list2 : list;
				}

				@Override
				public List<T> getDefaults() {
					return list;
				}
			};
		}
	}
}

