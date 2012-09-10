package data;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.*;
import java.awt.geom.Point2D;

/**
 * This service handle get, put and delete for a GeoJSON web service. The GeoJSON should
 * be in longitude and latitude degrees. The URL is in the format 
 * [base service url]/feature/[layer name]/[key]. The body is the GeoJSON object.
 * 
 * @author sutter
 */
public class FeatureData extends HttpServlet {
	
	private final static String SELECT_STMT = "select * from feature where layer = ? and feature_key = ?";
	public final static int SELECT_LAYER_INDEX = 1;
	public final static int SELECT_KEY_INDEX = 2;
	private final static String INSERT_STMT = "insert into feature (layer,feature_key,quadkey,feature,lat,lon,name) values (?,?,?,?,?,?,?)";
	public final static int INSERT_LAYER_INDEX = 1;
	public final static int INSERT_KEY_INDEX = 2;
	public final static int INSERT_QUADKEY_INDEX = 3;
	public final static int INSERT_FEATURE_INDEX = 4;
	public static int INSERT_LAT_INDEX = 5;
	public static int INSERT_LON_INDEX = 6;
	public final static int INSERT_NAME_INDEX = 7;
	private final static String UPDATE_STMT = "update feature set quadkey=?, feature=? , lat=?, lon=?, name=? where layer=? and feature_key=?";
	public final static int UPDATE_LAYER_INDEX = 6;
	public final static int UPDATE_KEY_INDEX = 7;
	public final static int UPDATE_QUADKEY_INDEX = 1;
	public final static int UPDATE_FEATURE_INDEX = 2;
	public static int UPDATE_LAT_INDEX = 3;
	public static int UPDATE_LON_INDEX = 4;
	public final static int UPDATE_NAME_INDEX = 5;
	private final static String DELETE_STMT = "delete from feature where layer = ? and feature_key = ?";
	public final static int DELETE_LAYER_INDEX = 1;
	public final static int DELETE_KEY_INDEX = 2;
	
	/**
	 * Handles the HTTP
	 * <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		OutputStream os = null;
		try {

			String[] params = Util.getPathParameters(request);
			
			//url pattern "/layer/key"
			if(params.length < 2) throw new Exception("Invalid request format");
			String layer = params[0];
			String key = params[1];

			conn = Util.getConnection(this.getServletContext());
			pstmt = conn.prepareStatement(SELECT_STMT);
			pstmt.setString(SELECT_LAYER_INDEX,layer);
			pstmt.setString(SELECT_KEY_INDEX,key);
			rs = pstmt.executeQuery();
			FeatureRecord featureRecord;
			if(rs.next()) {
				//there should be just one
				featureRecord = FeatureRecord.loadFromResultSet(rs);
			}
			else {
				throw new Exception("Object not found");
			}
			
			JSONObject json = new JSONObject(featureRecord.getFeature());
			
			os = response.getOutputStream();
			Util.writeJson(os,json);
		}
		catch(Exception ex) {
			throw new ServletException(ex);
		}
		finally {
			Util.closeRS(rs);
			Util.closeStmt(pstmt);
			Util.closeConn(conn);
			Util.closeOS(os);
		}
	}
	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		InputStream is = null;
		try {
			
			String[] params = Util.getPathParameters(request);
			
			//url pattern "/layer/key"
			if(params.length < 2) throw new Exception("Invalid request format");
			String layer = params[0];
			String key = params[1];
			
			JSONObject featureJson = Util.readJson(request.getInputStream());
			
			//find the centroid of the feature and get the quadkey for it
			Point2D centroid = GeoJsonUtils.getGeoJsonCentroid(featureJson);
			double mx = MercatorCoordinates.lonRadToMx(Math.toRadians(centroid.getX()));
			double my = MercatorCoordinates.latRadToMy(Math.toRadians(centroid.getY()));
			String quadkey = MercatorCoordinates.getQuadkeyMax(mx,my);
			
			//get the name
			String name = null;
			JSONObject properties = featureJson.optJSONObject("properties");
			if(properties != null) {
				name = properties.getString("name");
			}

			conn = Util.getConnection(this.getServletContext());
			
			//check if there is a record already
			pstmt = conn.prepareStatement(SELECT_STMT);
			pstmt.setString(SELECT_LAYER_INDEX,layer);
			pstmt.setString(SELECT_KEY_INDEX,key);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				//update this record
				pstmt = conn.prepareStatement(UPDATE_STMT);
				pstmt.setString(UPDATE_LAYER_INDEX,layer);
				pstmt.setString(UPDATE_KEY_INDEX,key);
				pstmt.setString(UPDATE_QUADKEY_INDEX,quadkey);
				pstmt.setString(UPDATE_FEATURE_INDEX,featureJson.toString());
				pstmt.setDouble(UPDATE_LON_INDEX,centroid.getX());
				pstmt.setDouble(UPDATE_LAT_INDEX,centroid.getY());
				pstmt.setString(UPDATE_NAME_INDEX,name);
			}
			else {
				//create a new record
				pstmt = conn.prepareStatement(INSERT_STMT);
				pstmt.setString(INSERT_LAYER_INDEX,layer);
				pstmt.setString(INSERT_KEY_INDEX,key);
				pstmt.setString(INSERT_QUADKEY_INDEX,quadkey);
				pstmt.setString(INSERT_FEATURE_INDEX,featureJson.toString());
				pstmt.setDouble(INSERT_LON_INDEX,centroid.getX());
				pstmt.setDouble(INSERT_LAT_INDEX,centroid.getY());
				pstmt.setString(INSERT_NAME_INDEX,name);
			}	
		
			pstmt.executeUpdate();

			conn.commit();
		}
		catch(Exception ex) {
			throw new ServletException(ex);
		}
		finally {
			Util.closeRS(rs);
			Util.closeStmt(pstmt);
			Util.closeConn(conn);
			Util.closeIS(is);
		}
	}
	
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			
			String[] params = Util.getPathParameters(request);
			
			//url pattern "/layer/key"
			if(params.length < 2) throw new Exception("Invalid request format");
			String layer = params[0];
			String key = params[1];

			conn = Util.getConnection(this.getServletContext());
			pstmt = conn.prepareStatement(DELETE_STMT);
			pstmt.setString(DELETE_LAYER_INDEX,layer);
			pstmt.setString(DELETE_KEY_INDEX,key);
			pstmt.executeUpdate();
			conn.commit();
		}
		catch(Exception ex) {
			throw new ServletException(ex);
		}
		finally {
			Util.closeRS(rs);
			Util.closeStmt(pstmt);
			Util.closeConn(conn);
		}
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>

}

