/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package labelmaker;

/**
 *
 * @author sutter
 */
public class AffineTransform {
	
	private final static int MXX = 0;
	private final static int MYX = 1;
	private final static int MXY = 2;
	private final static int MYY = 3;
	private final static int MX0 = 4;
	private final static int MY0 = 5;
	
	private double[] matrix = new double[6];
	
	public AffineTransform() {
		matrix[MXX] = matrix[MYY] = 1.0;
	}
	
	public void setTransform(double mxx,double myx,double mxy,double myy,double mx0,double my0) {
		matrix[MXX] = mxx;
		matrix[MYX] = myx;
		matrix[MXY] = mxy;
		matrix[MYY] = myy;
		matrix[MX0] = mx0;
		matrix[MY0] = my0;
	}

	public void translate(double dx, double dy) {
		Point2D point = new Point2D.Double(dx,dy);
		this.transform(point,point);
		matrix[MX0] = point.getX();
		matrix[MY0] = point.getY();
	}
	
	public void setToRotation(double angleRad) {
		matrix[MXX] = matrix[MYY] = Math.cos(angleRad);
		matrix[MYX] = Math.sin(angleRad);
		matrix[MXY] = -matrix[MYX];
		matrix[MX0] = matrix[MY0] = 0.0;
	}
	
	public void setToTranslation(double dx, double dy) {
		matrix[MXX] = matrix[MYY] = 1;
		matrix[MYX] = matrix[MXY] = 0.0;
		matrix[MX0] = dx;
		matrix[MY0] = dy;
	}
	
	public void preConcatenate(AffineTransform at) {
		//this is name confuses my, but this amounts to acting the passed 
		//transform on the local transform
		double mxx = at.matrix[MXX] * this.matrix[MXX] + at.matrix[MXY] * this.matrix[MYX];
		double myx = at.matrix[MYX] * this.matrix[MXX] + at.matrix[MYY] * this.matrix[MYX];
		double mxy = at.matrix[MXX] * this.matrix[MXY] + at.matrix[MXY] * this.matrix[MYY];
		double myy = at.matrix[MYX] * this.matrix[MXY] + at.matrix[MYY] * this.matrix[MYY];
		double mx0 = at.matrix[MXX] * this.matrix[MX0] + at.matrix[MXY] * this.matrix[MY0] + at.matrix[MX0];
		double my0 = at.matrix[MYX] * this.matrix[MX0] + at.matrix[MYY] * this.matrix[MY0] + at.matrix[MY0];
		
		this.matrix[MXX] = mxx;
		this.matrix[MYX] = myx;
		this.matrix[MXY] = mxy;
		this.matrix[MYY] = myy;
		this.matrix[MX0] = mx0;
		this.matrix[MY0] = my0;
	}
	
	public void transform(Point2D out, Point2D in) {
		double x = this.matrix[MXX] * in.getX() + this.matrix[MXY] * in.getY() + this.matrix[MX0];
		double y = this.matrix[MYX] * in.getX() + this.matrix[MYY] * in.getY() + this.matrix[MY0];
		out.setX(x);
		out.setY(y);
	}

	public AffineTransform createInverse() {
		double det = matrix[MXX]*matrix[MYY] - matrix[MYX]*matrix[MXY];
		AffineTransform inverse = new AffineTransform();
		inverse.matrix[MXX] = this.matrix[MYY]/det;
		inverse.matrix[MYY] = this.matrix[MXX]/det;
		inverse.matrix[MYX] = -this.matrix[MYX]/det;
		inverse.matrix[MXY] = -this.matrix[MXY]/det;
		inverse.matrix[MX0] = -inverse.matrix[MXX] * this.matrix[MX0] - inverse.matrix[MXY] * this.matrix[MY0];
		inverse.matrix[MY0] = -inverse.matrix[MYX] * this.matrix[MX0] - inverse.matrix[MYY] * this.matrix[MY0];
		
		return inverse;
	}
	
	public void getMatrix(double[] matrix) {
		for(int i = 0; i < 6; i++) {
			matrix[i] = this.matrix[i];
		}
	}
}
