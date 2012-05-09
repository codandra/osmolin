/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.indoor.provision.mapinfo;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class TypeBaseInfo {
	
	public final static int GEOM_TYPE_NONE = -1;
	public final static int GEOM_TYPE_POINT = 0;
	public final static int GEOM_TYPE_LINE = 1;
	public final static int GEOM_TYPE_AREA = 2;
	
	public final static int ICON_NONE = 0;
	public final static int ICON_POINT = 1;
	public final static int ICON_ALL = 2;
	
	public final static int ALLOWED_TYPE_POINT = 1;
	public final static int ALLOWED_TYPE_LINE = 2;
	public final static int ALLOWED_TYPE_AREA = 4;

	public final static int DEFAULT_ALLOWED_TYPES = ALLOWED_TYPE_POINT | ALLOWED_TYPE_LINE | ALLOWED_TYPE_AREA;
	public final static int DEFAULT_PATH_TYPE = GEOM_TYPE_AREA;
	public final static int DEFAULT_ZINDEX = 0;
	public final static boolean DEFAULT_HAS_CHILDREN = false;
	public final static int DEFAULT_ICON_INFO = ICON_NONE;
	
	private String name;
	private int allowedTypes;
	private int defaultPathType;
	private int zorder;
	private ArrayList<PropertyKey> properties = new ArrayList<PropertyKey>();
	
	//============================================
	// Public Methods
	//============================================
	
	/** This method loads a JSON into the object. */
	public void loadData(JSONObject json,
			TypeBaseInfo parent) throws Exception {
		this.name = json.getString("name");
		
		int defaultAllowedType = (parent != null) ? parent.getAllowedTypes() : DEFAULT_ALLOWED_TYPES;
		int defaultDeafultPathType = (parent != null) ? parent.getDefaultPathType() : DEFAULT_PATH_TYPE;
		int defaultZindex = (parent != null) ? parent.getZorder() : DEFAULT_ZINDEX;
		
		this.allowedTypes = json.optInt("allowedTypes",defaultAllowedType);
		this.defaultPathType = json.optInt("defaultPath",defaultDeafultPathType);
		this.zorder = json.optInt("zorder",defaultZindex);
		
		//do children
		this.properties = new ArrayList<PropertyKey>();
		if(parent != null) this.properties.addAll(parent.properties);
		if(json.has("properties")) {
			JSONArray pks = json.getJSONArray("properties");
			int cnt = pks.length();
			for(int i = 0; i < cnt; i++) {
				JSONObject pkJson = pks.getJSONObject(i);
				PropertyKey pk = PropertyKey.getPropertyKey(pkJson);
				this.properties.add(pk);
			}
		}
		
		
	}
	
	/** This method gets the name. */
	public String getName() {
		return name;
	}
		
	/** This method gets the allowed geometry types. */
	public int getAllowedTypes() {
		return allowedTypes;
	}
	
	/** This method gets the default type, area or path, for a line. */
	public int getDefaultPathType() {
		return defaultPathType;
	}
	
	/** This method gets the zorder, the relative stack order for the geometry. */
	public int getZorder() {
		return zorder;
	}
	
	/** This method gets the property list. */
	public ArrayList<PropertyKey> getProperties() {
		return properties;
	}
}
