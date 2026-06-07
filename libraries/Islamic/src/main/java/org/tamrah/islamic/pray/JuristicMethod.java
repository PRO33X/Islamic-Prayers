package org.tamrah.islamic.pray;

/**
 * 
 * @author abdullah alfadhel
 * @version 3.0
 * @since 3.0
 */

public enum JuristicMethod {
	Shafii(0),		// Shafii (standard)
	Hanafi(1);		// Hanafi
	private int id;
	private JuristicMethod(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public static JuristicMethod get(int id){
		switch (id) {
		case 1:
			return Hanafi;
		default:
			return Shafii;
		}
	}
}
