/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.indoor.provision.geom;

import org.json.*;
import intransix.indoor.provision.mapinfo.*;
import intransix.indoor.provision.MapProvision;

/**
 *
 * @author sutter
 */
public class OsmObject {
	
	//===================
	// Constants
	//===================
	
	public final static String REF_KEY = "ref";
	public final static long INVALID_ID = -1;
	
	public final static String RELATION_TYPE = "relation";
	public final static String WAY_TYPE = "way";
	public final static String NODE_TYPE = "node";
	
	public final static String TAGS_KEY = "tags";
	public final static String YES_VALUE = "yes";
	public final static String TRUE_VALUE = "true";
	public final static String NO_VALUE = "no";
	public final static String FALSE_VALUE = "false";
	
	public final static String ELEMENTS_KEY = "elements";
	public final static String ID_KEY = "id";
	public final static String TYPE_KEY = "type";
	
	public final static String NAME_KEY = "name";
	
	
	private final static String MAP_TYPE_INFO_NAME = "maptemplate";
	private final static String MAP_UPLOAD_NAME = "indoormap";
	private final static String LVLGEOM_UPLOAD_NAME = "lvlgeom";	
	
	
	
	
	
	private final static String DEFAULT_ANGLE_KEY = "default_angle";
	
	//========================
	// Properties
	//========================
	long id;
	boolean loaded = false;
	
	//========================
	// Public Methods
	//========================
	
	public OsmObject(long id) {
		this.id = id;
		this.loaded = false;
	}
	
	public static void loadOsmObject(JSONObject json, 
			MapTemplate mapTemplate,
			MapProvision mapProvision) {
		
		final String type = json.optString(TYPE_KEY,null);
		if(type == null) return;
		
		final long id = json.optLong(ID_KEY,INVALID_ID);
		if(id == INVALID_ID) return;
		
		if(type.equalsIgnoreCase(NODE_TYPE)) {
			OsmNode.loadOsmNode(id,json,mapTemplate,mapProvision);
		}
		else if(type.equalsIgnoreCase(NODE_TYPE)) {
			OsmWay.loadOsmWay(id,json,mapTemplate,mapProvision);
		}
		else if(type.equalsIgnoreCase(NODE_TYPE)) {
			OsmRelation.loadOsmRelation(id,json,mapTemplate,mapProvision);
		}
	}
	
	
	public static String getTagString(JSONObject element, String tag) {
		final JSONObject tags = element.optJSONObject(TAGS_KEY);
		if(tags == null) return null;
		return tags.optString(tag,null);
	}
	public static int getTagInt(JSONObject element, String tag, int defaultValue) {
		final JSONObject tags = element.optJSONObject(TAGS_KEY);
		if(tags == null) return defaultValue;
		return tags.optInt(tag,defaultValue);
	}
	public static double getTagDouble(JSONObject element, String tag, double defaultValue) {
		final JSONObject tags = element.optJSONObject(TAGS_KEY);
		if(tags == null) return defaultValue;
		return tags.optDouble(tag,defaultValue);
	}
	public static boolean getTagBoolean(JSONObject element, String tag, boolean defaultValue) {
		String val = getTagString(element,tag);
		if(val != null) {
			if((val.equalsIgnoreCase(YES_VALUE))||(val.equalsIgnoreCase(TRUE_VALUE))) {
				return true;
			}
			if((val.equalsIgnoreCase(NO_VALUE))||(val.equalsIgnoreCase(FALSE_VALUE))) {
				return false;
			}
		}
		//if we get here, no understood value found
		return defaultValue;
	}
	
}
