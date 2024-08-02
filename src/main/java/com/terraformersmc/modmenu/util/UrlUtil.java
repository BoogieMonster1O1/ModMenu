package com.terraformersmc.modmenu.util;

import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

public class UrlUtil {
	private static final Logger LOGGER = LogManager.getLogger( "Mod Menu | URL Utils" );

	public static OperatingSystem getOperatingSystem() {
		String string = System.getProperty( "os.name" ).toLowerCase( Locale.ROOT );
		if ( string.contains( "win" ) ) {
			return OperatingSystem.WINDOWS;
		} else if ( string.contains( "mac" ) ) {
			return OperatingSystem.OSX;
		} else if ( string.contains( "solaris" ) ) {
			return OperatingSystem.SOLARIS;
		} else if ( string.contains( "sunos" ) ) {
			return OperatingSystem.SOLARIS;
		} else if ( string.contains( "linux" ) ) {
			return OperatingSystem.LINUX;
		} else {
			return string.contains( "unix" ) ? OperatingSystem.LINUX : OperatingSystem.UNKNOWN;
		}
	}

	public enum OperatingSystem {
		LINUX( "linux" ),
		SOLARIS( "solaris" ),
		WINDOWS( "windows" ) {
			@Override
			protected String[] getURLOpenCommand( URL url ) {
				return new String[] { "rundll32", "url.dll,FileProtocolHandler", url.toString() };
			}
		},
		OSX( "mac" ) {
			@Override
			protected String[] getURLOpenCommand( URL url ) {
				return new String[] { "open", url.toString() };
			}
		},
		UNKNOWN( "unknown" );

		private final String name;

		OperatingSystem( String name ) {
			this.name = name;
		}

		public void open( URL url ) {
			try {
				Process process = AccessController.doPrivileged( (PrivilegedAction<Process>) () -> {
					try {
						return Runtime.getRuntime().exec( this.getURLOpenCommand( url ) );
					} catch ( IOException e ) {
						LOGGER.error( "Couldn't open url '{}'", url, e );
					}
					return null;
				} );
				if ( process == null )
					return;
				for ( String string : IOUtils.readLines( process.getErrorStream(), Charsets.UTF_8 ) )
					LOGGER.error( string );

				process.getInputStream().close();
				process.getErrorStream().close();
				process.getOutputStream().close();
			} catch ( IOException e ) {
				LOGGER.error( "Couldn't open url '{}'", url, e );
			}

		}

		public void open( URI uri ) {
			try {
				this.open( uri.toURL() );
			} catch ( MalformedURLException var3 ) {
				LOGGER.error( "Couldn't open uri '{}'", uri, var3 );
			}

		}

		public void open( File file ) {
			try {
				this.open( file.toURI().toURL() );
			} catch ( MalformedURLException var3 ) {
				LOGGER.error( "Couldn't open file '{}'", file, var3 );
			}

		}

		protected String[] getURLOpenCommand( URL url ) {
			String string = url.toString();
			if ( "file".equals( url.getProtocol() ) ) {
				string = string.replace( "file:", "file://" );
			}

			return new String[] { "xdg-open", string };
		}

		public void open( String uri ) {
			try {
				this.open( new URI( uri ).toURL() );
			} catch ( MalformedURLException | IllegalArgumentException | URISyntaxException var3 ) {
				LOGGER.error( "Couldn't open uri '{}'", uri, var3 );
			}

		}

		public String getName() {
			return this.name;
		}
	}
}
