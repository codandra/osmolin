package labelmaker;

import java.util.ArrayList;

/**
 *
 * @author sutter
 */
public class LabelMaker {
	
	private final static double ANGLE_TOL_RAD = 2.0 * Math.PI / 180; //2 degress
	
	
	private ArrayList<Point2D> polygon;
	private ArrayList<LabelCalc> labelCalcs = new ArrayList<LabelCalc>();
	
	public LabelMaker(ArrayList<Point2D> polygon) {
		this.polygon = polygon;
	}
	
	public LabelRect getLabel() {
		//make sure it is closed
		int size = polygon.size();
		if(size < 3) return null;
		Point2D first = polygon.get(0);
		Point2D last = polygon.get(size-1);
		if((first.getX() != last.getX())||(first.getY() != last.getY())) {
			polygon.add(first);
			size++;
		}
		
		for(int i=0; i < size-1; i++) {
			LabelCalc lc = new LabelCalc(polygon,i);
			double angleRad = lc.calculateAngleRad();
			if((angleRad != LabelCalc.INVALID_ANGLE)||(!angleMatchExists(angleRad))) { 
				//don't add this
				lc.calculateLabel();
				labelCalcs.add(lc);
			}
		}
		
		double bestArea = 0;
		LabelCalc bestRect = null;
		for(LabelCalc lc:labelCalcs) {
			double area = lc.getHeight() * lc.getWidth();
			if(area > bestArea) {
				bestArea = area;
				bestRect = lc;
			}
		}
		
		if(bestRect != null) {
			LabelRect rect = new LabelRect();
			rect.centerX = bestRect.getCenter().getX();
			rect.centerY = bestRect.getCenter().getY();
			rect.width = bestRect.getWidth();
			rect.height = bestRect.getHeight();
			rect.angleDeg = bestRect.getFinalAngleDeg();
			return rect;
		}
		else {
			return null;
		}
	}
	
	private boolean angleMatchExists(double angleRad) {
		for(LabelCalc lc:labelCalcs) {
			if(Math.abs(lc.getInitialAngleRad() - angleRad) < ANGLE_TOL_RAD) return true;
		}
		return false;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		
		//=============================
		// Transform tests
		//=============================
		labelmaker.AffineTransform atd1 = new labelmaker.AffineTransform();
		java.awt.geom.AffineTransform atj1 = new java.awt.geom.AffineTransform();
		
		atd1.setToRotation(.2);
		atj1.setToRotation(.2);
		compare("rotation",atd1,atj1);
		
		labelmaker.Point2D pd = new labelmaker.Point2D.Double(.3,5.1);
		java.awt.geom.Point2D pj = new java.awt.geom.Point2D.Double(.3,5.1);
		
		atd1.transform(pd, pd);
		atj1.transform(pj, pj);
		
		compare("point transform",pd,pj);
		
		labelmaker.AffineTransform atd3 = new labelmaker.AffineTransform();
		java.awt.geom.AffineTransform atj3 = new java.awt.geom.AffineTransform();
		atd3.setToTranslation(1.1,2.2);
		atj3.setToTranslation(1.1,2.2);
		compare("set to translation",atd3,atj3);
		
		labelmaker.AffineTransform atd2 = null;
		java.awt.geom.AffineTransform atj2 = null;
		try {
			atd2 = atd1.createInverse();
			atj2 = atj1.createInverse();
			
			compare("inverse",atd2,atj2);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		atd1.translate(1.0,2.0);
		atj1.translate(1.0,2.0);
		
		compare("translate",atd1,atj1);
		
		atd1.preConcatenate(atd2);
		atj1.preConcatenate(atj2);
		
		compare("preconcatenate",atd1,atj1);
		
		//===========================
		// Label Tests
		//===========================
		
		ArrayList<Point2D> polygon = new ArrayList<Point2D>();
//		polygon.add(new Point2D.Double(0,0));
//		polygon.add(new Point2D.Double(11,0));
//		polygon.add(new Point2D.Double(11,31));
//		polygon.add(new Point2D.Double(0,31));
		
//		polygon.add(new Point2D.Double(5,7));
//		polygon.add(new Point2D.Double(15,12));
//		polygon.add(new Point2D.Double(10,22));
//		polygon.add(new Point2D.Double(0,17));
		
		polygon.add(new Point2D.Double(0,0));
		polygon.add(new Point2D.Double(10,0));
		polygon.add(new Point2D.Double(0,10));
		polygon.add(new Point2D.Double(0,0));
		
		LabelMaker labelMaker = new LabelMaker(polygon);
		LabelRect rect = labelMaker.getLabel();
		
		if(rect != null) {
			System.out.print("Label: ");
			System.out.print(rect.centerX);
			System.out.print(",");
			System.out.print(rect.centerY);
			System.out.print(", w=");
			System.out.print(rect.width);
			System.out.print(", h=");
			System.out.print(rect.height);
			System.out.print(", angleDeg=");
			System.out.print(rect.angleDeg);
		}
		
	}
	
	public static void compare(String msg, labelmaker.AffineTransform atd,java.awt.geom.AffineTransform atj) {
		double[] md = new double[6];
		atd.getMatrix(md);
		double[] mj = new double[6];
		atj.getMatrix(mj);
		System.out.print(msg);
		System.out.print(": ");
		for(int i = 0; i < 6; i++) {
			System.out.print(md[i] - mj[i]);
			System.out.print(",");
		}
		System.out.println();
	} 
	
	public static void compare(String msg, labelmaker.Point2D pd,java.awt.geom.Point2D pj) {
		System.out.print(msg);
		System.out.print(": ");
		System.out.print(pd.getX() - pj.getX());
		System.out.print(",");
		System.out.print(pd.getY() - pj.getY());
		System.out.println();
	} 
}
