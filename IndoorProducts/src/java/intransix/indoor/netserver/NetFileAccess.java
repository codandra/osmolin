package intransix.indoor.netserver;

import java.util.HashMap;
import intransix.indoor.provision.FileAccess;
import intransix.indoor.provision.DataLoader;
import org.json.*;

/**
 *
 * @author sutter
 */
public class NetFileAccess implements FileAccess {
	
	private final static String SERVER_TAG = "net_file_server";
	
	private HashMap<String,String> paramMap = new HashMap<String,String>();
	
	public NetFileAccess() {
		paramMap.put(SERVER_TAG,"");
	}
	
	@Override
	public HashMap<String,String> getParamMap() {
		return paramMap;
	}

	@Override
	public JSONObject getFileData(String fileName, String key) throws Exception {
		String indoorServer = paramMap.get(SERVER_TAG);
		String url = indoorServer + "/file/" + fileName + "/" + key;
		
		JSONObject result = DataLoader.request(url,null,"GET");
		
		return result;
	}
	
	@Override
	public JSONObject getFileData(String fileName, String key, String version) throws Exception {
		String indoorServer = paramMap.get(SERVER_TAG);
		String url = indoorServer + "/file/" + fileName + "/" + key + "/" + version;
		
		JSONObject result = DataLoader.request(url,null,"GET");
		
		return result;
	}
		
	@Override
	public int getVersion(String fileName, String key) throws Exception {
		String indoorServer = paramMap.get(SERVER_TAG);
		String url = indoorServer + "/version/" + fileName + "/" + key;
		
		JSONObject result = DataLoader.request(url,null,"GET");
		int version = result.getInt("version");
		
		return version;
	}
		
	@Override
	public void upload(String fileName, String key, 
			int version, JSONObject data)throws Exception {
		String indoorServer = paramMap.get(SERVER_TAG);
		String url = indoorServer + "/file/" + fileName + "/" + key + "/" + version;
		
		DataLoader.request(url,data,"PUT");
	}
	
	
}
