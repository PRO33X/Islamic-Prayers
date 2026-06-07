package org.tamrah.islamic.hijri;

import java.util.Calendar;
import java.util.Locale;


// Original author: abdullah alfadhel
// HijraCalendar class is implementation of Muayyad Saleh Alsadi Hijra http://git.ojuba.org/cgit/hijra



public class HijraCalendar extends Calendar implements Hijri {
	// Proclaim serialization
	private static final long serialVersionUID = -602763277873317926L;
	protected static final int CONTS_P = 191;
	protected static final int CONTS_Q = 360;
	protected static final int CONTS_A = 48;
	//Julian 0622-7-16 = gregorian 0759-6-11 (I think it should be 622, 7, 19)
	protected static final int HIJRI_EPOCH = 227015;
	
	/**
	 * Gets a hijri calendar using the default time zone and locale. The
	 * <code>HijraCalendar</code> returned is based on the current time
	 * in the default time zone with the default locale.
	 *
	 * @return a HijraCalendar.
	 */
	public static HijraCalendar getInstance(){
		return new HijraCalendar(Calendar.getInstance());
	}
	
	//
	public static HijraCalendar getInstance(Locale locale){
		return new HijraCalendar(Calendar.getInstance(locale));
	}
	
	public HijraCalendar(Calendar calendar){
		gregorianToHijri(calendar);
	}
	/**
	 * 
	 * @param year Hijri year
	 * @param month Hijri month
	 * @param day Hijri day
	 */
	public HijraCalendar(int year, int month, int day){
		fields[ERA] = AH;
		fields[YEAR] = year;
		fields[MONTH] = month;
		set(DAY_OF_MONTH, day);
	}
	
	@Override
	public Calendar toGregorianCalendar(){
		return hijriToGregorian(internalGet(YEAR), internalGet(MONTH), internalGet(DAY_OF_MONTH));
	}
	
	/**
	 * 
	 * @param year Hijri year
	 * @param month Hijri month
	 * @return the number of days in a given hijri month in a given
	 */
	protected int getHijriMonthDays(int year, int month){
		int Mc = ( year -1) *12 + month;
		if ((((Mc+ CONTS_A) * CONTS_P) % CONTS_Q)  < CONTS_P) 
			return 30;
		else
			return 29;
	}
	
	/**
	 * 
	 * @param year Hijri year
	 * @param month Hijri month
	 * @return the number of days before a given moth in a given year (0 for month=1)
	 */
	protected int getHijriDaysBeforeMonth(int year, int month){
		int Mc = ( year -1) *12 + 1 + CONTS_A;
		int McM = Mc * CONTS_P;
		int sum = 0;
		for (int i = 1; i < month; i++) {
			if ((McM % CONTS_Q)  < CONTS_P)
				sum+=30;
			else
				sum+=29;
			McM+=CONTS_P;
		}
		return sum;
	}
	
	/**
	 * 
	 * @param year Hijri year
	 * @return the number of days in a given year
	 */
	protected int getHijriYearDays(int year){
		return getHijriDaysBeforeMonth(year,13);
	}
	
	/**
	 * 
	 * @param year Hijri year
	 * @param month Hijri month
	 * @param day Hijri day
	 * @return the day number within the year of the Islamic date (year, month, day), 1 for 1/1 in any year
	 */
	protected int getHijriDayOfYear(int year, int month, int day){
		return getHijriDaysBeforeMonth(year, month) + day;
	}
	
	/**
	 * 
	 * @param year Hijri year
	 * @param month Hijri month
	 * @param day Hijri day
	 * @return absolute date of Hijri (year, month, day), eg. ramadan (9),1,1427 -> 732578
	 */
	protected int hijriToAbsolute(int year, int month, int day){
		int Mc = (year - 1) * 12;
		// day count=days before Hijra plus (...)
		int dc = HIJRI_EPOCH;
		// plus days in the years before till first multiples of q plus (...)
		Mc-=Mc % CONTS_Q;
		int y = year - Mc/12;
		dc += (Mc*29) + (Mc*CONTS_P/CONTS_Q);
		// plus those after the multiples plus (...)
		for (int i = 1; i < y; i++)
			dc += getHijriYearDays(i);
		// plus days from the begining of that year
		dc += getHijriDayOfYear (year, month, day) - 1;
		return dc;
	}
	
	/**
	 * Hijri date (Y,M,D) corresponding to the given absolute number of days.
	 * @param date absolute number of days
	 */
	protected void absoluteToHijri(int date){
		if(date < HIJRI_EPOCH) //pre-Islamic date
			return;
		int Mc = (date-HIJRI_EPOCH+1)*CONTS_Q/(29*CONTS_Q+CONTS_P);
		int mYEAR = Mc/12+1;
		int mMONTH = (Mc%12)+1;
		// consistency check
		int d = hijriToAbsolute(mYEAR,mMONTH,1); //TODO: this is an expensive call
		if (date < d){ // go one month back if needed
				mMONTH-=1;
			if (mMONTH==0){
				mYEAR-=1;
				mMONTH=DHU_AL_HIJJAH;
			}
			int monthDays = getHijriMonthDays(mYEAR,mMONTH);
			d-=monthDays; // this call is fast
		}
		//
		int mDAY = 1 + date - d;
		fields[ERA] = AH;
		fields[YEAR] = mYEAR;
		fields[MONTH] = mMONTH;
		fields[DAY_OF_MONTH] = mDAY;
		computeFields();
	}
	
	/**
	 * 
	 * @param year
	 * @return 1 (True) if YEAR is a Gregorian leap year
	 */
	protected int isGregorianLeapYear(int year){
		if ((year % 4) == 0 && ((year % 100) == 0 || (year % 400) == 0))
			return 1;
		return 0;
	}
	
	//Length of months (in days) in Gregorian calendar 
	int [] days_in_month = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	
	/**
	 * 
	 * @param year
	 * @param month
	 * @return The last day in MONTH during YEAR
	 */
	protected int getGregorianMonthDays(int year, int month){
		if (month == 2 && isGregorianLeapYear(year) == 1)
			return 29;
		return days_in_month[month-1];
	}
	
	/**
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return the day number within the year of the date (year,month, day)
	 */
	protected int getGergorianDayNumber(int year, int month, int day){
		if (month<3) 
			 return day + (31 * (month - 1));
		return day + (31 * (month - 1)) - 
				   ((month << 2) + 23) / 10 + (isGregorianLeapYear(year));
	}
	
	protected int gregorianToAbsolute(int year, int month, int day){
		int prior_years = year - 1;
	   return getGergorianDayNumber (year, month, day) + 
		   365 * prior_years + (prior_years >> 2) - 
		   (prior_years / 100) + (prior_years / 400);
	}
	
	/**
	 * (year month day) corresponding to the absolute DATE.
		The absolute date is the number of days elapsed since the (imaginary)
		Gregorian date Sunday, December 31, 1 BC.
	 * @param date
	 */
	protected Calendar absoluteToGregorian(int date){
		Calendar calendar = Calendar.getInstance();
		int d0 = date - 1;
		int n400 = d0 / 146097;
		int d1 = d0 % 146097;
		int n100 = d1 / 36524;
		int d2 = d1 % 36524;
		int n4 = d2 / 1461;
		int d3 = d2 % 1461;
		int n1 = d3 / 365;
//		int dd = (d3 % 365) + 1;
		int yy = ((400 * n400) + (100 * n100) + (n4 * 4) + n1);
		if ((n100 == 4) || (n1 == 4)){
			calendar.set(Calendar.YEAR, yy);
			calendar.set(Calendar.MONTH, Calendar.DECEMBER);
			calendar.set(Calendar.DAY_OF_MONTH, 31);
		}else{
			yy=yy+1;
			int mm = 1;
			while(date >= gregorianToAbsolute (yy,mm, 1)){
				mm+=1;
			}
			int d=gregorianToAbsolute (yy, mm-1, 1);
			calendar.set(Calendar.YEAR, yy);
			calendar.set(Calendar.MONTH, mm-2);
			calendar.set(Calendar.DAY_OF_MONTH, date-d+1);
		}
		//Set time
		for (int i = AM_PM; i < FIELD_COUNT; i++) {
			calendar.set(i, fields[i]);
		}
		return calendar;
	}
	
	/**
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return the day-of-the-week index of hijri (year,month,day) Date, 1 for Sunday, 2 for Monday, etc.
	 */
	protected int getHijriDayOfWeek(int year, int month, int day){
		return hijriToAbsolute(year, month, day) % 7 + 2;
	}
	
	/**
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return the day-of-the-week index of gregorian (year, month, day) DATE, 1 for Sunday, 2 for Monday, etc.
	 */
	protected int getGregorianDayOfWeek(int year, int month, int day){
		return gregorianToAbsolute (year,month, day) % 7 + 1;
	}
	
	/**
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return gregorian (year, month, day) converted from Islamic Hijri calender
	 */
	protected Calendar hijriToGregorian(int year, int month, int day){
		return absoluteToGregorian( hijriToAbsolute (year, month, day));
	}
	
	/**
	 * Hijri  (year, month, day) converted from gregorian calender
	 * @param year
	 * @param month
	 * @param day
	 */
	protected void gregorianToHijri(int year, int month, int day){
		absoluteToHijri(gregorianToAbsolute(year, month, day));
	}
	
	/**
	 * Hijri  (year, month, day) converted from gregorian calender
	 * @param calendar 
	 */
	protected void gregorianToHijri(Calendar calendar){
		absoluteToHijri(gregorianToAbsolute(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH)));
		//Set time
		for (int i = AM_PM; i < FIELD_COUNT; i++) {
			fields[i] = calendar.get(i);
		}
	}
	
	@Override
	public void set(int field, int value) {
		if(field == YEAR && value < 0){
			fields[YEAR] = -value;
			fields[ERA] = BH;
		}else
			fields[field] = value;
		if(field == YEAR || field == MONTH || field == DATE)
			computeFields();
	}
	
	@Override
	public void add(int field, int amount) {
		// If amount == 0, do nothing even the given field is out of
		if (amount == 0) {
			return;   // Do nothing!
		}

		if (field < 0 || field >= ZONE_OFFSET) {
			throw new IllegalArgumentException();
		}
		
		switch (field) {
		case DAY_OF_MONTH:
			Calendar calendarDAY_OF_MONTH = toGregorianCalendar();
			calendarDAY_OF_MONTH.add(DAY_OF_MONTH, amount);
			gregorianToHijri(calendarDAY_OF_MONTH);
			break;
		case DAY_OF_YEAR:
			Calendar calendarDAY_OF_YEAR = toGregorianCalendar();
			calendarDAY_OF_YEAR.add(DAY_OF_YEAR, amount);
			gregorianToHijri(calendarDAY_OF_YEAR);
			break;
		case MONTH:
			int month = internalGet(MONTH) + amount;
			int year = (month-1) / getMaximum(MONTH);
			if(amount > 0){
				//PLUS
				fields[YEAR] += year;
				if(month % getMaximum(MONTH) == 0)
					set(MONTH, DHU_AL_HIJJAH);
				else
					set(MONTH, month % getMaximum(MONTH));
			}else{
				//MINUS
				if(month > 0){
					set(MONTH, month);
				}else{
					year--;
					fields[YEAR] += year;
					set(MONTH, getMaximum(MONTH) + (month % getMaximum(MONTH)));
				}
			}
			if(internalGet(DATE) == 30 && getHijriMonthDays(internalGet(YEAR), internalGet(MONTH)) != 30)
				set(DATE, 29);
			break;
		default:
			set(field, internalGet(field) + amount);
			break;
		}
	}

	@Override
	protected void computeFields() {
		fields[DAY_OF_WEEK] = getHijriDayOfWeek(internalGet(YEAR), internalGet(MONTH), internalGet(DATE));
		fields[WEEK_OF_MONTH] = internalGet(DATE)/7 + (internalGet(DATE)%7>0?1:0);
		fields[DAY_OF_YEAR] = getHijriDayOfYear(internalGet(YEAR), internalGet(MONTH), internalGet(DATE));
		fields[WEEK_OF_YEAR] = internalGet(DAY_OF_YEAR)/7 + (internalGet(DAY_OF_YEAR)%7>0?1:0);
	}

	@Override
	protected void computeTime() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getGreatestMinimum(int field) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLeastMaximum(int field) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaximum(int field) {
		int ret = 0;
		switch (field) {
		case DAY_OF_MONTH:
			ret = 30;
			break;
		case DAY_OF_WEEK:
			ret = 7;
		case MONTH:
			ret = 12;
			break;
		case YEAR:
			ret = 9999;
			break;
//		case ERA:
//			ret = AH;
//			break;
		}
		return ret;
	}

	@Override
	public int getMinimum(int field) {
		int ret = 0;
		switch (field) {
		case DAY_OF_MONTH:
		case DAY_OF_WEEK:
		case MONTH:
			ret = 1;
			break;
		case YEAR:
			ret = 0;
			break;
//		case ERA:
//			ret = BH;
		}
		return ret;
	}
	
	@Override
	public int getActualMaximum(int field) {
		int ret = 0;
		switch (field) {
		case DAY_OF_MONTH:
			ret = getHijriMonthDays(get(YEAR), get(MONTH));
			break;
		case DAY_OF_WEEK:
			ret = 7;
		case MONTH:
			ret = 12;
			break;
		case YEAR:
			ret = 9999;
			break;
//		case ERA:
//			ret = AH;
//			break;
		}
		return ret;
	}

	@Override
	public void roll(int field, boolean up) {
		// TODO Auto-generated method stub

	}

}