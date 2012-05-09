package intransix.indoor.provision;

import java.net.URL;
import java.net.HttpURLConnection;
import org.json.*;
import java.io.*;
import java.util.HashMap;
import javax.servlet.ServletContext;

/**
 *
 * @author sutter
 */
public class DataLoader {
	
	private String osmUrl;
	private FileAccess fileAccess;
	
	public DataLoader() {
	}
	
	public void init(ServletContext context) throws Exception {
		//get osm params
		osmUrl = context.getInitParameter("osm_url");
		if(osmUrl == null) {
			throw new Exception("OSM URL not set!");
		}
		
		//create file access
		String fileClass = context.getInitParameter("file_access_class");
		if(fileClass == null) {
			throw new Exception("File access class not set!");
		}
		fileAccess = (FileAccess)Class.forName(fileClass).newInstance();
		
		//initialize fiel access parameters
		HashMap<String,String> params = fileAccess.getParamMap();
		for(String key:params.keySet()) {
			String keyValue = context.getInitParameter(key);
			params.put(key,keyValue);
		}
	}

	/** This method gets the data for the given id or mapName. If the id is not null it
	 * uses that parameter to load the data. Otherwise it uses the name. If neither the name
	 * or the id are set an exception is thrown. */
	public JSONObject getOsmData(String id, String mapName) throws Exception {
		StringBuilder sb = new StringBuilder();
//		sb.append("[out:json];");
//		sb.append("(rel");
//		if(id != null) {
//			sb.append("(");
//			sb.append(id);
//			sb.append(");");
//		}
//		else if(mapName != null) {
//			sb.append("[\"name\"=\"");
//			sb.append(mapName);
//			sb.append("\"];");
//		}
//		else {
//			throw new Exception("The ID or name must be set.");
//		}
//		sb.append("rel(r););");
//		sb.append("(._;way(r););");
//		sb.append("(._;node(r););");
//		sb.append("(._;node(w););");
//		sb.append("(._;rel(w););");
//		sb.append("out body;");
//		String data = sb.toString();
		
		sb.append("<osm-script output='json'>");
		sb.append("<union into='_'>");
		
		if(id != null) {
			sb.append(String.format("<id-query into='_' ref='%s' type='relation'/>",id));
		}
		else {
			sb.append(String.format("<query into='_' type='relation'><has-kv k='name' v='%s'/></query>",mapName));
		}
		
		sb.append("<recurse from='_' into='x' type='relation-relation'/>");

		sb.append("</union><union into='_'>"
				+ "<item set='_'/>"
				+ "<recurse from='_' into='x' type='relation-way'/>"
				+ "<recurse from='_' into='x' type='relation-node'/>"
				+ "</union>"
				+ "<union into='_'>"
				+ "<item set='_'/>"
				+ "<recurse from='_' into='x' type='way-node'/>"
				+ "<recurse from='_' into='x' type='way-relation'/>"
				+ "</union>");
		sb.append("<print/></osm-script>");
		
		String data = sb.toString();
		
		String encodedData = java.net.URLEncoder.encode(data,"UTF-8");
		
		String url = osmUrl + "?data=" + encodedData;
		
//url = "http://localhost:8080/IndoorProducts/mgwmall.json";		
		
		JSONObject result = request(url,null,"GET");
		
		return result;
	}
	
	public FileAccess getFileAccess() {
		return fileAccess;
	}
	
	//=======================
	// Network Request Methods
	//=======================
	
	public static JSONObject request(String stringUrl, JSONObject jsonRequest, String method) throws Exception {
		URL url = new URL(stringUrl);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod(method);

		//set output flags
		conn.setDoInput(true);
		
		//write output
		if(jsonRequest != null) {
			conn.setDoOutput(true);
			OutputStream os = null;
			os = conn.getOutputStream();
			writeJSON(os,jsonRequest);
		}
		else {
			conn.setDoOutput(false);
		}

		int responseCode = conn.getResponseCode();
		if(responseCode != 200) {
			throw new Exception("Error in request: " + responseCode + ":" + conn.getResponseMessage());
		}
		//read input
		JSONObject jsonResponse = null;
		InputStream is = null;
		is = conn.getInputStream();
		jsonResponse = readJSON(is);
		
		return jsonResponse;
	}

	public static JSONObject readJSON(InputStream is) throws Exception {
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
	
	public static void writeJSON(OutputStream os, JSONObject json) throws Exception {
		OutputStreamWriter out = new OutputStreamWriter(os, "UTF-8");
		String data = json.toString();
		out.write(data, 0, data.length());
		out.flush();
		out.close();
	}
}

