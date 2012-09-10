package data;

import java.io.*;
import javax.servlet.ServletContext;

import javax.naming.InitialContext;
import java.sql.*;
import javax.servlet.http.HttpServletRequest;
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
	
	/** This method gets the given context parameter. */
	public static String getContextParam(String tag, ServletContext context) throws Exception {
		String stringValue = context.getInitParameter(tag);
		if(stringValue == null) {
			throw new Exception("Context value not set: " + tag);
		}
		return stringValue;
	}
	
	/** This method returns the list of parameters that appear after the base URL
	 * pattern. 
	 * 
	 * @param request	The http request
	 * @return			An array of string parameters.
	 */
	public static String[] getPathParameters(HttpServletRequest request) {
		String path = request.getPathInfo();
		if(path.charAt(0) == '/') {
			path = path.substring(1);
		}

		String[] params = path.split("/");
		return params;
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
		String jsonString = readText(is);
		if(jsonString.length() > 0) {
			return new JSONObject(jsonString);
		}
		else {
			return null;
		}
	}
	
	public static void writeJson(OutputStream os, JSONObject json) throws Exception {
		String data = json.toString();
		writeText(os,data);
	}
	
	public static String readText(InputStream is) throws Exception {
		InputStreamReader in = new InputStreamReader(is, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		StringBuilder sb = new StringBuilder();
		while(true) {
			String data = reader.readLine();
			if(data == null) break;
			sb.append(data);
		}
		return sb.toString();
	}
	
	public static void writeText(OutputStream os, String text) throws Exception {
		OutputStreamWriter out = new OutputStreamWriter(os, "UTF-8");
		out.write(text, 0, text.length());
		out.flush();
		out.close();
	}
	
	
}
