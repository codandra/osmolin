package intransix.indoor.provision;

import java.util.HashMap;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public interface FileAccess {
	
	HashMap<String,String> getParamMap();
	
	JSONObject getFileData(String fileName, 
			String key) 
			throws Exception;
	
	JSONObject getFileData(String fileName, 
			String key, 
			String version) 
			throws Exception;
		
	int getVersion(String fileName,
			String key)
			throws Exception;
		
	void upload(String fileName, String key, 
			int version, JSONObject data) throws Exception;
}
