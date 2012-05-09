/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import data.Util;
import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.sql.*;

/**
 *
 * @author sutter
 */
public class FileData extends HttpServlet {
	
	private final static String SELECT3_STMT = "select file_id from file where name = ? and name_key = ? and version = ?";
	private final static String SELECT2_STMT = "select file_id from file where name = ? and name_key = ? order by version desc limit 1";

	private final static String INSERT_STMT = "insert into file (name,name_key,version) values (?,?,?)";
	private final static String DELETE1_STMT = "delete from file where file_id = ?";
	private final static String DELETE2_STMT = "delete from file where name = ? and name_key = ? and version = ?";
	
	
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
			RestFileInfo fileInfo = RestFileInfo.loadFileInfo(request);
		
			int fileId = -1;
			
			String basePath = Util.getContextParam("base_path",this.getServletContext());

			conn = Util.getConnection(this.getServletContext());
			
			if(fileInfo.version == RestFileInfo.INVALID_VERSION) {
				pstmt = conn.prepareStatement(SELECT2_STMT);
			}
			else {
				pstmt = conn.prepareStatement(SELECT3_STMT);
			}
			pstmt.setString(1,fileInfo.name);
			pstmt.setString(2,fileInfo.key);
			if(fileInfo.version != RestFileInfo.INVALID_VERSION) {
				pstmt.setInt(3, fileInfo.version);
			}
			rs = pstmt.executeQuery();
			while(rs.next()) {
				fileId = rs.getInt(1);
			}
			
			os = response.getOutputStream();
			loadFile(basePath,fileId,os);
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
			
			RestFileInfo fileInfo = RestFileInfo.loadFileInfo(request);
			if(fileInfo.version == RestFileInfo.INVALID_VERSION) throw new Exception("The version must be specified");
		
			int fileId = -1;
			
			String basePath = Util.getContextParam("base_path",this.getServletContext());

			conn = Util.getConnection(this.getServletContext());
			
			pstmt = conn.prepareStatement(INSERT_STMT,Statement.RETURN_GENERATED_KEYS);

			pstmt.setString(1,fileInfo.name);
			pstmt.setString(2,fileInfo.key);
			pstmt.setInt(3,fileInfo.version);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();
			while(rs.next()) {
				fileId = rs.getInt(1);
			}
			
			is = request.getInputStream();
			boolean success = saveFile(basePath,fileId,is);
			
			//if it failed, try to delete the entry from the db
			if(!success) {
				Util.closeRS(rs);
				rs = null;
				Util.closeStmt(pstmt);
				pstmt = null;
				
				pstmt = conn.prepareStatement(DELETE1_STMT);
				pstmt.setInt(1,fileId);
				pstmt.execute();
				
				throw new Exception("File save failed");
			}
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
			
			RestFileInfo fileInfo = RestFileInfo.loadFileInfo(request);
			if(fileInfo.version == RestFileInfo.INVALID_VERSION) throw new Exception("The version must be specified");
		
			int fileId = -1;
			
			String basePath = Util.getContextParam("base_path",this.getServletContext());

			conn = Util.getConnection(this.getServletContext());
			
			pstmt = conn.prepareStatement(SELECT3_STMT);

			pstmt.setString(1,fileInfo.name);
			pstmt.setString(2,fileInfo.key);
			pstmt.setInt(3,fileInfo.version);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				fileId = rs.getInt(1);
			}
			
			boolean success = deleteFile(basePath,fileId);
			
			//if we succeeded delete the entry from the db
			if(success) {
				Util.closeRS(rs);
				rs = null;
				Util.closeStmt(pstmt);
				pstmt = null;
				
				pstmt = conn.prepareStatement(DELETE1_STMT);
				pstmt.setInt(1,fileId);
				pstmt.execute();
			}
			else {
				throw new Exception("Error deleting databse entry - file kept");
			}

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
	
	/** This method loads the file given by the file id into the given output stream. */
	private boolean loadFile(String basePath, int fileId, OutputStream os) throws Exception {

		String fileString = getFileString(basePath, fileId);

		//load file
		InputStream	is = null;
		try {
			File file = new File(fileString);
			FileInputStream fis = new FileInputStream(file);
			is = new java.util.zip.GZIPInputStream(fis);
			int data;
			while((data = is.read()) != -1) {
				os.write(data);
			}
		}
		catch(Exception ex) {
			//failure
			return false;
		}
		finally {
			if(is!= null) try {is.close();} catch(Exception ex) {};
		}

		return true;
	}

	/** This method saves from the given input stream to the file given by the file id. */
	private boolean saveFile(String basePath, int fileId, InputStream is) throws Exception {

		String fileString = getFileString(basePath, fileId);

		//open the data file
		OutputStream os = null;
		try {
			File file = new File(fileString);
			
			//make sure the parent exists
			File parent = file.getParentFile();
			if(!parent.exists()) {
				parent.mkdirs();
			}

			//write the file
			FileOutputStream fos = new FileOutputStream(file);
			os = new java.util.zip.GZIPOutputStream(fos);
			int data;
			while((data = is.read()) != -1) {
				os.write(data);
			}
		}
		catch(Exception ex) {
			//failure
			return false;
		}
		finally {
			if(os != null) try {os.close();} catch(Exception ex) {};
		}
		
		return true;
	}
	
	/** This method deletes the file given by the file id. */
	private boolean deleteFile(String basePath, int fileId) throws Exception {

		String fileString = getFileString(basePath, fileId);

		//load file
		try {
			File file = new File(fileString);
			file.delete();
		}
		catch(Exception ex) {
			//failure
			return false;
		}

		return true;
	}
	
	/** This method converts a file id to a path name. */
	private String getFileString(String basePath, long fileId) {
		int[] bytes = new int[8];
		int leadingByte = 0;
		int byteVal;
		for(int i = 0; i < 8; i++) {
			byteVal = (int)((fileId >> 8*i) & 0xff);
			bytes[i] = byteVal;
			if(byteVal > 0) leadingByte = i;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(basePath);
		//head folder
		sb.append(xString(leadingByte+1));
		//each directory
		for(int i = leadingByte; i >= 0; i--) {
			sb.append('/');
			sb.append(Integer.toHexString(bytes[i]));
			sb.append(xString(i));
		}
		
		return sb.toString();
	}

	/** This method makes a string of x's */
	private String xString(int n) {
		String xs = "";
		for(int i = 0; i < n; i++) xs += 'x';
		return xs;
	}

}
