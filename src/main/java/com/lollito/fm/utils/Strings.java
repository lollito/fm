package com.lollito.fm.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {

	private static Random random;

	static {
		random = new Random();
		random.setSeed( new Date().getTime() );
	}

	public static String generateHexId ( int length ) {

		int len = Math.abs( length );
		if ( len < 1 ) {
			len = 1;
		} else if ( len > 1024 ) {
			len = 1024;
		}

		StringBuilder result = new StringBuilder( len + 32 );
		while ( result.length() < len ) {
			result.append( Long.toHexString( random.nextLong() ) );
		}

		result.setLength( len );
		return result.toString();
	}

	public String trim(String string, char ch) {
		char value[] = string.toCharArray();
        int len = value.length;
        int st = 0;
        char[] val = value;    
        while ((st < len) && (val[st] <= ch)) {
            st++;
        }
        while ((st < len) && (val[len - 1] <= ch)) {
            len--;
        }
        return ((st > 0) || (len < value.length)) ? string.substring(st, len) : string;
    }
	
	public static List<String> matchAll ( String pattern, String string ) {
		Pattern p = Pattern.compile( pattern );
		Matcher m = p.matcher( string );
		List<String> results = new ArrayList<String>();
		while ( m.find() ) {
			results.add( m.group() );
		}
		return results;
	}

	public static String replaceString ( String origin, String pattern, String substPattern ) {
		if ( pattern == null || "".equals( pattern ) || substPattern == null || origin == null || origin.equals( "" ) ||
		     origin.indexOf( pattern ) == -1 ) {
			return origin;
		}

		int idxStart = origin.indexOf( pattern );
		StringBuffer iniziale = new StringBuffer( origin );
		do {
			iniziale.replace( idxStart, idxStart + pattern.length(), substPattern );
			idxStart = idxStart + substPattern.length();
			idxStart = iniziale.toString().indexOf( pattern, idxStart );
		} while ( idxStart != -1 );
		return iniziale.toString();
	}

	public static boolean isEmpty( String string ) {
		return( string == null || "".equals( string ) );
	}
	
	public static boolean isEmptyTrimmed( String string ) {
		return( string == null || "".equals( string.trim() ) );
	}
	
	public static boolean hasText( String string ) {
		return !isEmptyTrimmed( string );
	}

	public static String defaultValue( String string, String defaultValue ) {
		if( isEmpty( string ) ) {
			return defaultValue;
		} else {
			return string;
		}
	}

	public static String capitalizeFirst( String string ) {
		if( hasText( string ) ) {
			if( string.length() == 1 ) {
				return string.toUpperCase();
			} else {
				return string.substring( 0, 1 ).toUpperCase() + string.substring( 1 );
			}
		} else {
			return string;
		}
	}
	
	public static String lowerCaseFirst( String string ) {
		if( hasText( string ) ) {
			if( string.length() == 1 ) {
				return string.toLowerCase();
			} else {
				return string.substring( 0, 1 ).toLowerCase() + string.substring( 1 );
			}
		} else {
			return string;
		}
	}
	
	public static String defaultValue( Object string, Object defaultValue ) {
		return defaultValue( (String)string, (String)defaultValue );
	}

	public static String fillLeft( int maxLength, char fill, String source  ) {
		return fill( maxLength, fill, true, source  );
	}

	public static String fillRight( int maxLength, char fill, String source  ) {
		return fill( maxLength, fill, false, source  );
	}

	private static String fill( int maxLength, char fill, boolean left, String source  ) {

		if ( source == null || source.length() >= maxLength  ){
			return source;
		}

		StringBuilder result = new StringBuilder(maxLength);
		if ( !left ) {
			result.append( source );
		}

		for ( int len = source.length(); len < maxLength; len++ ) {
			result.append( fill );
		}

		if ( left ) {
			result.append( source );
		}

		return result.toString();
	}
}


