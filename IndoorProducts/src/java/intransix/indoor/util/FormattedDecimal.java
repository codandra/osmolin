/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package intransix.indoor.util;

import org.json.JSONString;

/**
 * This class extends the JSONString method to give custom formatted output to allow the
 * number of decimal points to be specifed
 * @author sutter
 */
public class FormattedDecimal implements JSONString {

	private double value;
	private int decimalPlaces;

	public FormattedDecimal(double value, int decimalPlaces) {
		this.value = value;
		this.decimalPlaces = decimalPlaces;
	}

	public String toJSONString() {
		return String.format("%1$." + String.valueOf(decimalPlaces) + "f", value);
	}
	
	public double getValue() {
		return value;
	}
}
