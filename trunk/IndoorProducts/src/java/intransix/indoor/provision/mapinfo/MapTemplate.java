package intransix.indoor.provision.mapinfo;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import intransix.indoor.provision.geom.OsmObject;


/**
 *
 * @author sutter
 */
public class MapTemplate extends TypeBaseInfo {
	
	public final static String SHELL_TYPE_NAMESPACE = "app";	
	
	public final static int INVALID_VERSION = -1;
	
	public String TYPE_STRUCTURE = "structure";
	public String TYPE_LEVEL = "level";
	public String TYPE_MULTIPOLYGON = "multipolygon";
	public String ROLE_PARENT = "parent";
	public String ROLE_LEVEL = "level";
	public String ROLE_SHELL = "shell";
	public String ROLE_FEATURE = "feature";
	public String KEY_ZLEVEL = "zlevel";
	public String KEY_ZCONTEXT = "zcontext";
	public String KEY_REF_KEY = "ref:key";
	public String KEY_REF = "ref";
	public String KEY_REF_SCOPE_GEOM = "ref:scope:geom";
	public String KEY_REF_SCOPE_REL = "ref:scope:rel";
	
	public int RADIANS_PRECISION = 2;
	public int COORDINATE_PRECISION = 3;
	
	private String name;
	private int version;
	private ArrayList<NamespaceInfo> namespaces = new ArrayList<NamespaceInfo>();
	
	private FeatureTypeInfo shellFeatureType;

	//============================================
	// Public Methods
	//============================================
	
	/** This method loads the map template. */
	public static MapTemplate getMapTemplate(JSONObject json) throws Exception {
		MapTemplate mfi = new MapTemplate();
		
		mfi.loadData(json,null);
		
		mfi.version = json.optInt("version",INVALID_VERSION);
		
		//load configuration constants
		JSONObject nameDefs = json.optJSONObject("nameDefs");
		if(nameDefs != null) {
			mfi.TYPE_STRUCTURE = nameDefs.optString("structureType",mfi.TYPE_STRUCTURE);
			mfi.ROLE_PARENT = nameDefs.optString("parentRole",mfi.ROLE_PARENT);
			mfi.ROLE_LEVEL = nameDefs.optString("levelRole",mfi.ROLE_LEVEL);
			mfi.ROLE_SHELL = nameDefs.optString("shellROle",mfi.ROLE_SHELL);
			mfi.ROLE_FEATURE = nameDefs.optString("featureRole",mfi.ROLE_FEATURE);
			mfi.TYPE_LEVEL = nameDefs.optString("levelType",mfi.TYPE_LEVEL);
			mfi.KEY_ZLEVEL = nameDefs.optString("zlevelKey",mfi.KEY_ZLEVEL);
			mfi.KEY_ZCONTEXT = nameDefs.optString("zcontextKey",mfi.KEY_ZCONTEXT);
			mfi.KEY_REF_KEY = nameDefs.optString("refKeyKey",mfi.KEY_REF_KEY);
			mfi.KEY_REF = nameDefs.optString("refKey",mfi.KEY_REF);
			mfi.KEY_REF_SCOPE_GEOM = nameDefs.optString("refScopeGeomKey",mfi.KEY_REF_SCOPE_GEOM);
			mfi.KEY_REF_SCOPE_REL = nameDefs.optString("refScopeRelKey",mfi.KEY_REF_SCOPE_REL);
		}
		
		//read the output precision values
		mfi.RADIANS_PRECISION = json.optInt("radianPrecision",mfi.RADIANS_PRECISION);
		mfi.COORDINATE_PRECISION = json.optInt("coordinatePrecision",mfi.COORDINATE_PRECISION);
		
		//load namespaces
		JSONArray ns = json.getJSONArray("namespace");
		int cnt = ns.length();
		for(int i = 0; i < cnt; i++) {
			JSONObject niJson = ns.getJSONObject(i);
			NamespaceInfo ni = NamespaceInfo.getNamespaceInfo(niJson,mfi);
			mfi.namespaces.add(ni);
		}
		
		JSONObject shellTypeInfo = json.getJSONObject("shell");
		if(shellTypeInfo != null) {
			mfi.shellFeatureType = FeatureTypeInfo.getGeomInfo(shellTypeInfo,null,SHELL_TYPE_NAMESPACE);
		}
		
		return mfi;
	}
	
	/** This method returns the template version number. */
	public int getVersion() {
		return version;
	}
	
	/** This method gets the feature type info for a given feature. */
	public FeatureTypeInfo getFeatureTypeInfo(JSONObject featureJson) {
				
		for(NamespaceInfo ni:namespaces) {
			String key = ni.getName();
			String type = OsmObject.getTagString(featureJson,key);
			if(type != null) {
				FeatureTypeInfo fti = ni.getFeatureTypeInfo(featureJson, type);
				if(fti != null) {
					return fti;
				}
			}
		}

		//none found
		return null;
	}
	
	/** This method returns the feature type for the shell object. */
	public FeatureTypeInfo getShellFeatureType() {
		return shellFeatureType;
	}
}
