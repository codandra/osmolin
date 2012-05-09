/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.indoor.provision.geom;

import java.util.*;
import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.MapTemplate;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class OsmLevel extends OsmRelation {
	
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

	public OsmMap getMap() {
		return map;
	}

	/** This method creates a key object for looking up zcontext and zlevel. If INVALID_ID is passed in
	 * for zcontext or INVALID_ZLEVEL is passed in for zlevel, null is returned.*/
	public static String createZlevelKey(long zcontext, int zlevel) {
		if((zcontext == INVALID_ID)||(zlevel == INVALID_ZLEVEL)) return null;
		else return String.valueOf(zcontext) + ":" + zlevel;
	}

	public JSONObject getLevelJson() {
		return null;
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
			if((feature != null)&&(map != null)) {
				feature.setZInfo(map.getId(),zlevel);
				this.addFeature(feature);
			}
		}
	}

//	/** This method creates a level object. */
//	private void createLevel(JSONObject osmLevel, JSONArray indoorLevelList) throws Exception {
//		long id = osmLevel.getLong("id");
//		int zlevel = PropertyUtils.getTagInt(osmLevel,MapTemplate.KEY_ZLEVEL,0);
//		String name = PropertyUtils.getTagString(osmLevel,NAME_KEY);
//
//		//create the levle
//		JSONObject indoorLevel = new JSONObject();
//		indoorLevel.put("id",id);
//		indoorLevel.put("z",zlevel);
//		if(name == null) name = "unnamed";
//		indoorLevel.put("nm",name);
//		indoorLevelList.put(indoorLevel);
//
//		//create the level geometry
//		JSONObject levelGeoJson = new JSONObject();
//		levelGeoJson.put("id",id);
//		levelGeoJson.put("mid",mapId);
//		levelGeoJson.put("l","en"); //fix this!!!
//		levelGeoJson.put("ft","lvlg1");
//		//version added later
//
//		//add the geometry
//		ArrayList<JSONObject> featureList = new ArrayList<JSONObject>();
//
//		JSONArray members = osmLevel.getJSONArray(MEMBERS_KEY);
//
//		//get nodes and ways
//		int cnt = members.length();
//		for(int i = 0; i < cnt; i++) {
//			addFeatureGeoJson(members.getJSONObject(i),featureList);
//		}
//
//		//process the multipolygons
//		for(OsmMultipoly mpr:multipolygons) {
//			addMultipolyFeatureGeoJson(mpr,featureList);
//		}
//
//		//sort the geometry by zindex
//		Collections.sort(featureList,new JsonIntFieldComparator("zorder",DEFAULT_ZLEVEL_FOR_SORT));
//
//		levelGeoJson.put("type","FeatureCollection");
//		JSONArray featureJsonArray = new JSONArray();
//		levelGeoJson.put("features",featureJsonArray);
//		for(JSONObject f:featureList) {
//			featureJsonArray.put(f);
//		}
//
//		levelGeomObjects.add(levelGeoJson);
//
//	}
	
}
