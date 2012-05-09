package data;

import java.io.*;
import javax.servlet.ServletContext;

import javax.naming.InitialContext;
import java.sql.*;
import javax.sql.DataSource;


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
}
