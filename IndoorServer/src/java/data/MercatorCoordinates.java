package data;

import java.awt.geom.AffineTransform;

/**
 * These are transformations between latitude and longitude in radians and the version of
 * mercator coordinates where a span of 1 unit covers the globe.
 * 
 * Original source code was provided by Micello, Inc.
 */
public class MercatorCoordinates {
	
	public final static double MIN_SIZE = 0;
	public final static double MAX_SIZE = 1;
	
	//value used in conversion for EPSG:900913 (mercator in meters coordinates)
	private final static double EARTH_CIRC_METERS = 40075016.68; 
	private final static double METERS_PER_MERC0 = EARTH_CIRC_METERS;
	
	public final static int MAX_ZOOM = 30;
	public final static int MAX_COUNT = 1 << MAX_ZOOM;

	/** This method returns the number of meters in 1 mercator coordinate unit, for either the x or y direction. */
	public static double metersPerMerc(double my) {
		double latRad = myToLatRad(my);
		return METERS_PER_MERC0 * Math.cos(latRad);

	}

	public static double lonRadToMx(double lonRad) {
		return (.5 + lonRad/(2 * Math.PI) );
	}

	public static double latRadToMy(double latRad) {
		double sinLat = Math.sin(latRad);
		return ( .5 - Math.log((1 + sinLat)/(1-sinLat))/(4 * Math.PI) );
	}

	public static double mxToLonRad(double mx) {
		return 2*Math.PI * (mx - .5);
	}

	public static double myToLatRad(double my) {
		return Math.atan( Math.sinh( 2*Math.PI * (.5 - my) ));
	}

	private final static int MXX_INDEX = 0;
	private final static int MYX_INDEX = 1;
	private final static int MXY_INDEX = 2;
	private final static int MYY_INDEX = 3;
	private final static int MX0_INDEX = 4;
	private final static int MY0_INDEX = 5;

	private final static double MERC_TO_LON_DEG = 360.0;

	/** This takes as input a transformation matrix from natural coordinates to mercator
	 * coordinates and returns a tranformation from natural coordinates to lat lon. */
	public static AffineTransform convertMercatorToLatLonTransform(AffineTransform natToMxy) {
		double[] llElements = new double[6];
		natToMxy.getMatrix(llElements);

		//convert the 0 point
		double mx0 = Math.toDegrees(mxToLonRad(llElements[MX0_INDEX]));
		double my0Rad = myToLatRad(llElements[MY0_INDEX]);
		double my0 = Math.toDegrees(my0Rad);

		double cosLat = Math.cos(my0Rad);

		//rescale the transform matrix, change sign for Y
		double mxx = MERC_TO_LON_DEG * llElements[MXX_INDEX];
		double mxy = MERC_TO_LON_DEG * llElements[MXY_INDEX];
		double myx = -cosLat * MERC_TO_LON_DEG * llElements[MYX_INDEX];
		double myy = -cosLat * MERC_TO_LON_DEG * llElements[MYY_INDEX];

		return new AffineTransform(mxx,myx,mxy,myy,mx0,my0);

	}

	/** This takes as input a transformation matrix from natural coordinates to mercator
	 * coordinates and returns a tranformation from natural coordinates to lat lon. */
	public static AffineTransform convertLatLonToMercatorTransform(AffineTransform natToLatLon) {
		double[] llElements = new double[6];
		natToLatLon.getMatrix(llElements);

		//convert the 0 point
		double lon0Rad = Math.toRadians(llElements[MX0_INDEX]);
		double mx0 = MercatorCoordinates.lonRadToMx(lon0Rad);
		double lat0Rad = Math.toRadians(llElements[MY0_INDEX]);
		double my0 = MercatorCoordinates.latRadToMy(lat0Rad);

		double cosLat = Math.cos(lat0Rad);

		//rescale the transform matrix, change sign for Y
		double mxx = llElements[MXX_INDEX] / MERC_TO_LON_DEG;
		double mxy = llElements[MXY_INDEX] / MERC_TO_LON_DEG;
		double myx = llElements[MYX_INDEX] / (-cosLat * MERC_TO_LON_DEG);
		double myy = llElements[MYY_INDEX] / (-cosLat * MERC_TO_LON_DEG);

		return new AffineTransform(mxx,myx,mxy,myy,mx0,my0);

	}
	
		/** This gets the quadkey for a given mx and my
	 * 
	 * @param mx	mercator coordinates, with a range of 0 to 1
	 * @param my	mercator coordinates, with a range of 0 to 1
	 * @return		The quadkey string
	 */
	public static String getQuadkeyMax(double mx, double my) {
		int mxMax = (int)(mx * MAX_COUNT);
		int myMax = (int)(my * MAX_COUNT);
		return getQuadkey(mxMax,myMax,MAX_ZOOM);
	}
	
	/** This returns the quadkey for the given tile.
	 * 
	 * @param x			The tile x value
	 * @param y			The tile y value
	 * @param zoom		The zoom scale
	 * @return			The quadkey string
	 */
	public static String getQuadkey(int tileX, int tileY, int zoom) {
		int[] bits = new int[zoom];
		boolean xbit, ybit;
		int mask = 0x01;
		for(int i = 0; i < zoom; i++) {
			xbit = ((tileX & mask) != 0);
			ybit = ((tileY & mask) != 0);
			bits[i] = (xbit ? 1 : 0) + (ybit ? 2 : 0);
			mask <<= 1;
		}
		StringBuilder sb = new StringBuilder();
		for(int i = zoom-1; i >= 0; i--) {
			sb.append(bits[i]);
		}
		return sb.toString();
	}
	
	public static double mercMetersXToMercX(double metersX) {
		return metersX/EARTH_CIRC_METERS + .5;
	}
	public static double mercMetersYToMercY(double metersY) {
		return .5 - metersY/EARTH_CIRC_METERS;
	}
	
	public static double mercXToMetersX(double mx) {
		return (mx - .5) * EARTH_CIRC_METERS;
	}
	public static double mercYToMetersY(double my) {
		return (.5 - my) * EARTH_CIRC_METERS;
	}

}
