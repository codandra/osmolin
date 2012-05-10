package intransix.indoor.provision.geom;

import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.FeatureTypeInfo;
import intransix.indoor.provision.mapinfo.MapTemplate;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import intransix.indoor.geom.*;

/**
 *
 * @author sutter
 */
public class OsmWay extends OsmFeature {
	
	private final static String WAY_NODES_KEY = "nodes";
	
	
	ArrayList<OsmNode> nodes = new ArrayList<OsmNode>();
	
	public OsmWay(long id) {
		super(id);
	}

	public ArrayList<OsmNode> getNodes() {
		return nodes;
	}
	
	public static void loadOsmWay(long id,
			JSONObject json, 
			MapTemplate mapTemplate,
			MapProvision mapProvision) {
		
		OsmWay way = mapProvision.getOsmWay(id);
		
		//this shouldn't happen. if it does, keep the existing data
		if(way.loaded) return;
		
		//lookup nodes
		JSONArray wayNodes = json.optJSONArray(WAY_NODES_KEY);
		if(wayNodes == null) return;
		
		int cnt = wayNodes.length();
		//make sure there is more than one point
		if(cnt <= 1) return;
		
		long nodeId;
		long zcontext = INVALID_ID;
		int zlevel = OsmLevel.INVALID_ZLEVEL;
		boolean firstNode = true;
		for(int i = 0; i < cnt; i++) {
			nodeId = wayNodes.optLong(i,INVALID_ID);
			if(nodeId == INVALID_ID) continue;
			
			OsmNode node = mapProvision.getOsmNode(nodeId);
			//this shouldn't happen - node is created automatically
			if(node == null) return;

			//set the level info, if it is present
			if(firstNode) {
				//copy the zcontext from the node
				zcontext = node.getZcontext();
				zlevel = node.getZlevel();
				firstNode = false;
			}
			else {
				//if there is disagreement, clear the z info
				if((zcontext != INVALID_ID)&&(zcontext != node.getZcontext())&&(zlevel != node.getZlevel())) {
					zcontext = INVALID_ID;
					zlevel = OsmLevel.INVALID_ZLEVEL;
				}
			}

			//add node
			way.nodes.add(node);
		}
		
		//load the feature properties
		boolean success = way.loadFeatureProperties(json,mapTemplate);
		
		//floag loading completed
		if(success) { 
			way.loaded = true;
		}
		
	}
	
	//========================
	// Protected Methods
	//========================
	@Override
	protected JSONObject getGeometryJson(MapTemplate mapTemplate, AffineTransform lonlatToXY) throws Exception {
		JSONObject geomJson = new JSONObject();
		
		//the object must have a type
		if(fti == null) return null;
		
		//get linestring for json
		JSONArray wayJson = getJsonPointArray(lonlatToXY, mapTemplate.COORDINATE_PRECISION);
		if(wayJson == null) return null;
		
		//load geom type
		int geomType = fti.getDefaultPathType();
		String typeString;
		JSONArray coordJson;
		if(geomType == FeatureTypeInfo.GEOM_TYPE_AREA) {
			//get type
			typeString = "Polygon";
			//get coordinates
			coordJson = new JSONArray();
			coordJson.put(wayJson);
		}
		else if(geomType == FeatureTypeInfo.ALLOWED_TYPE_LINE) {
			//get type
			typeString = "LineString";
			//get coordinates
			coordJson = wayJson;
		}
		else {
			//no geometry
			return null;
		}
		
		geomJson.put("type",typeString);
		geomJson.put("coordinates",coordJson);

		return geomJson;
	}

	private JSONArray getJsonPointArray(AffineTransform lonlatToXY, int precision) throws Exception {
		
		JSONArray pointJsonArray = new JSONArray();
		for(OsmNode node:nodes) {
			JSONArray point = node.getJsonPoint(lonlatToXY,precision);
			if(point != null) {
				pointJsonArray.put(point);
			}
		}
		return pointJsonArray;
	}

			
	
}
