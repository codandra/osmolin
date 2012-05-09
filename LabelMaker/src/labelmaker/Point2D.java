package labelmaker;

/**
 *
 * @author sutter
 */
public interface Point2D {
	
	double getX();

	double getY();
	
	void setX(double x);
	
	void setY(double y);
	
	
	public static class Double implements Point2D {
		private double x;
		private double y;
		
		public Double() {
			this.x = 0.0;
			this.y = 0.0;
		}

		public Double(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}
		
		public void setX(double x) {
			this.x = x;
		}
		
		public void setY(double y) {
			this.y = y;
		}
	}
}
