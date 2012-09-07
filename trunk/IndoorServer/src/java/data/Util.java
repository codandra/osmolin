package data;

import java.io.*;
import javax.servlet.ServletContext;

import javax.naming.InitialContext;
import java.sql.*;
import javax.sql.DataSource;
import org.json.JSONObject;


/**
 *
 * @author sutter
 */
public class Util {
	
	/** This returns a database connection, with autocommit off. */
	public static Connection getConnection(ServletContext context) throws Exception {
		String ds_string= context.getInitParameter("datasource_url");
		InitialContext ic = new InitialContext();
		DataSource ds  = (DataSource) ic.lookup(ds_string);
		Connection conn = ds.getConnection();
		conn.setAutoCommit(false);
		return conn;
	}
	
	public static String getContextParam(String tag, ServletContext context) throws Exception {
		String stringValue = context.getInitParameter(tag);
		if(stringValue == null) {
			throw new Exception("Context value not set: " + tag);
		}
		return stringValue;
	}
	
	/** This method closes the object if it is not null, wrapping it in a try block
	 * and taking no action if the close throws an error. */
	public static void closeConn(Connection c) {
		if(c != null) {
			try {c.close();} catch(Exception ex) {}
		}
	}
	
	/** This method closes the object if it is not null, wrapping it in a try block
	 * and taking no action if the close throws an error. */
	public static void closeStmt(Statement s) {
		if(s != null) {
			try {s.close();} catch(Exception ex) {}
		}
	}
	
	/** This method closes the object if it is not null, wrapping it in a try block
	 * and taking no action if the close throws an error. */
	public static void closeRS(ResultSet rs) {
		if(rs != null) {
			try {rs.close();} catch(Exception ex) {}
		}
	}
	
	/** This method closes the object if it is not null, wrapping it in a try block
	 * and taking no action if the close throws an error. */
	public static void closeIS(InputStream is) {
		if(is != null) {
			try {is.close();} catch(Exception ex) {}
		}
	}
	
	/** This method closes the object if it is not null, wrapping it in a try block
	 * and taking no action if the close throws an error. */
	public static void closeOS(OutputStream os) {
		if(os != null) {
			try {os.close();} catch(Exception ex) {}
		}
	}
	
	/** This method closes the object if it is not null, wrapping it in a try block
	 * and taking no action if the close throws an error. */
	public static void closeWriter(Writer writer) {
		if(writer != null) {
			try {writer.close();} catch(Exception ex) {}
		}
	}
	
	public static JSONObject readJson(InputStream is) throws Exception {
		InputStreamReader in = new InputStreamReader(is, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		StringBuilder sb = new StringBuilder();
		while(true) {
			String data = reader.readLine();
			if(data == null) break;
			sb.append(data);
		}
		String jsonString = sb.toString();
		if(jsonString.length() > 0) {
			return new JSONObject(jsonString);
		}
		else {
			return null;
		}
	}
	
	public static void writeJson(OutputStream os, JSONObject json) throws Exception {
		OutputStreamWriter out = new OutputStreamWriter(os, "UTF-8");
		String data = json.toString();
		out.write(data, 0, data.length());
		out.flush();
		out.close();
	}
	
	public final static int MAX_ZOOM = 30;
	public final static int MAX_COUNT = 1 << MAX_ZOOM;
	
	
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
	public static String getQuadkey(int x, int y, int zoom) {
		int[] bits = new int[zoom];
		boolean xbit, ybit;
		int mask = 0x01;
		for(int i = 0; i < zoom; i++) {
			xbit = ((x & mask) != 0);
			ybit = ((y & mask) != 0);
			bits[i] = (xbit ? 1 : 0) + (ybit ? 2 : 0);
			mask <<= 1;
		}
		StringBuilder sb = new StringBuilder();
		for(int i = zoom-1; i >= 0; i--) {
			sb.append(bits[i]);
		}
		return sb.toString();
	}
	
	public final static double HALF_METERS = 20037508.34;
	
	public static double mercMetersXToMercX(double metersX) {
		return (metersX + HALF_METERS)/(2 * HALF_METERS);
	}
	public static double mercMetersYToMercY(double metersY) {
		return (HALF_METERS - metersY)/(2 * HALF_METERS);
	}
}
