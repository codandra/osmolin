package intransix.indoor.provision.geom;

import java.util.*;
import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.MapTemplate;
import intransix.indoor.provision.mapinfo.FeatureTypeInfo;
import intransix.indoor.geom.*;
import org.json.*;

/**
 *
 * @author sutter
 */
public class OsmMap extends OsmRelation {

	private final static String DEFAULT_ANGLE_KEY = "default_angle";
	private final static double MIN_ANGLE = -720;
	private final static double MAX_ANGLE = 720;

	private final static double FRACTIONAL_PADDING = .05;

	private final static double EARTH_CIRCUMFERENCE = 40040000.0; //somewhere between polar and equitorial value - I picked it at random though
	private final static double METERS_PER_DEGREE = EARTH_CIRCUMFERENCE / 360.0;
	
	String name;
	int version;
	HashSet<OsmLevel> levels = new HashSet<OsmLevel>();
	double width;
	double height;
	double defaultAngleRad;
	AffineTransform lonlatToXY;
	OsmFeature parent;

	private String mapTemplateName;
	private HashSet<String> activeNamespaces = new HashSet<String>();
	
	public OsmMap(long id) {
		super(id);
	}

	public HashSet<OsmLevel> getLevels() {
		return levels;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void addNamespaceName(String name) {
		this.activeNamespaces.add(name);
	}

	/** This method loads a map object from a osm map json. */
	public static void loadOsmMap(long id,
			JSONObject json, 
			MapTemplate mapTemplate,
			MapProvision mapProvision) {
		
		OsmMap map = mapProvision.getOsmMap(id);
		
		//get the name
		map.name = getTagString(json,NAME_KEY);
		if(map.name == null) map.name = String.valueOf(id);

		//get the default angle
		map.defaultAngleRad = Math.toRadians(getTagDouble(json,DEFAULT_ANGLE_KEY,0));

		//map template name
		map.mapTemplateName = mapTemplate.getName();
		
		//load the members
		map.loadMembers(json,mapTemplate,mapProvision);
		
		//flag as loaded
		map.loaded = true;
		
	}

	/** This method loads the relation members into the map. */
	@Override
	protected void loadMember(long memberId, String type, String role,
			MapTemplate mapTemplate, MapProvision mapProvision) {
		
		if(role.equalsIgnoreCase(mapTemplate.ROLE_PARENT)) {
			//try to load the parent
			if(type.equalsIgnoreCase(NODE_TYPE)) {
				this.parent = mapProvision.getOsmNode(memberId);
			}
			else if(type.equalsIgnoreCase(OsmObject.WAY_TYPE)) {
				this.parent = mapProvision.getOsmWay(memberId);
			}
			else {
				this.parent = null;
			}
		}
		else if(role.equalsIgnoreCase(mapTemplate.ROLE_LEVEL)) {
			//add the level
			OsmLevel level = mapProvision.getOsmLevel(memberId);
			if(level != null) {
				level.setMap(this);
				this.levels.add(level);
			}
		}
	}

	public JSONObject getMapJson() {
		return null;
	}

	/** This method calculates the local coordinate system for the map. */
	public void finalizeMap() throws Exception {

		//-----------------------------
		//create a node list and load feature namespaces
		//-----------------------------
		
		FeatureTypeInfo fti;
		ArrayList<OsmNode> nodes = new ArrayList<OsmNode>();
		for(OsmLevel level:levels) {
			for(OsmFeature feature:level.getFeatures()) {
				//load feature type
				fti = feature.getFeatureTypeInfo();
				if(fti != null) {
					String namespace = fti.getNamespaceName();
					this.activeNamespaces.add(namespace);
				}
				
				//add to node list
				if(feature instanceof OsmNode) {
					nodes.add((OsmNode)feature);
				}
				else if(feature instanceof OsmWay) {
					for(OsmNode node:((OsmWay)feature).getNodes()) {
						nodes.add(node);
					}
				}
			}
		}

		//---------------------
		// Create transform
		//---------------------

		//get the map location
		double minLat = MAX_ANGLE;
		double maxLat = MIN_ANGLE;
		double minLon = MAX_ANGLE;
		double maxLon = MIN_ANGLE;

		for(OsmNode node:nodes) {
			final double lat = node.getLat();
			final double lon = node.getLon();
			if(minLat > lat) minLat = lat;
			if(minLon > lon) minLon = lon;
			if(maxLat < lat) maxLat = lat;
			if(maxLon < lon) maxLon = lon;
		}

		double centerLat = (minLat + maxLat) / 2.0;
		double centerLon = (minLon + maxLon) / 2.0;

		//create the non-translated transform

		this.lonlatToXY = new AffineTransform();
		double cosLat = Math.cos(Math.toRadians(centerLat));
		//get no rotated transform
		double lonToX = cosLat * METERS_PER_DEGREE;
		double lonToY = 0.0;
		double latToX = 0.0;
		double latToY = -1.0 * METERS_PER_DEGREE;
		double lonOffset = 0;
		double latOffset = 0;
		lonlatToXY.setTransform(lonToX,lonToY,latToX,latToY,lonOffset,latOffset);
		//fix so the center point is 0. Doesn't actually matter but we'll do it anyway
		Point2D centerPoint = new Point2D.Double(centerLon,centerLat);
		lonlatToXY.translate(-centerPoint.getX(),-centerPoint.getY());

		//add rotation
		AffineTransform at = new AffineTransform();
		at.setToRotation(defaultAngleRad);
		lonlatToXY.preConcatenate(at);

		//transform with this intermediate transform to find translation

		//this is a number bigger than any coordinate we will get
		double minX = EARTH_CIRCUMFERENCE;
		double maxX = -EARTH_CIRCUMFERENCE;
		double minY = EARTH_CIRCUMFERENCE;
		double maxY = -EARTH_CIRCUMFERENCE;
		for(OsmNode node:nodes) {
			final double lat = node.getLat();
			final double lon = node.getLon();
			Point2D pt = new Point2D.Double(lon,lat);
			lonlatToXY.transform(pt, pt);
			double x = pt.getX();
			double y = pt.getY();
			if(minX > x) minX = x;
			if(minY > y) minY = y;
			if(maxX < x) maxX = x;
			if(maxY < y) maxY = y;
		}

		//add some padding and translate thie
		double dx = maxX - minX;
		double dy = maxY - minY;
		minX = minX - FRACTIONAL_PADDING*dx;
		minY = minY - FRACTIONAL_PADDING*dy;
		maxX = maxX + FRACTIONAL_PADDING*dx;
		maxY = maxY + FRACTIONAL_PADDING*dy;

		//adjust the transformationn
		at = new AffineTransform();
		at.setToTranslation(-minX, -minY);
		lonlatToXY.preConcatenate(at);
		this.width = maxX - minX;
		this.height = maxY - minY;

//check coordinate range
//		minX = EARTH_CIRCUMFERENCE;
//		maxX = -EARTH_CIRCUMFERENCE;
//		minY = EARTH_CIRCUMFERENCE;
//		maxY = -EARTH_CIRCUMFERENCE;
//		for(OsmNode node:nodes) {
//			final double lat = node.getLat();
//			final double lon = node.getLon();
//			Point2D pt = new Point2D.Double(lon,lat);
//			lonlatToXY.transform(pt, pt);
//			double x = pt.getX();
//			double y = pt.getY();
//			if(minX > x) minX = x;
//			if(minY > y) minY = y;
//			if(maxX < x) maxX = x;
//			if(maxY < y) maxY = y;
//		}
	}


//	/** This method creates the map object. */
//	private JSONObject createMapObject() throws Exception {
//		mapId = osmMapObject.getLong(ID_KEY);
//		String name = PropertyUtils.getTagString(osmMapObject,NAME_KEY);
//		JSONObject mapObject = new JSONObject();
//		mapObject.put("id",mapId);
//		mapObject.put("l","en"); //fix this!!!
//		mapObject.put("ft","map1");
//		mapObject.put("nm",name);
//
//		mapObject.put("t",getTransformJson());
//
//		mapObject.put("ar",new FormattedDecimal(angleRad,RADIANS_PRECISION));
//		mapObject.put("h",(int)height);
//		mapObject.put("w",(int)width);
//
//		//version and map feature namespace list added later
//
//		return mapObject;
//	}
//
//
//
//	/** This method creates the transform json. */
//	private JSONArray getTransformJson() throws Exception {
//		double[] matrix = new double[6];
//		AffineTransform xyToLatlon = lonlatToXY.createInverse();
//		xyToLatlon.getMatrix(matrix);
//		JSONArray json = new JSONArray();
//		for(int i = 0; i < 6; i++) {
//			json.put(new FormattedDecimal(matrix[i],TRANSFORM_PRECISION));
//		}
//		return json;
//	}


//		/** This method gets a list of the map feature namespaces used in the map. */
//	private JSONArray getActiveNamespaceList() {
//		JSONArray namespaceList = new JSONArray();
//		for(String namespaceName:activeNamespaces) {
//			namespaceList.put(namespaceName);
//		}
//		return namespaceList;
//	}

//	private class JsonIntFieldComparator implements Comparator<JSONObject> {
//		private String compareField;
//		private int defaultValue;
//
//		public JsonIntFieldComparator(String compareField, int defaultValue) {
//			this.compareField = compareField;
//			this.defaultValue = defaultValue;
//		}
//
//		@Override
//		public int compare(JSONObject a, JSONObject b) {
//			int aval = a.optInt(compareField,defaultValue);
//			int bval = b.optInt(compareField,defaultValue);
//			return Integer.compare(aval, bval);
//		}
//	}
}
