package labelmaker;

import java.util.ArrayList;

/**
 * This class calculates a label based on a grid parallel to a given side.
 * 
 * @author sutter
 */
public class LabelCalc {
	
	public final static double SMALL = 1e-9;
	public final static double BIG = 1e9;
	public final static double INVALID_ANGLE = 720;
	public final static double SQUARE_FRACTION_TOLERANCE = .9;
	
	private final static int POINT_LENGTH = 25;

	private int side;
	private double angleRad;
	private ArrayList<Point2D> polygon;
	
	boolean[][] grid;
			
	private double x0;
	private double y0;
	private double delta;
	private int countX;
	private int countY;
	private Point2D center;
	private double width;
	private double height;
	private boolean isValid = false;
	
	//constructor
	public LabelCalc(ArrayList<Point2D> polygon, int side) {
		this.polygon = polygon;
		this.side = side;
	}
	
	public boolean getIsValid() {
		return isValid;
	}
	
	public Point2D getCenter() {
		return center;
	}
	
	public double getWidth() {
		return width;
	}
	
	public double getHeight() {
		return height;
	}
	
	public double getInitialAngleRad() {
		return angleRad;
	}
	
	public double getFinalAngleDeg() {
		double baseAngle = Math.toDegrees(angleRad);
		if(isSquare()) {
			//do angle closest to 0
			if(baseAngle > 45) return baseAngle - 90;
			else if(baseAngle < -45) return baseAngle + 90;
			else return baseAngle;
		}
		else {
			if(width >= height) {
				return baseAngle;
			}
			else {
				if(baseAngle >= 0) {
					return baseAngle + 90;
				}
				else {
					return baseAngle - 90;
				}
			}
		}
	}
	
	private boolean isSquare() {
		if((width >= height)&&(height/width > SQUARE_FRACTION_TOLERANCE)) return true;
		else if(width/height > SQUARE_FRACTION_TOLERANCE) return true;
		else return false;
	}
	
	/** This calculates the angle based on the given side. If it returns
	 * INVALID_ANGLE there is no valid area. */
	public double calculateAngleRad() {
		if(polygon.size() <= side) return INVALID_ANGLE;
	
		Point2D p1 = polygon.get(side);
		int next = side+1;
		if(next >= polygon.size()) next = 0;
		Point2D p2 = polygon.get(next);
		
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		if((dx < SMALL)&&(dy < SMALL)) {
			angleRad = INVALID_ANGLE;
		}
		else {
			//make a unit vector
			double temp;
			temp = Math.sqrt(dx*dx + dy*dy);
			dx /= temp;
			dy /= temp;
			//normalize - put in quandrant 1
			if(dx < 0) {
				dx = -dx;
				dy = -dy;
			}
			if(dy < 0) {
				temp = dx;
				dx = -dy;
				dy = dx;
			}
			//get orientation
			angleRad = Math.atan2(dy, dx);
		}
		return angleRad;
	}
	
	public boolean calculateLabel() {
		//transform the polygon
		double minX = BIG;
		double minY = BIG;
		double maxX = -BIG;
		double maxY = -BIG;
		AffineTransform at = new AffineTransform();
		at.setToRotation(-angleRad);
		ArrayList<Point2D> transformed = new ArrayList<Point2D>();
		Point2D firstP2 = null;
		Point2D p2 = null;
		for(Point2D p:polygon) {
			p2 = new Point2D.Double();
			at.transform(p2, p);
			transformed.add(p2);
			if(p2.getX() < minX) minX = p2.getX();
			if(p2.getY() < minY) minY = p2.getY();
			if(p2.getX() > maxX) maxX = p2.getX();
			if(p2.getY() > maxY) maxY = p2.getY();
			if(firstP2 == null) firstP2 = p2;
		}
		//normalize so last point equals first point
		if((firstP2.getX() != p2.getX())||(firstP2.getY() != p2.getY())) {
			polygon.add(firstP2);
		}
		//copy over
		polygon = transformed;
		
		//get the grid parameters
		double dx = maxX - minX;
		double dy = maxY - minY;
		
		delta = Math.sqrt(dx*dy)/POINT_LENGTH;
		
		countX = (int)Math.ceil(dx/delta);
		countY = (int)Math.ceil(dy/delta);
		
		double offsetX = (countX * delta - dx)/2.0;
		double offsetY = (countY * delta - dy)/2.0;
		
		x0 = minX - offsetX;
		y0 = minY - offsetY;
		
		//create and fill the grid
		grid = new boolean[countX][countY];
		for(int ix = 0; ix < countX; ix++) {
			for(int iy = 0; iy < countY; iy++) {
				grid[ix][iy] = isInside(ix,iy);
			}
		}
		
		boolean rectFound = analyzeImage();
		return rectFound;
	}
	
	private boolean analyzeImage() {
		
		int area = 0;
		boolean rectFound = false;
		
		int rectX = 0;
		int rectY = 0;
		int rectWidth = 0;
		int rectHeight = 0;
		
System.out.println("Anylyze start w = " + countX + ", h = " + countY);
long start = System.currentTimeMillis();		
		
		int[] endXArray = new int[countY];
		//try for start pixel X in largest rectangle
		for(int startRX = 0; startRX < countX; startRX++) {
			//check the start pixel Y in the largest rectangle 
			for(int currentRY = 0; currentRY < countY; currentRY++) {
				//search for the rectangle x end
				//mark end as invalid to initialize
				endXArray[currentRY] = -1;
				for(int endRX = startRX; endRX < countX; endRX++) {
					//find actual end for this Y
					if(grid[endRX][currentRY]) {
						endXArray[currentRY] = endRX;
					}
					else {
						break;
					}
				}
			}
			//for a given start x, we know the valid range of x for a given y
			//find the best range of y
			int currentArea;
			for(int startRY = 0; startRY < countY; startRY++) {
				int minWidth = Integer.MAX_VALUE;
				int columnHeight;
				for(int endRY = startRY; endRY < countY; endRY++) {
					int rowWidth = (endXArray[endRY] - startRX + 1);
					if(rowWidth <= 0) {
						//if the row width is 0, there is no more for this column
						//using this start y
						break;
					}
					if(rowWidth < minWidth) {
						minWidth = rowWidth;
					}
					columnHeight = endRY - startRY + 1;
					currentArea = minWidth * columnHeight;
					
					if(currentArea > area) {
						area = currentArea;
						rectX =  startRX;
						rectY =  startRY;
						rectWidth = minWidth - 1;
						rectHeight = columnHeight - 1;
						rectFound = true;
					}
				}
			}
		}
		
long end = System.currentTimeMillis();
System.out.println("Anylyze finished: " + String.valueOf(end - start) + " msec");

		if(rectFound) {
			//get center
			double x = x0 + delta * (rectX + rectWidth / 2.0);
			double y = y0 + delta * (rectY + rectHeight / 2.0);
			center = new Point2D.Double(x,y);
			AffineTransform at = new AffineTransform();
			at.setToRotation(angleRad);
			at.transform(center, center);
			
			//get height and width
			width = rectWidth * delta;
			height = rectHeight * delta;
			
			isValid = true;
		}
		
		return rectFound;
	}


	private boolean isInside(int ix, int iy) {
		double x = x0 + delta * ix;
		double y = y0 + delta * iy;
		
		boolean isInside = false;
		Point2D prev = null;
		for(Point2D p:polygon) {
			if(prev == null) {
				prev = p;
			}
			else {
				//get the value of y where the lines intersect
				if(halfLineHits(p,prev,x,y)) {
					isInside = !isInside;
				}
			}
			prev = p;
		}
		
		//odd nubmer of hits means inside
		return isInside;
	}
	
	/** Calculate a vertical line from the test point intersecting the segment
	 * defined by p1 and p2. */
	private boolean halfLineHits(Point2D p1, Point2D p2, double x0, double y0) {
		double ax0 = p1.getX();
		double ay0 = p1.getY();
		double ax1 = p2.getX();
		double ay1 = p2.getY();
		
		if(ax0 == ax1) {
			return false;
		}
		else {
			double tint = (x0 - ax0)/ (ax1 - ax0);
			if((tint >= 0)&&(tint < 1)) {
				double yint = (ay1 - ay0) * tint + ay0;
				if(yint > y0) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}
	}

}
