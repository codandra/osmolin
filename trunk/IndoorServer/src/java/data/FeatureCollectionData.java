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
import java.util.*;

/**
 * This is a lookup for GeoJSON feature objects based on tile. Non-point objects
 * are returned in the tile associated with the approximate centroid of the object.
 * The URL is of the format:
 * [base service url]/featurecollection/[layer name]/[zoom]/[tileX]/[tileY].
 * 
 * The body is a feature collection GeoJSON object. 
 * @author sutter
 */
public class FeatureCollectionData extends HttpServlet {
	
	private final static String SELECT_STMT = "select * from feature where layer = ? and quadkey like ?";

	
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
			//url pattern "/layer/zoom/tileX/tileY"
			if(params.length < 4) throw new Exception("Invalid request format");

			String layer = params[0];
			int zoom = Integer.parseInt(params[1]);
			int tileX = Integer.parseInt(params[2]); 
			int tileY = Integer.parseInt(params[3]); 
			
			String quadkey = MercatorCoordinates.getQuadkey(tileX,tileY,zoom);
			String pattern = quadkey + "%";
			

			conn = Util.getConnection(this.getServletContext());
			pstmt = conn.prepareStatement(SELECT_STMT);
			pstmt.setString(1,layer);
			pstmt.setString(2,pattern);
			rs = pstmt.executeQuery();
			List<FeatureRecord> featureRecords = new ArrayList<FeatureRecord>();
			FeatureRecord featureRecord;
			while(rs.next()) {
				//there should be just one
				featureRecord = FeatureRecord.loadFromResultSet(rs);
				featureRecords.add(featureRecord);
			}
			
			JSONObject json = createFeatureCollection(layer, tileX, tileY, zoom, featureRecords);
			
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
	

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
	
	private JSONObject createFeatureCollection(String layer, int tileX, int tileY,
			int zoom, List<FeatureRecord> featureRecords) throws Exception {
		
		JSONObject json = new JSONObject();
		
		json.put("layer",layer);
		json.put("tileX",tileX);
		json.put("tileY",tileY);
		json.put("zoom",zoom);
		
		//add the features
		json.put("type","FeatureCollection");
		JSONArray featureArray = new JSONArray();
		json.put("features",featureArray);
		
		for(FeatureRecord featureRecord:featureRecords) {
			String featureString = featureRecord.getFeature();
			JSONObject featureJson = new JSONObject(featureString);
			featureArray.put(featureJson);
		}
		
		return json;
	}
	
}


