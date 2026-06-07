package org.tamrah.islamic.pray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class PrayTime {
	//Date
	Calendar calendar;
	//Times
	private Date fajr;
	private Date sunrise;
	private Date dhuhr;
	private Date asr;
	private Date sunset;
	private Date maghrib;
	private Date isha;
	private List<Date> prayerTimes;
	// ---------------------- Global Variables --------------------
	private CalculationMethod calcMethod; // caculation method
	private JuristicMethod asrJuristic; // Juristic method for Asr
	private int dhuhrMinutes; // minutes after mid-day for Dhuhr
	private int adjustHighLats; // adjusting method for higher latitudes
	private double lat; // latitude
	private double lng; // longitude
	private double timeZone; // time-zone
	private double JDate; // Julian date
	// ------------------------------------------------------------
	// Adjusting Methods for Higher Latitudes
	private int None; // No adjustment
	private int MidNight; // middle of night
	private int OneSeventh; // 1/7th of night
	private int AngleBased; // angle/60th of night
	// Time Names
	// private ArrayList<String> timeNames;
	// --------------------- Technical Settings --------------------
	private int numIterations; // number of iterations needed to compute times
	// ------------------- Calc Method Parameters --------------------
	private HashMap<Integer, double[]> methodParams;

	/*
	 * this.methodParams[methodNum] = new Array(fa, ms, mv, is, iv);
	 *
	 * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
	 * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
	 * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter value
	 * (in angle or minutes)
	 */
	private int[] offsets;

	public PrayTime(Calendar date, double latitude,
			double longitude, TimeZone timeZone, 
			CalculationMethod calculationMethod,
			JuristicMethod juristicMethods){
		this.calendar = date;
		this.lat = latitude;
		this.lng = longitude;
		this.timeZone = getOffset(timeZone);
		this.calcMethod = calculationMethod;
		this.asrJuristic = juristicMethods;
		init();
	}
	//
	private void init(){
		// Initialize vars
		this.setDhuhrMinutes(0);
		this.setAdjustHighLats(1);
		// Adjusting Methods for Higher Latitudes
		None = 0; // No adjustment
		MidNight = 1; // middle of night
		OneSeventh = 2; // 1/7th of night
		AngleBased = 3; // angle/60th of night
		
		//http://moonsighting.com/how-we.html
		if(this.lat >= 48.5)
			this.setAdjustHighLats(3);

		// --------------------- Technical Settings --------------------

		numIterations = 1; // number of iterations needed to compute
		// times

		// ------------------- Calc Method Parameters --------------------

		// Tuning offsets {fajr, sunrise, dhuhr, asr, sunset, maghrib, isha}
		offsets = new int[7];

		/*
		 *
		 * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
		 * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
		 * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter
		 * value (in angle or minutes)
		 */
		methodParams = new HashMap<Integer, double[]>();

		// Jafari
		double[] Jvalues = {16,0,4,0,14};
		methodParams.put(CalculationMethod.Jafari.getId(), Jvalues);

		// Karachi
		double[] Kvalues = {18,1,0,0,18};
		methodParams.put(CalculationMethod.Karachi.getId(), Kvalues);

		// ISNA
		double[] Ivalues = {15,1,0,0,15};
		methodParams.put(CalculationMethod.ISNA.getId(), Ivalues);

		// MWL
		double[] MWvalues = {18,1,0,0,17};
		methodParams.put(CalculationMethod.MWL.getId(), MWvalues);

		// Makkah
		double[] MKvalues = {18.5,1,0,1,90};
		methodParams.put(CalculationMethod.Makkah.getId(), MKvalues);

		// Egypt
		double[] Evalues = {19.5,1,0,0,17.5};
		methodParams.put(CalculationMethod.Egypt.getId(), Evalues);

		// Tehran
		double[] Tvalues = {17.7,0,4.5,0,14};
		methodParams.put(CalculationMethod.Tehran.getId(), Tvalues);

		// Custom
		double[] Cvalues = {18,1,0,0,17};
		methodParams.put(CalculationMethod.Custom.getId(), Cvalues);
		
		//
		computeDayTimes();
	}

	// ---------------------- Trigonometric Functions -----------------------
	// range reduce angle in degrees.
	private double fixangle(double a) {

		a = a - (360 * (Math.floor(a / 360.0)));

		a = a < 0 ? (a + 360) : a;

		return a;
	}

	// range reduce hours to 0..23
	private double fixhour(double a) {
		a = a - 24.0 * Math.floor(a / 24.0);
		a = a < 0 ? (a + 24) : a;
		return a;
	}

	// radian to degree
	private double radiansToDegrees(double alpha) {
		return ((alpha * 180.0) / Math.PI);
	}

	// deree to radian
	private double DegreesToRadians(double alpha) {
		return ((alpha * Math.PI) / 180.0);
	}

	// degree sin
	private double dsin(double d) {
		return (Math.sin(DegreesToRadians(d)));
	}

	// degree cos
	private double dcos(double d) {
		return (Math.cos(DegreesToRadians(d)));
	}

	// degree tan
	private double dtan(double d) {
		return (Math.tan(DegreesToRadians(d)));
	}

	// degree arcsin
	private double darcsin(double x) {
		double val = Math.asin(x);
		return radiansToDegrees(val);
	}

	// degree arccos
	private double darccos(double x) {
		double val = Math.acos(x);
		return radiansToDegrees(val);
	}

	// degree arctan
	private double darctan(double x) {
		double val = Math.atan(x);
		return radiansToDegrees(val);
	}

	// degree arctan2
	private double darctan2(double y, double x) {
		double val = Math.atan2(y, x);
		return radiansToDegrees(val);
	}

	// degree arccot
	private double darccot(double x) {
		double val = Math.atan2(1.0, x);
		return radiansToDegrees(val);
	}

	// ---------------------- Time-Zone Functions ----------------------
	private double getOffset(TimeZone timeZone) {
        int milliSecHour = 3600000;
        double hoursDiff = timeZone.getRawOffset() / (double) milliSecHour;
        if(timeZone.inDaylightTime(calendar.getTime()))
            hoursDiff += timeZone.getDSTSavings() / (double) milliSecHour;
        return hoursDiff;
    }


	// ---------------------- Julian Date Functions -----------------------
	// calculate julian date from a calendar date
	private double julianDate(int year, int month, int day) {

		if (month <= 2) {
			year -= 1;
			month += 12;
		}
		double A = Math.floor(year / 100.0);

		double B = 2 - A + Math.floor(A / 4.0);

		double JD = Math.floor(365.25 * (year + 4716))
				+ Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;

		return JD;
	}

	// ---------------------- Calculation Functions -----------------------
	// References:
	// http://www.ummah.net/astronomy/saltime
	// http://aa.usno.navy.mil/faq/docs/SunApprox.html
	// compute declination angle of sun and equation of time
	private double[] sunPosition(double jd) {

		double D = jd - 2451545;
		double g = fixangle(357.529 + 0.98560028 * D);
		double q = fixangle(280.459 + 0.98564736 * D);
		double L = fixangle(q + (1.915 * dsin(g)) + (0.020 * dsin(2 * g)));

		// double R = 1.00014 - 0.01671 * [self dcos:g] - 0.00014 * [self dcos:
		// (2*g)];
		double e = 23.439 - (0.00000036 * D);
		double d = darcsin(dsin(e) * dsin(L));
		double RA = (darctan2((dcos(e) * dsin(L)), (dcos(L))))/ 15.0;
		RA = fixhour(RA);
		double EqT = q/15.0 - RA;
		double[] sPosition = new double[2];
		sPosition[0] = d;
		sPosition[1] = EqT;

		return sPosition;
	}

	// compute equation of time
	private double equationOfTime(double jd) {
		double eq = sunPosition(jd)[1];
		return eq;
	}

	// compute declination angle of sun
	private double sunDeclination(double jd) {
		double d = sunPosition(jd)[0];
		return d;
	}

	// compute mid-day (Dhuhr, Zawal) time
	private double computeMidDay(double t) {
		double T = equationOfTime(this.getJDate() + t);
		double Z = fixhour(12 - T);
		return Z;
	}

	// compute time for a given angle G
	private double computeTime(double G, double t) {

		double D = sunDeclination(this.getJDate() + t);
		double Z = computeMidDay(t);
		double Beg = -dsin(G) - dsin(D) * dsin(this.getLat());
		double Mid = dcos(D) * dcos(this.getLat());
		double V = darccos(Beg/Mid)/15.0;

		return Z + (G > 90 ? -V : V);
	}

	// compute the time of Asr
	// Shafii: step=1, Hanafi: step=2
	private double computeAsr(double step, double t) {
		double D = sunDeclination(this.getJDate() + t);
		double G = -darccot(step + dtan(Math.abs(this.getLat() - D)));
		return computeTime(G, t);
	}

	// ---------------------- Misc Functions -----------------------
	// compute the difference between two times
	private double timeDiff(double time1, double time2) {
		return fixhour(time2 - time1);
	}

	// -------------------- Interface Functions --------------------

	// return prayer times for a given date
	public List<Date> getPrayerTimes() {
		return prayerTimes;
	}

	// set custom values for calculation parameters
	private void setCustomParams(double[] params) {

		for (int i = 0; i < 5; i++) {
			if (params[i] == -1) {
				params[i] = methodParams.get(this.getCalcMethod().getId())[i];
				methodParams.put(CalculationMethod.Custom.getId(), params);
			} else {
				methodParams.get(CalculationMethod.Custom.getId())[i] = params[i];
			}
		}
		this.setCalcMethod(CalculationMethod.Custom);
	}

	// set the angle for calculating Fajr
	public void setFajrAngle(double angle) {
		double[] params = {angle, -1, -1, -1, -1};
		setCustomParams(params);
	}

	// set the angle for calculating Maghrib
	public void setMaghribAngle(double angle) {
		double[] params = {-1, 0, angle, -1, -1};
		setCustomParams(params);

	}

	// set the angle for calculating Isha
	public void setIshaAngle(double angle) {
		double[] params = {-1, -1, -1, 0, angle};
		setCustomParams(params);

	}

	// set the minutes after Sunset for calculating Maghrib
	public void setMaghribMinutes(double minutes) {
		double[] params = {-1, 1, minutes, -1, -1};
		setCustomParams(params);

	}

	// set the minutes after Maghrib for calculating Isha
	public void setIshaMinutes(double minutes) {
		double[] params = {-1, -1, -1, 1, minutes};
		setCustomParams(params);

	}

	// ---------------------- Compute Prayer Times -----------------------
	// compute prayer times at given julian date
	private double[] computeTimes(double[] times) {

		double[] t = dayPortion(times);

		double Fajr = this.computeTime(
				180 - methodParams.get(this.getCalcMethod().getId())[0], t[0]);

		double Sunrise = this.computeTime(180 - 0.833, t[1]);

		double Dhuhr = this.computeMidDay(t[2]);
		double Asr = this.computeAsr(1 + this.getAsrJuristic().getId(), t[3]);
		double Sunset = this.computeTime(0.833, t[4]);

		double Maghrib = this.computeTime(
				methodParams.get(this.getCalcMethod().getId())[2], t[5]);
		double Isha = this.computeTime(
				methodParams.get(this.getCalcMethod().getId())[4], t[6]);

		double[] CTimes = {Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha};

		return CTimes;

	}

	// compute prayer times at given julian date
	private void computeDayTimes() {
		this.setJDate(julianDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DATE)));
		double lonDiff = lng / (15.0 * 24.0);
		this.setJDate(this.getJDate() - lonDiff);
		prayerTimes = new ArrayList<Date>();
		double[] times = {5, 6, 12, 13, 18, 18, 18}; // default times

		for (int i = 1; i <= this.numIterations; i++) {
			times = computeTimes(times);
		}

		times = adjustTimes(times);
		times = tuneTimes(times);
		//
		Calendar tempCalendar = Calendar.getInstance();
		tempCalendar.set(Calendar.YEAR, this.calendar.get(Calendar.YEAR));
		tempCalendar.set(Calendar.MONTH, this.calendar.get(Calendar.MONTH));
		tempCalendar.set(Calendar.DAY_OF_MONTH, this.calendar.get(Calendar.DAY_OF_MONTH));
		
		for (int i = 0; i < 7; i++) {
			double time = fixhour(times[i] + 0.5 / 60.0); // add 0.5 minutes to round
			int hours = (int)Math.floor(time);
			double minutes = Math.floor((time - hours) * 60.0);
			tempCalendar.set(Calendar.HOUR_OF_DAY, hours);

			tempCalendar.set(Calendar.MINUTE, (int) minutes);
			tempCalendar.set(Calendar.SECOND, 1);
			tempCalendar.set(Calendar.MILLISECOND, 1);
			prayerTimes.add(tempCalendar.getTime());
		}
		this.fajr = prayerTimes.get(0);
		this.sunrise = prayerTimes.get(1);
		this.dhuhr = prayerTimes.get(2);
		this.asr = prayerTimes.get(3);
		this.sunset = prayerTimes.get(4);
		this.maghrib = prayerTimes.get(5);
		this.isha = prayerTimes.get(6);
	}
	
	// adjust times in a prayer time array
	private double[] adjustTimes(double[] times) {
		for (int i = 0; i < times.length; i++) {
			times[i] += this.getTimeZone() - this.getLng() / 15;
		}

		times[2] += this.getDhuhrMinutes() / 60; // Dhuhr
		if (methodParams.get(this.getCalcMethod().getId())[1] == 1) // Maghrib
		{
			times[5] = times[4] + methodParams.get(this.getCalcMethod().getId())[2]/ 60;
		}
		if (methodParams.get(this.getCalcMethod().getId())[3] == 1) // Isha
		{
			times[6] = times[5] + methodParams.get(this.getCalcMethod().getId())[4]/ 60;
		}

		if (this.getAdjustHighLats() != this.None) {
			times = adjustHighLatTimes(times);
		}

		return times;
	}

	// adjust Fajr, Isha and Maghrib for locations in higher latitudes
	private double[] adjustHighLatTimes(double[] times) {
		double nightTime = timeDiff(times[4], times[1]); // sunset to sunrise

		// Adjust Fajr
		double FajrDiff = nightPortion(methodParams.get(this.getCalcMethod().getId())[0]) * nightTime;

		if (Double.isNaN(times[0]) || timeDiff(times[0], times[1]) > FajrDiff) {
			times[0] = times[1] - FajrDiff;
		}

		// Adjust Isha
		double IshaAngle = (methodParams.get(this.getCalcMethod().getId())[3] == 0) ? methodParams.get(this.getCalcMethod().getId())[4] : 18;
		double IshaDiff = this.nightPortion(IshaAngle) * nightTime;
		if (Double.isNaN(times[6]) || this.timeDiff(times[4], times[6]) > IshaDiff) {
			times[6] = times[4] + IshaDiff;
		}

		// Adjust Maghrib
		double MaghribAngle = (methodParams.get(this.getCalcMethod().getId())[1] == 0) ? methodParams.get(this.getCalcMethod().getId())[2] : 4;
		double MaghribDiff = nightPortion(MaghribAngle) * nightTime;
		if (Double.isNaN(times[5]) || this.timeDiff(times[4], times[5]) > MaghribDiff) {
			times[5] = times[4] + MaghribDiff;
		}

		return times;
	}

	// the night portion used for adjusting times in higher latitudes
	private double nightPortion(double angle) {
	   double calc = 0;

	if (adjustHighLats == AngleBased)
		calc = (angle)/60.0;
	else if (adjustHighLats == MidNight)
		calc = 0.5;
	else if (adjustHighLats == OneSeventh)
		calc = 0.14286;

	return calc;
	}

	// convert hours to day portions
	private double[] dayPortion(double[] times) {
		for (int i = 0; i < 7; i++) {
			times[i] /= 24;
		}
		return times;
	}

	// Tune timings for adjustments
	// Set time offsets
	public void tune(int[] offsetTimes) {

		for (int i = 0; i < offsetTimes.length; i++) { // offsetTimes length
			// should be 7 in order
			// of Fajr, Sunrise,
			// Dhuhr, Asr, Sunset,
			// Maghrib, Isha
			this.offsets[i] = offsetTimes[i];
		}
	}

	private double[] tuneTimes(double[] times) {
		for (int i = 0; i < times.length; i++) {
			times[i] = times[i] + this.offsets[i] / 60.0;
		}

		return times;
	}

	public CalculationMethod getCalcMethod() {
		return calcMethod;
	}

	public void setCalcMethod(CalculationMethod calcMethod) {
		this.calcMethod = calcMethod;
	}
	
	public void setCalcMethod(int calcMethod) {
		this.calcMethod = CalculationMethod.get(calcMethod);
	}

	public JuristicMethod getAsrJuristic() {
		return asrJuristic;
	}

	public void setAsrJuristic(JuristicMethod asrJuristic) {
		this.asrJuristic = asrJuristic;
	}
	
	public void setAsrJuristic(int asrJuristic) {
		this.asrJuristic = JuristicMethod.get(asrJuristic);
	}

	public int getDhuhrMinutes() {
		return dhuhrMinutes;
	}

	public void setDhuhrMinutes(int dhuhrMinutes) {
		this.dhuhrMinutes = dhuhrMinutes;
	}

	public int getAdjustHighLats() {
		return adjustHighLats;
	}

	public void setAdjustHighLats(int adjustHighLats) {
		this.adjustHighLats = adjustHighLats;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(double timeZone) {
		this.timeZone = timeZone;
	}

	public double getJDate() {
		return JDate;
	}

	public void setJDate(double jDate) {
		JDate = jDate;
	}
	
	public Date getFajr() {
		return fajr;
	}
	
	public Date getSunrise() {
		return sunrise;
	}
	
	public Date getDhuhr() {
		return dhuhr;
	}
	
	public Date getAsr() {
		return asr;
	}
	
	public Date getSunset() {
		return sunset;
	}
	
	public Date getMaghrib() {
		return maghrib;
	}
	
	public Date getIsha() {
		return isha;
	}
}