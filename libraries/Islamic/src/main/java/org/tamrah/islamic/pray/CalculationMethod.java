package org.tamrah.islamic.pray;

/**
 * 
 * @author abdullah alfadhel
 * @version 3.0
 * @since 3.0
 */

public enum CalculationMethod {
	Jafari(0),		// Ithna Ashari
	Karachi(1),		// University of Islamic Sciences, Karachi
	ISNA(2),		// Islamic Society of North America (ISNA)
	MWL(3),			// Muslim World League (MWL)
	Makkah(4),		// Umm al-Qura, Makkah
	Egypt(5),		// Egyptian General Authority of Survey
	Tehran(6),		// Institute of Geophysics, University of Tehran
	Custom(7);		// Custom Setting
	private int id;
	private CalculationMethod(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public static CalculationMethod get(int id){
		switch (id) {
		case 0:
			return Jafari;
		case 1:
			return Karachi;
		case 2:
			return ISNA;
		case 3:
			return MWL;
		case 4:
			return Makkah;
		case 5:
			return Egypt;
		case 6:
			return Tehran;
		default:
			return Custom;
		}
	}
}