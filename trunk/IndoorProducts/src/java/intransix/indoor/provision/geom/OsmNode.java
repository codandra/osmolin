package intransix.indoor.provision.geom;

import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.MapTemplate;
import intransix.indoor.geom.*;
import intransix.indoor.util.FormattedDecimal;
import org.json.JSONArray;
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
		
		node.lon = json.optDouble(LON_KEY,INVALID_ANGLE);
		if(node.lon == INVALID_ANGLE) return;
		
		//try to read the zlevel
		node.zlevel = OsmNode.getTagInt(json, mapTemplate.KEY_ZLEVEL,OsmLevel.INVALID_ZLEVEL);
		node.zcontext = OsmNode.getTagLong(json, mapTemplate.KEY_ZCONTEXT,INVALID_ID);
		
		//load feature properties
		boolean success = node.loadFeatureProperties(json,mapTemplate);
		
		//flag loading completed
		if(success) { 
			node.loaded = true;
		}
			
	}
	
	//========================
	// protected methods
	//========================
	
	@Override
	protected JSONObject getGeometryJson(MapTemplate mapTemplate, AffineTransform lonlatToXY) throws Exception {
		JSONObject geomJson = new JSONObject();
		geomJson.put("type","Point");
		JSONArray coords = getJsonPoint(lonlatToXY,mapTemplate.COORDINATE_PRECISION);
		geomJson.put("coordinates",coords);
		
		return geomJson;
	}
	
	//========================
	// package methods
	//========================
	
	/** This method creates a json point in the format [x,y]. The number of decimal points
	 * should be specified with precision. */
	JSONArray getJsonPoint(AffineTransform lonlatToXY, int precision) throws Exception {
		Point2D point = new Point2D.Double(lon,lat);
		lonlatToXY.transform(point, point);
		JSONArray pointJson = new JSONArray();
		pointJson.put(new FormattedDecimal(point.getX(),precision));
		pointJson.put(new FormattedDecimal(point.getY(),precision));
		return pointJson;
	}

}
