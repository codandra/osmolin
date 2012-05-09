package labelmaker;

import java.awt.*;
import java.awt.Shape;
import java.awt.geom.*;
import java.awt.image.*;

import java.util.List;
import java.util.ArrayList;

public class ShapeAnalyzer {

//	Shape baseShape;
//	
//	Graphics2D g2;
//	BufferedImage image;
//	Raster raster;
//	double rotAngle, transX, transY;
//
//	private final static double MIN_ANGLE_DIFF_RAD = Math.toRadians(1.0);
//	private final static double DEG90_IN_RAD = Math.toRadians(90);
//	
//	private final static int STROKE_WIDTH = 2;
//	private final static int STROKE_COLOR_INT = 0x0000ff;
//	private final static int FILL_COLOR_INT = 0xff0000;
//	
//	private final double PIXEL_AREA = 600;
//	
//	public LabelRect getBestRect(ArrayList<Point2D> polygon) {
//		
//		
//		
//		double scaleFactor = getWorkingScaleFactor(shape);
//
//		AffineTransform at = new AffineTransform();
//		at.scale(scaleFactor, scaleFactor);
//		baseShape = at.createTransformedShape(shape);
//		
//		double bestArea = 0;
//		Rectangle bestRect = null;
//		double bestRotAngleRad = 9;
//		Point2D bestTrans = null;
//		boolean bestFound = false;
//		
////do fixed angles matching walls
//List<Double> borderAngles = getBorderAngles(baseShape);
//for(double angleRad:borderAngles) {
//
//			//load the working image
//			Point2D transAmount = loadImage(baseShape,angleRad);
////CLUDGE - temp test
//if(transAmount == null) return null;
//			
//			Rectangle rect = analyzeImage();
//			double area = rect.getWidth() * rect.getHeight();
//			if(area > bestArea) {
//				bestArea = area;
//				bestRect = rect;
//				bestRotAngleRad = angleRad;
//				bestTrans = transAmount;
//				bestFound = true;
//			}
//		}
//		
//		if(!bestFound) {
//			System.out.println("No rectangle found");
//			return null;
//		}
//		
//System.out.println("width,height = " + bestRect.width + "," + bestRect.height);
//	
//		//get the best value and display it
//		 Point2D[] outPoints = getPointsFromRect(bestRect);
//		 
//		try {
//			AffineTransform fix = new AffineTransform();
//			//doing this order is equivelent to doing the trans first, rotate second, scale third
//			fix.scale(1/scaleFactor,1/scaleFactor);
//			fix.rotate(-bestRotAngleRad);
//			fix.translate(-bestTrans.getX(), -bestTrans.getY());
//			fix.transform(outPoints, 0, outPoints, 0, outPoints.length);
//			
//			//store results, adjusting so bigest dimension is width (at least for lang with horizontal text)
//			double width;
//			double height;
//			double angleRad;
//			if(bestRect.width > bestRect.height) {
//				width = bestRect.width / scaleFactor;
//				height = bestRect.height / scaleFactor;
//				angleRad = -bestRotAngleRad;
//			}
//			else {
//				width = bestRect.height / scaleFactor;
//				height = bestRect.width / scaleFactor;
//				angleRad = -bestRotAngleRad + Math.PI/2.0;
//			}
//			Point2D center = getCenter(outPoints);
//			LabelRect result = new LabelRect(center.getX(),center.getY(),width,height,angleRad);
//			return result;
//
//		}
//		catch(Exception ex) {
//			ex.printStackTrace();
//			return null;
//		}
//		
//		
//	}
//	
//	//================================
//	// Private Methods
//	//================================
//	
//	private Point2D getCenter(Point2D[] points) {
//		double x = 0;
//		double y = 0;
//		for(Point2D point:points) {
//			x += point.getX();
//			y += point.getY();
//		}
//		return new Point2D.Double(x/4,y/4);
//	}
//	
//	private Point2D[] getPointsFromRect(Rectangle rect) {
//		Point2D[] points = new Point2D[4];
//		points[0] = new Point2D.Double(rect.x,rect.y);
//		points[1] = new Point2D.Double(rect.x + rect.width,rect.y);
//		points[2] = new Point2D.Double(rect.x + rect.width,rect.y + rect.height);
//		points[3] = new Point2D.Double(rect.x,rect.y + rect.height);
//		
//		return points;
//	}
//	
//	private double getWorkingScaleFactor(Shape shape) {
//		Rectangle bounds = shape.getBounds();
//		double area = bounds.height * bounds.width;
//		if(area == 0) return 1;
//		
//		return Math.sqrt(PIXEL_AREA / area);
//		
//	}
//	
//	/** creates an image of the input path rotated the given angle. The 
// 	 * return value is the amount the path was translated so the bounds of the image intersected
//	 * the bounds of the object. */
//	private Point2D loadImage(Shape shape, double theta) {
//		
//		AffineTransform rotTransform = new AffineTransform();
//		rotTransform.rotate(theta);
//		Shape transformedPath = rotTransform.createTransformedShape(shape);
//		
//		Rectangle2D bounds = transformedPath.getBounds2D();
//		AffineTransform tranTransform = new AffineTransform();
//		Point2D transAmount = new Point2D.Double(-bounds.getX(),-bounds.getY());
//		tranTransform.translate(transAmount.getX(),transAmount.getY());
//		transformedPath = tranTransform.createTransformedShape(transformedPath);
//if((bounds.getWidth() == 0)||(bounds.getHeight() == 0)) {
//	System.out.println("Warning - width/height 0!");
//	return null;
//}
//		//create the image
//		image = new BufferedImage((int)Math.ceil(bounds.getWidth()),(int)Math.ceil(bounds.getHeight()),BufferedImage.TYPE_INT_RGB);
//		g2 = (Graphics2D)image.getGraphics();
//		
//		g2.setColor(Color.WHITE);
//		g2.fillRect(0,0,image.getWidth(),image.getHeight());
//		
//		Stroke stroke = new BasicStroke(STROKE_WIDTH);
//		
//		g2.setColor(new Color(FILL_COLOR_INT));
//		g2.fill(transformedPath);
//		g2.setStroke(stroke);
//		g2.setColor(new Color(STROKE_COLOR_INT));
//		g2.draw(transformedPath);
//		
//		raster = image.getData();
//		
//		return transAmount;
//	}
//	
//	private Rectangle analyzeImage() {
//		
//		int area = 0;
//		Rectangle bestAreaRect = new Rectangle(0,0,0,0);
//		
//		int imageWidth = image.getWidth();
//		int imageHeight = image.getHeight();
//		
//System.out.println("Anylyze start w = " + imageWidth + ", h = " + imageHeight);
//long start = System.currentTimeMillis();		
//		
//		int[] endXArray = new int[imageHeight];
//		//try for start pixel X in largest rectangle
//		for(int startRX = 0; startRX < imageWidth; startRX++) {
//			//check the start pixel Y in the largest rectangle 
//			for(int currentRY = 0; currentRY < imageHeight; currentRY++) {
//				//search for the rectangle x end
//				//mark end as invalid to initialize
//				endXArray[currentRY] = -1;
//				for(int endRX = startRX; endRX < imageWidth; endRX++) {
//					//find actual end for this Y
//					int rgb = getPixel(endRX,currentRY);
//					if(rgb == FILL_COLOR_INT) {
//						endXArray[currentRY] = endRX;
//					}
//					else {
//						break;
//					}
//				}
//			}
//			//for a given start x, we know the valid range of x for a given y
//			//find the best range of y
//			int currentArea;
//			for(int startRY = 0; startRY < imageHeight; startRY++) {
//				int minWidth = Integer.MAX_VALUE;
//				int columnHeight;
//				for(int endRY = startRY; endRY < imageHeight; endRY++) {
//					int rowWidth = (endXArray[endRY] - startRX + 1);
//					if(rowWidth <= 0) {
//						//if the row width is 0, there is no more for this column
//						//using this start y
//						break;
//					}
//					if(rowWidth < minWidth) {
//						minWidth = rowWidth;
//					}
//					columnHeight = endRY - startRY + 1;
//					currentArea = minWidth * columnHeight;
//					
//					if(currentArea > area) {
//						area = currentArea;
//						bestAreaRect.x =  startRX;
//						bestAreaRect.y =  startRY;
//						bestAreaRect.width = minWidth;
//						bestAreaRect.height = columnHeight;
//					}
//				}
//			}
//		}
//		
//long end = System.currentTimeMillis();
//System.out.println("Anylyze finished: " + String.valueOf(end - start) + " msec");		
//		//return the best rectangle, converted to Rect2D
//		return bestAreaRect;
//	}
//	
//	private int getPixel(int x, int y) {
//		int[] intOut = new int[3];
//		raster.getPixel(x,y,intOut);
//		int rgb = 0;
//		for(int val:intOut) {
//			rgb <<= 8;
//			rgb += val;
//		}
//		return rgb;
//	}
//
//	private double[] segmentCoords = new double[6];
//	private List<Double> getBorderAngles(Shape shape) {
//		List<Double> angleList = new ArrayList<Double>();
//		PathIterator pi = shape.getPathIterator(null);
//		double startX = 0,startY=0,endX=0,endY=0;
//		boolean doCalc = false;
//		while(!pi.isDone()) {
//			int segmentType = pi.currentSegment(segmentCoords);
//			switch(segmentType) {
//				case PathIterator.SEG_MOVETO:
//					endX = segmentCoords[0];
//					endY = segmentCoords[1];
//					doCalc = false;
//					break;
//
//				case PathIterator.SEG_LINETO:
//					endX = segmentCoords[0];
//					endY = segmentCoords[1];
//					doCalc = true;
//					break;
//
//				case PathIterator.SEG_QUADTO:
//					endX = segmentCoords[0];
//					endY = segmentCoords[1];
//					doCalc = true;
//					break;
//
//				case PathIterator.SEG_CUBICTO:
//					endX = segmentCoords[0];
//					endY = segmentCoords[1];
//					doCalc = true;
//					break;
//
//				case PathIterator.SEG_CLOSE:
//					doCalc = false;
//					break;
//
//				default:
//					doCalc = false;
//			}
//
//			if(doCalc) {
//				boolean save = true;
//				//get absolute angle
//				double angle = -(float)Math.atan2(endY - startY, endX - startX);
//				//rotate to 0-90 range
//				while(angle < 0) angle += DEG90_IN_RAD;
//				while(angle > DEG90_IN_RAD) angle -= DEG90_IN_RAD;
//				//check if it is in list already
//				for(double existing:angleList) {
//					if(Math.abs(angle - existing) < MIN_ANGLE_DIFF_RAD) {
//						save = false;
//						break;
//					}
//				}
//				//save if needed
//				if(save) {
//					angleList.add(angle);
//				}
//			}
//
//			//next
//			startX = endX;
//			startY = endY;
//			pi.next();
//		}
//
//		return angleList;
//	}

}
