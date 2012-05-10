package intransix.indoor.provision;


import intransix.indoor.provision.mapinfo.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import intransix.indoor.provision.geom.*;

/**
 *
 * @author sutter
 */
public class MapProvision {

	//============================
	// Constants
	//============================
			
	private final static String MAP_TYPE_INFO_NAME = "maptemplate";
	private final static String MAP_UPLOAD_NAME = "indoormap";
	private final static String LVLGEOM_UPLOAD_NAME = "lvlgeom";

	//key in osm data download
	private final static String ELEMENTS_KEY = "elements";

	//============================
	// Private Properties
	//============================

	private DataLoader dataLoader;
	private MapTemplate mapTemplate;
	
	private HashMap<Long,OsmNode> nodeMap = new HashMap<Long,OsmNode>();
	private HashMap<Long,OsmWay> wayMap = new HashMap<Long,OsmWay>();
	private HashMap<Long,OsmLevel> levelMap = new HashMap<Long,OsmLevel>();
	private HashMap<Long,OsmMap> mapMap = new HashMap<Long,OsmMap>();
	private HashMap<Long,OsmMultipoly> polyMap = new HashMap<Long,OsmMultipoly>();

	//============================
	// Public Methods
	//============================

	public MapProvision(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	public void createAndUpload(String mapTemplateName, String structureId, String name) throws Exception {
		
		//get the map template
		loadTemplate(mapTemplateName);
		
		//store input
		loadVenueInfo(structureId,name);

		//this is only needed if the level relation did not include the features on the level
		populateLevels();
		
		//create the transforms
		for(OsmMap map:mapMap.values()) {
			map.finalizeMap();
		}

//if(true) throw new Exception("Test finished");

		//lookup version fo outgoing data
		FileAccess fileAccess = dataLoader.getFileAccess();

		//create the data files
		for(OsmMap map:mapMap.values()) {
			//make sure the map is loaded
			if(map.getIsLoaded()) {
				//get the version for this map
				String mapIdString = String.valueOf(map.getId());
				int version = fileAccess.getVersion(MAP_UPLOAD_NAME,mapIdString);

				//set map version
				map.setVersion(version);

				//get the map object
				JSONObject mapJson = map.getMapJson(mapTemplate);
				fileAccess.upload(MAP_UPLOAD_NAME,mapIdString,version,mapJson);

				for(OsmLevel level:map.getLevels()) {

					//make sure the level are loaded
					if(level.getIsLoaded()) {
						//get the level object
						JSONObject levelJson = level.getLevelGeoJson(mapTemplate);
						if(levelJson != null) {
							String lidString = String.valueOf(level.getId());
							fileAccess.upload(LVLGEOM_UPLOAD_NAME,lidString,version,levelJson);
						}
						else {
							throw new Exception("There was an error creating the json for level id = " + level.getId());
						}
					}
					else {
						throw new Exception("There was an error loading level id = " + level.getId());
					}
				}
			}
			else {
				throw new Exception("There was an error loading the map id = " + map.getId());
			}
		}	

	}
	
		public OsmNode getOsmNode(Long id) {
		OsmNode osmNode = nodeMap.get(id);
		if(osmNode == null) {
			osmNode = new OsmNode(id);
			nodeMap.put(id,osmNode);
		}
		return osmNode;
	}
	
	public OsmWay getOsmWay(Long id) {
		OsmWay osmWay = wayMap.get(id);
		if(osmWay == null) {
			osmWay = new OsmWay(id);
			wayMap.put(id,osmWay);
		}
		return osmWay;
	}
	
	public OsmLevel getOsmLevel(Long id) {
		OsmLevel osmLevel = levelMap.get(id);
		if(osmLevel == null) {
			osmLevel = new OsmLevel(id);
			levelMap.put(id,osmLevel);
		}
		return osmLevel;
	}
	
	public OsmMap getOsmMap(Long id) {
		OsmMap osmMap = mapMap.get(id);
		if(osmMap == null) {
			osmMap = new OsmMap(id);
			mapMap.put(id,osmMap);
		}
		return osmMap;
	}
	
	public OsmMultipoly getOsmMultipoly(Long id) {
		OsmMultipoly osmMultipoly = polyMap.get(id);
		if(osmMultipoly == null) {
			osmMultipoly = new OsmMultipoly(id);
			polyMap.put(id,osmMultipoly);
		}
		return osmMultipoly;
	}
	
	//====================================
	// Private Functions
	//====================================
	
	/** This method loads the map feature type information. */
	private void loadTemplate(String mapTemplateName) throws Exception {
		FileAccess fileAccess = dataLoader.getFileAccess();
		JSONObject mfiJson = fileAccess.getFileData(MAP_TYPE_INFO_NAME,mapTemplateName);
		mapTemplate = MapTemplate.getMapTemplate(mfiJson);
	}
	
	/** This load the venue info. */
	private void loadVenueInfo(String mapId, String name) throws Exception {
		JSONObject inputData = dataLoader.getOsmData(mapId,name);
		
		JSONArray elements = inputData.getJSONArray(ELEMENTS_KEY);
		
		int cnt = elements.length();
		
		for(int i = 0; i < cnt; i++) {
			final JSONObject element = elements.getJSONObject(i);
			OsmObject.loadOsmObject(element, mapTemplate, this);
		}
	}

	/** This method adds the nodes and ways to a level if te info is marked on the node. This should
	 * be OK if the info is instead (or also) on the relation. */
	private void populateLevels() {
		//create a map of levels by zcontext+zlevel
		HashMap<String,OsmLevel> zlevelMap = new HashMap<String,OsmLevel>();
		for(OsmMap map:mapMap.values()) {
			if(!map.getIsLoaded()) continue;

			long zcontext = map.getId();
			for(OsmLevel level:map.getLevels()) {
				if(!level.getIsLoaded()) continue;
				
				int zlevel = level.getZlevel();
				String key = OsmLevel.createZlevelKey(zcontext,zlevel);
				zlevelMap.put(key, level);
		
//RELATION DEFINED LEVELS////////////////////////////////
				//any existing features in this level do not have the zcontext set!!!
				for(OsmFeature feature:level.getFeatures()) {
					feature.setZcontext(zcontext);
				}
/////////////////////////////////////////////////////
			}
		}
		
//NODE DEFINED LEVELS////////////////////////////////
//if the feature levels are defined in the level relation, we will re-add all the level features
//but that shouldn't be a problem because we use a hash set.
		//polulate levels with features
		for(OsmFeature feature:nodeMap.values()) {
			addFeatureToLevel(feature,zlevelMap);
		}
		for(OsmFeature feature:wayMap.values()) {
			addFeatureToLevel(feature,zlevelMap);
		}
/////////////////////////////////////////////////////
	}

	private void addFeatureToLevel(OsmFeature feature, HashMap<String,OsmLevel> zlevelMap) {
		if(feature.placeInMap()) {
			String key = OsmLevel.createZlevelKey(feature.getZcontext(),feature.getZlevel());
			if(key != null) {
				OsmLevel level = zlevelMap.get(key);
				if(level != null) {
					//add feature to level
					level.addFeature(feature);
				}
			}
		}
	}
}
