package intransix.indoor.provision.mapinfo;

import java.util.HashMap;
import org.json.*;

/**
 *
 * @author sutter
 */
public class NamespaceInfo extends TypeBaseInfo {
	
	private int version;
	private HashMap<String,FeatureTypeInfo> featureTypeInfoMap = new HashMap<String,FeatureTypeInfo>();

	//============================================
	// Public Methods
	//============================================
	
	/** This method loads a namespace info object from a json. 
	 * 
	 * @param json			The object to load from
	 * @param mt			The map template for the namespace
	 * @return				A NamespaceInfo object
	 * @throws Exception 
	 */
	public static NamespaceInfo getNamespaceInfo(JSONObject json, MapTemplate mt) throws Exception {
		NamespaceInfo ni = new NamespaceInfo();
		ni.loadData(json, mt);
		ni.version = json.getInt("version");
		
		JSONArray ftInfos = json.getJSONArray("mapFeatures");
		int cnt = ftInfos.length();
		for(int i = 0; i < cnt; i++) {
			JSONObject ftiJson = ftInfos.getJSONObject(i);
			FeatureTypeInfo fti = FeatureTypeInfo.getGeomInfo(ftiJson,ni,ni.getName());
			ni.featureTypeInfoMap.put(fti.getName(),fti);
		}

		return ni;
	}
	
	/** This method gets the version. */
	public int getVersion() {
		return version;
	}
	
	/** This method returns the feature type appropriate for the given json object and name. */
	public FeatureTypeInfo getFeatureTypeInfo(JSONObject featureJson, String name) {
		return featureTypeInfoMap.get(name);
	}
	
	//============================================
	// Private Methods
	//============================================
	
	private NamespaceInfo() {
	}
	
}
