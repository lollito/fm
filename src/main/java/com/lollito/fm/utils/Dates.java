package com.lollito.fm.utils;

import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ThreadLocalRandom;


public class Dates {
	public static final int MILLISECOND = Calendar.MILLISECOND;
	public static final int SECOND = Calendar.SECOND;
	public static final int MINUTE = Calendar.MINUTE;
	public static final int HOUR = Calendar.HOUR;
	public static final int HOUR_OF_DAY = Calendar.HOUR_OF_DAY;
	public static final int DATE = Calendar.DATE;
	public static final int MONTH=Calendar.MONTH;
	public static final int YEAR = Calendar.YEAR;
	public static final int WEEK_OF_YEAR = Calendar.WEEK_OF_YEAR;
	
	private static long[] dividers;
	static {
		dividers = new long[20];
		dividers[ MILLISECOND ] = 1;
		dividers[ SECOND ] = 1000;
		dividers[ MINUTE ] = 60 * 1000;
		dividers[ HOUR ] = 60 * 60 * 1000;
		dividers[ DATE ] = 24 * 60 * 60 * 1000;
	}
	
	public static Date add( Date from, int field, int amount ) {
		Calendar fromCal = new GregorianCalendar();
		fromCal.setTime( from );
		fromCal.add( field, amount );
		return fromCal.getTime();
	}

	public static Date subtract( Date from, int field, int amount ) {
		return add( from, field, amount * -1 );
	}

	public static long diff(Date from, Date to, int field ) {
		long divider = dividers[ field ];
		if( divider != 0 ) {
			return ( to.getTime() - from.getTime() ) / divider; 
		} else {
			throw new RuntimeException( "Unsupported difference field" );
		}
	}
	
	public static Date addDays(Date data,int days){
		Calendar cal = Calendar.getInstance();
	    cal.setTime(data);
	    cal.add(Calendar.DATE, days); 
	    cal.set(Calendar.HOUR_OF_DAY,0);
	    cal.set(Calendar.MINUTE,0);
	    cal.set(Calendar.SECOND,0);
	    cal.set(Calendar.MILLISECOND,0);
	    return cal.getTime();
	}
	
	public static int getFieldValue(Date date,int field){
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(field);
	}

	public static Date create( int year, int month, int day ) {
		return new GregorianCalendar( year, month-1, day ).getTime();
	}
	
	public static int compareByDateOnly( Date a, Date b) {
		
		Calendar calA = Calendar.getInstance();
		calA.setTime( a );
		calA.set( MILLISECOND, 0 );
		calA.set( SECOND, 0 );
		calA.set( MINUTE, 0 );
		calA.set( HOUR, 0 );
		calA.set( HOUR_OF_DAY ,0);
		
		Calendar calB = Calendar.getInstance();
		calB.setTime( b );
		calB.set( MILLISECOND, 0 );
		calB.set( SECOND, 0 );
		calB.set( MINUTE, 0 );
		calB.set( HOUR, 0 );
		calB.set( HOUR_OF_DAY ,0);
		
		return calA.compareTo( calB );
	}

	public static Date now() {
		return new Date();
	}

	public static Date today() {
		Calendar cal = Calendar.getInstance();
		cal.set( MILLISECOND, 0 );
		cal.set( SECOND, 0 );
		cal.set( MINUTE, 0 );
		cal.set( HOUR, 0 );
		return cal.getTime();
	}
	
	public static Date tomorrow() {
		return addDays(now(), 1);
	}
	
	public static Date yesterday() {
		Calendar cal = Calendar.getInstance();
		cal.set( MILLISECOND, 0 );
		cal.set( SECOND, 0 );
		cal.set( MINUTE, 0 );
		cal.set( HOUR, 0 );
		
		cal.add(DATE, -1);
		
		return cal.getTime();
	}

	public static int currentYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}
	
	public static LocalDate generateRandomDate(){
		LocalDate fromDate = LocalDate.of( 1980 , Month.JANUARY , 1 ); 
		LocalDate toDate = LocalDate.of( 2000 , Month.DECEMBER , 31 ); 
		LocalDate birth = LocalDate.ofEpochDay(ThreadLocalRandom.current().longs(fromDate.toEpochDay(), toDate.toEpochDay()).findAny().getAsLong());
//		return Date.from(birth.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		return birth;
	}
}
