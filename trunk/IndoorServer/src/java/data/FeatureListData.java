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
 * This is a lookup for all features in the database for a given layer name.
 * The URL is of the format:
 * [base service url]/featurelist/[layer name].
 * 
 * The body is a feature collection GeoJSON object. 
 * @author sutter
 */
public class FeatureListData extends HttpServlet {
	
	private final static String SELECT_STMT = "select feature_key, name, lat, lon from feature where layer = ? order by name, feature_key";

	
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
			
			//url pattern "/layer"
			if(params.length < 1) throw new Exception("Invalid request format");
			String layer = params[0];

			conn = Util.getConnection(this.getServletContext());

			pstmt = conn.prepareStatement(SELECT_STMT);
			pstmt.setString(1,layer);
			rs = pstmt.executeQuery();
			JSONArray featureList = new JSONArray();
			while(rs.next()) {
				//there should be just one
				JSONObject entry = new JSONObject();
				String key = rs.getString("feature_key");
				entry.put("key",key);
				String name = rs.getString("name");
				if(name != null) {
					entry.put("name",name);
				}
				double lat = rs.getDouble("lat");
				entry.put("lat",lat);
				double lon = rs.getDouble("lon");
				entry.put("lon",lon);
				featureList.put(entry);
			}
			
			JSONObject json = new JSONObject();
			json.put("layer",layer);
			json.put("features",featureList);
			
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
	
}


