package intransix.indoor.provision.geom;

import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.FeatureTypeInfo;
import intransix.indoor.provision.mapinfo.MapTemplate;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class OsmNode extends OsmFeature {
	
	private final static String LAT_KEY = "lat";
	private final static String LON_KEY = "lon";
	
	public final static double INVALID_ANGLE = 720;
	
	double lat;
	double lon;
	
	public OsmNode(long id) {
		super(id);
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}
	
	public static void loadOsmNode(long id,
			JSONObject json, 
			MapTemplate mapTemplate,
			MapProvision mapProvision) {
		
		OsmNode node = mapProvision.getOsmNode(id);
		
		//this shouldn't happen. if it does, keep the existing data
		if(node.loaded) return;
		
		node.lat = json.optDouble(LAT_KEY,INVALID_ANGLE);
		if(node.lat == INVALID_ANGLE) return;
		
		node.lon = json.optDouble(LAT_KEY,INVALID_ANGLE);
		if(node.lon == INVALID_ANGLE) return;
		
		//try to read the zlevel
		node.zlevel = OsmNode.getTagInt(json, mapTemplate.KEY_ZLEVEL,OsmLevel.INVALID_ZLEVEL);
		node.zcontext = OsmNode.getTagLong(json, mapTemplate.KEY_ZCONTEXT,INVALID_ID);
		
		//load feature properties
		boolean success = node.loadFeatureProperties(json,mapTemplate);
		
		//set geom type - either point or none
		if((node.fti != null)&&(node.fti.getIsPointAllowed())) {
			node.geomType = FeatureTypeInfo.GEOM_TYPE_POINT;
		}
		else {
			node.geomType = FeatureTypeInfo.GEOM_TYPE_NONE;
		}
		
		//flag loading completed
		if(success) { 
			node.loaded = true;
		}
			
	}
}
