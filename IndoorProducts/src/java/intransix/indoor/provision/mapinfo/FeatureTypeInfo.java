package intransix.indoor.provision.mapinfo;

import intransix.indoor.provision.geom.OsmObject;
import java.util.*;
import org.json.*;

/**
 *
 * @author sutter
 */
public class FeatureTypeInfo extends TypeBaseInfo {
	
	private final static String AREA_KEY = "area";
	
	private String typeName;
	private String namespaceName;
	
	//============================================
	// Public Methods
	//============================================
	
	/** This method loads a feature type from a json.
	 * 
	 * @param json				The json to read from
	 * @param parent			The parent TypeBaseInfo. This should be the namespace. Null is OK.
	 * @param namespaceName		The namespace for the feature info.
	 * @return					The FeatureTypeInfo object
	 * @throws Exception 
	 */
	public static FeatureTypeInfo getGeomInfo(JSONObject json,
			TypeBaseInfo parent, String namespaceName) throws Exception {
		FeatureTypeInfo fti = new FeatureTypeInfo();
		fti.loadData(json,parent);
		fti.typeName = namespaceName + ":" + json.optString("outName",fti.getName());
		fti.namespaceName = namespaceName;
		
		return fti;
	}
	
	/** This method gives the object type for the given way. It checks if the area or line is
	 * specified and if not it uses the default value. If an illegal value is specifieid, none is
	 * returned. */
	public int getPathType(JSONObject osmJson) {
		boolean defaultIsArea = (getDefaultPathType() == GEOM_TYPE_AREA);
		boolean isArea = OsmObject.getTagBoolean(osmJson,AREA_KEY,defaultIsArea);
		int allowedTypes = this.getAllowedTypes();
		if(isArea) {
			//check if this is labeled as an area
			if((allowedTypes | FeatureTypeInfo.ALLOWED_TYPE_AREA) != 0) return FeatureTypeInfo.GEOM_TYPE_AREA;
			else return FeatureTypeInfo.GEOM_TYPE_NONE;
		}
		else {
			//check if this is labeled as a line
			if((allowedTypes | FeatureTypeInfo.ALLOWED_TYPE_LINE) != 0) return FeatureTypeInfo.GEOM_TYPE_LINE;
			else return FeatureTypeInfo.GEOM_TYPE_NONE;
		}
	}
	
	public boolean getIsPointAllowed() {
		return ((this.getAllowedTypes() | FeatureTypeInfo.ALLOWED_TYPE_POINT) != 0);
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public String getNamespaceName() {
		return namespaceName;
	}
	
	//============================================
	// Private Methods
	//============================================
	
	private FeatureTypeInfo() {
		
	}
	
	
}
