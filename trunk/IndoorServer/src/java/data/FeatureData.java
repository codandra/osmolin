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

/**
 *
 * @author sutter
 */
public class FeatureData extends HttpServlet {
	
	private final static String SELECT_STMT = "select * from feature where layer = ? and feature_key = ?";
	public final static int SELECT_LAYER_INDEX = 1;
	public final static int SELECT_KEY_INDEX = 2;
	private final static String INSERT_STMT = "insert into feature (layer,feature_key,quadkey,feature) values (?,?,?,?)";
	public final static int INSERT_LAYER_INDEX = 1;
	public final static int INSERT_KEY_INDEX = 2;
	public final static int INSERT_QUADKEY_INDEX = 3;
	public final static int INSERT_FEATURE_INDEX = 4;
	private final static String UPDATE_STMT = "update feature set quadkey=?, feature=? where layer=? and feature_key=?";
	public final static int UPDATE_LAYER_INDEX = 3;
	public final static int UPDATE_KEY_INDEX = 4;
	public final static int UPDATE_QUADKEY_INDEX = 1;
	public final static int UPDATE_FEATURE_INDEX = 2;
	private final static String DELETE_STMT = "delete from feature where layer = ? and feature_key = ?";
	public final static int DELETE_LAYER_INDEX = 1;
	public final static int DELETE_KEY_INDEX = 2;
	
	private final static String TYPE_POLYGON = "polygon";
	private final static String TYPE_LINESTRING = "linestring";
	private final static String TYPE_POINT = "point";
	
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

			RequestParams requestParams = RequestParams.loadParams(request);

			conn = Util.getConnection(this.getServletContext());
			pstmt = conn.prepareStatement(SELECT_STMT);
			pstmt.setString(SELECT_LAYER_INDEX,requestParams.layer);
			pstmt.setString(SELECT_KEY_INDEX,requestParams.key);
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
			
			RequestParams requestParams = RequestParams.loadParams(request);
			JSONObject featureJson = Util.readJson(request.getInputStream());
			
			String quadkey = getFeatureQuadkey(featureJson);

			conn = Util.getConnection(this.getServletContext());
			
			//check if there is a record already
			pstmt = conn.prepareStatement(SELECT_STMT);
			pstmt.setString(SELECT_LAYER_INDEX,requestParams.layer);
			pstmt.setString(SELECT_KEY_INDEX,requestParams.key);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				//update this record
				pstmt = conn.prepareStatement(UPDATE_STMT);
				pstmt.setString(UPDATE_LAYER_INDEX,requestParams.layer);
				pstmt.setString(UPDATE_KEY_INDEX,requestParams.key);
				pstmt.setString(UPDATE_QUADKEY_INDEX,quadkey);
				pstmt.setString(UPDATE_FEATURE_INDEX,featureJson.toString());
			}
			else {
				//create a new record
				pstmt = conn.prepareStatement(INSERT_STMT);
				pstmt.setString(INSERT_LAYER_INDEX,requestParams.layer);
				pstmt.setString(INSERT_KEY_INDEX,requestParams.key);
				pstmt.setString(INSERT_QUADKEY_INDEX,quadkey);
				pstmt.setString(INSERT_FEATURE_INDEX,featureJson.toString());
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
			
			RequestParams requestParams = RequestParams.loadParams(request);

			conn = Util.getConnection(this.getServletContext());
			pstmt = conn.prepareStatement(DELETE_STMT);
			pstmt.setString(DELETE_LAYER_INDEX,requestParams.layer);
			pstmt.setString(DELETE_KEY_INDEX,requestParams.key);
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
	
	private String getFeatureQuadkey(JSONObject featureJson) throws Exception {
		String type = featureJson.optString("type",null);
		if(type == null) {
			throw new Exception("Feature type parameter not found.");
		}
		
		JSONArray coordinates = featureJson.getJSONArray("coordinates");
		
		//get the average position for the feature
		
		double xAve = 0;
		double yAve = 0;
		int count = 0;
		if(type.equalsIgnoreCase(TYPE_POLYGON)) {
			//get the average of the leading line string (outside)
			JSONArray lineString = coordinates.getJSONArray(0);
			int cnt = lineString.length();
			for(int i = 0; i < cnt; i++) {
				JSONArray point = lineString.getJSONArray(i);
				xAve += point.getDouble(0);
				yAve += point.getDouble(1);
				count++;
			}
		}
		else if(type.equalsIgnoreCase(TYPE_LINESTRING)) {
			//get the average of the leading line string (outside)
			JSONArray lineString = coordinates;
			int cnt = lineString.length();
			for(int i = 0; i < cnt; i++) {
				JSONArray point = lineString.getJSONArray(i);
				xAve += point.getDouble(0);
				yAve += point.getDouble(1);
				count++;
			}
		}
		else if(type.equalsIgnoreCase(TYPE_POINT)) {
			//get the average of the leading line string (outside)
			JSONArray point = coordinates;
			xAve += point.getDouble(0);
			yAve += point.getDouble(1);
			count++;
		}
		else {
			throw new Exception("Unsupported feature type: " + type);
		}
		
		if(count > 0) {
			xAve /= count;
			yAve /= count;
		}
		
		double mx = Util.mercMetersXToMercX(xAve);
		double my = Util.mercMetersYToMercY(yAve);
		
		String quadKey = Util.getQuadkeyMax(mx, my);
		
		return quadKey;
	}
	
	private static class RequestParams {
		
		public String layer;
		public String key;
		
		public static RequestParams loadParams(HttpServletRequest request) throws Exception {
			
			RequestParams requestParams = new RequestParams();
			
			String path = request.getPathInfo();
			if(path.charAt(0) == '/') {
				path = path.substring(1);
			}

			String[] params = path.split("/");
			if(params.length < 2) throw new Exception("Invalid request format");

			//url pattern "/name/key/version"
			requestParams.layer = params[0];
			requestParams.key = params[1];
			
			return requestParams;
		}
	}

}

