package intransix.indoor.provision.geom;

import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.FeatureTypeInfo;
import intransix.indoor.provision.mapinfo.MapTemplate;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

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
		for(int i = 0; i < cnt; i++) {
			nodeId = wayNodes.optLong(i,INVALID_ID);
			if(nodeId == INVALID_ID) continue;
			
			OsmNode node = mapProvision.getOsmNode(nodeId);
			//if a node is missing, don't load this way
			if(node == null) return;
			
			way.nodes.add(node);
		}
		
		//load the feature properties
		boolean success = way.loadFeatureProperties(json,mapTemplate);
		
		
		//set the geom type
		if(way.fti != null) {
			way.geomType = way.fti.getPathType(json);
		}
		else {
			way.geomType = FeatureTypeInfo.GEOM_TYPE_NONE;
		}
		
		//floag loading completed
		if(success) { 
			way.loaded = true;
		}
		
	}
}
