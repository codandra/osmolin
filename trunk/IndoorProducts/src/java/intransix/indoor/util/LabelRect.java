package intransix.indoor.util;

import java.util.ArrayList;
import org.json.*;
import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class LabelRect {

	private double centerX;
	private double centerY;
	private double width;
	private double height;
	private double angleRad;

	public static LabelRect createLabel(ArrayList<Point2D> points) {
		if(points.size() < 3) return null;
		
		Path2D path = new Path2D.Double();
		boolean pathStarted = false;
		for(Point2D p:points) {
			if(!pathStarted) {
				path.moveTo(p.getX(),p.getY());
				pathStarted = true;
			}
			else {
				path.lineTo(p.getX(),p.getY());
			}
		}
		
		ShapeAnalyzer sa = new ShapeAnalyzer();
		return sa.getBestRect(path);	
	}
	
	public double getCenterX() {
		return centerX;
	}
	
	public double getCenterY() {
		return centerY;
	}

	public LabelRect(double centerX, double centerY, double width, double height, double angleRad) {
		super();
		this.centerX = centerX;
		this.centerY = centerY;
		this.width = width;
		this.height = height;
		this.angleRad = angleRad;
	}
	
	public JSONArray toJson(int coordPrecision, int radPrecison) {
		JSONArray json = new JSONArray();
		json.put(new FormattedDecimal(centerX,coordPrecision));
		json.put(new FormattedDecimal(centerY,coordPrecision));
		json.put(new FormattedDecimal(width,coordPrecision));
		json.put(new FormattedDecimal(height,coordPrecision));
		json.put(new FormattedDecimal(angleRad,radPrecison));
		
		return json;
	}

}
