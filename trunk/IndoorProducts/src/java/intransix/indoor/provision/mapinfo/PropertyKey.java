package intransix.indoor.provision.mapinfo;

import org.json.*;

/**
 *
 * @author sutter
 */
public class PropertyKey {
	/** This is the key for the property in the OSM data. */
	public String osmKey;
	
	/** This is the key that should be used in the app geoJSON. It defaults
	 to the value of the osmKey. */
	public String appKey;
	
	/** This field determines if the property from this key should be used
	 * if a property of the same naem already exists in the app geoJSON. */
	public boolean replace = true;
	
	/** This method loads the contents of a JSON into the object. */
	public static PropertyKey getPropertyKey(JSONObject json) throws Exception {
		PropertyKey pk = new PropertyKey();
		pk.osmKey = json.getString("key");	
		pk.appKey = json.optString("appKey",pk.osmKey);
		pk.replace = json.optBoolean("replace",true);
		return pk;
	}
}
