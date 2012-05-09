package data;

import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.sql.*;

import org.json.*;

/**
 *
 * @author sutter
 */
public class Version extends HttpServlet {
	
	private final static String INSERT_STMT = "insert into version (name,name_key) VALUES (?,?)";



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
		PrintWriter out = null;
		try {
			
			RestFileInfo fileInfo = RestFileInfo.loadFileInfo(request);
		
			int version = 0;
			
			conn = Util.getConnection(this.getServletContext());
			
			pstmt = conn.prepareStatement(INSERT_STMT,Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1,fileInfo.name);
			pstmt.setString(2,fileInfo.key);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();
			while(rs.next()) {
				version = rs.getInt(1);
			}
			conn.commit();
			
			JSONObject json = new JSONObject();
			json.put("version",version);
			
			out = response.getWriter();
			out.println(json.toString());
		}
		catch(Exception ex) {
			throw new ServletException(ex);
		}
		finally {
			Util.closeRS(rs);
			Util.closeStmt(pstmt);
			Util.closeConn(conn);
			Util.closeWriter(out);
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
