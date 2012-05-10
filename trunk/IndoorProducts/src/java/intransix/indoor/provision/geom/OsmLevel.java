/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.indoor.provision.geom;

import java.util.*;
import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.MapTemplate;
import intransix.indoor.geom.AffineTransform;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author sutter
 */
public class OsmLevel extends OsmRelation implements Comparable<OsmLevel> {
	
	public final static int INVALID_ZLEVEL = Integer.MIN_VALUE;
	
	String name;
	int version;
	OsmMap map = null;
	int zlevel = INVALID_ZLEVEL;
	OsmFeature shell;
	HashSet<OsmFeature> features = new HashSet<OsmFeature>();
	
	public OsmLevel(long id) {
		super(id);
	}

	public int getZlevel() {
		return zlevel;
	}

	public void addFeature(OsmFeature feature) {
		this.features.add(feature);
	}

	public HashSet<OsmFeature> getFeatures() {
		return features;
	}

	public void setMap(OsmMap map) {
		this.map = map;
	}

//	public OsmMap getMap() {
//		return map;
//	}

	/** This method creates a key object for looking up zcontext and zlevel. If INVALID_ID is passed in
	 * for zcontext or INVALID_ZLEVEL is passed in for zlevel, null is returned.*/
	public static String createZlevelKey(long zcontext, int zlevel) {
		if((zcontext == INVALID_ID)||(zlevel == INVALID_ZLEVEL)) return null;
		else return String.valueOf(zcontext) + ":" + zlevel;
	}
	
	public static void loadOsmLevel(long id,
			JSONObject json, 
			MapTemplate mapTemplate,
			MapProvision mapProvision) {
		
		OsmLevel level = mapProvision.getOsmLevel(id);
		
		//get the zlevel
		level.zlevel = getTagInt(json,mapTemplate.KEY_ZLEVEL,0);
		
		//load the name
		level.name = getTagString(json,NAME_KEY);
		if(level.name == null) level.name = String.valueOf(id);
		
		//load the level
		level.loadMembers(json, mapTemplate, mapProvision);
		
		//flag as loaded
		level.loaded = true;
	}
	
	public JSONObject getMapJsonEntry(MapTemplate mapTemplate) throws Exception {
		JSONObject indoorLevel = new JSONObject();
		indoorLevel.put("id",getId());
		indoorLevel.put("z",zlevel);
		indoorLevel.put("nm",name);
		return indoorLevel;
	}
	
	public JSONObject getLevelGeoJson(MapTemplate mapTemplate) throws Exception {

		JSONObject levelGeoJson = new JSONObject();
		levelGeoJson.put("id",getId());
		levelGeoJson.put("mid",map.getId());
		levelGeoJson.put("v",map.getVersion());
		levelGeoJson.put("l","en"); //fix this!!!
		levelGeoJson.put("ft","lvlg1");

		//add the geometry
		ArrayList<OsmFeature> featureList = getOrderedFeatureList();
		JSONArray featureJsonArray = new JSONArray();
		AffineTransform lonlatToXY = map.getLonLatToXY();
		for(OsmFeature feature:featureList) {
			JSONObject featureJson = feature.getJsonObject(mapTemplate,lonlatToXY);
			if(featureJson != null) {
				featureJsonArray.put(featureJson);
			}
		}
		levelGeoJson.put("features", featureJsonArray);
		
		return levelGeoJson;
	}
	
	@Override
	protected void loadMember(long memberId, String type, String role,
			MapTemplate mapTemplate, MapProvision mapProvision) {
		
		if(role.equalsIgnoreCase(mapTemplate.ROLE_SHELL)) {
			//try to load the parent
			if(type.equalsIgnoreCase(NODE_TYPE)) {
				this.shell = mapProvision.getOsmNode(memberId);
			}
			else if(type.equalsIgnoreCase(OsmObject.WAY_TYPE)) {
				this.shell = mapProvision.getOsmWay(memberId);
			}
			else {
				this.shell = null;
			}
		}
		else if(role.equalsIgnoreCase(mapTemplate.ROLE_FEATURE)) {
//RELATION DEFINED LEVELS///////////////////////////////
			//this will only be present if features are included in the level relation
			//alternatively, thenode should specify the zlevel and zcontext
			
			//try to load the feature
			OsmFeature feature = null;
			if(type.equalsIgnoreCase(NODE_TYPE)) {
				feature = mapProvision.getOsmNode(memberId);
			}
			else if(type.equalsIgnoreCase(OsmObject.WAY_TYPE)) {
				feature = mapProvision.getOsmWay(memberId);
			}
			if(feature != null) {
				//we might not know zcontext yet!!! Set that later.
				feature.setZlevel(zlevel);
				this.addFeature(feature);
			}
///////////////////////////////////////////////////////////////////
		}
	}
	
	@Override
	public int compareTo(OsmLevel level) {
		//return negative is this level is less that passed level.
		return this.zlevel - level.zlevel; 
	}
	
	/** This method gets the array of features, ordered by draw order. */
	private ArrayList<OsmFeature> getOrderedFeatureList() {
		ArrayList<OsmFeature> featureList = new ArrayList();
		featureList.addAll(this.features);
		Collections.sort(featureList);
		return featureList;
	}	
	
}
