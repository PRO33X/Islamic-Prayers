package org.tamrah.islamic.hijri;

import java.util.Calendar;

public interface Hijri {
	/**
	 * 
	 */
	static final int AH = 1;
	
	/**
	 * 
	 */
	static final int BH = 0;
	
	/**
	 * Value of the {@link #MONTH} field indicating the
	 * first month of the year in the Hijri calendar.
	 */
	public final static int MUHARRAM = 1;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * second month of the year in the Hijri calendar.
	 */
	public final static int SAFAR = 2;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * third month of the year in the Hijri calendars.
	 */
	public final static int RABI_I = 3;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * fourth month of the year in the Hijri calendar.
	 */
	public final static int RABI_II = 4;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * fifth month of the year in the Hijri calendar.
	 */
	public final static int JUMADA_I = 5;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * sixth month of the year in the Hijri calendar.
	 */
	public final static int JUMADA_II = 6;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * seventh month of the year in the Hijri calendar.
	 */
	public final static int RAJAB = 7;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * eighth month of the year in the Hijri calendar.
	 */
	public final static int SHAABAN = 8;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * ninth month of the year in the Hijri calendar.
	 */
	public final static int RAMADAN = 9;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * tenth month of the year in the Hijri calendar.
	 */
	public final static int SHAWWAL = 10;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * eleventh month of the year in the Hijri calendar.
	 */
	public final static int DHU_AL_QIDAH = 11;

	/**
	 * Value of the {@link #MONTH} field indicating the
	 * twelfth month of the year in the Hijri calendar.
	 */
	public final static int DHU_AL_HIJJAH = 12;
	public Calendar toGregorianCalendar();
}