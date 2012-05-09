package intransix.indoor.provision;


import intransix.indoor.provision.mapinfo.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
//import java.awt.geom.*;

import java.util.Collections;

import intransix.indoor.provision.geom.*;
import intransix.indoor.util.FormattedDecimal;
//import intransix.indoor.util.LabelRect;
import intransix.indoor.geom.*;
import intransix.indoor.provision.geom.*;

/**
 *
 * @author sutter
 */
public class MapProvision {
	
	private final static double MIN_ANGLE = -720;
	private final static double MAX_ANGLE = 720;
	
	private final static double FRACTIONAL_PADDING = .05;
	
	private final static double EARTH_CIRCUMFERENCE = 40040000.0; //somewhere between polar and equitorial value - I picked it at random though
	private final static double METERS_PER_DEGREE = EARTH_CIRCUMFERENCE / 360.0;
	
	private final static int RADIANS_PRECISION = 2;
	private final static int COORD_PRECISION = 3;
	private final static int TRANSFORM_PRECISION = 7;
	
	private final static int DEFAULT_ZLEVEL_FOR_SORT = 999;
			
	private final static String MAP_TYPE_INFO_NAME = "maptemplate";
	private final static String MAP_UPLOAD_NAME = "indoormap";
	private final static String LVLGEOM_UPLOAD_NAME = "lvlgeom";	
	
	private final static String ELEMENTS_KEY = "elements";
	private final static String ID_KEY = "id";
	private final static String TYPE_KEY = "type";
	
	private final static String RELATION_TYPE = "relation";
	private final static String MEMBERS_KEY = "members";
	private final static String REF_KEY = "ref";
	private final static String ROLE_KEY = "role";
	
	private final static String WAY_TYPE = "way";
	private final static String NODE_TYPE = "node";
	
	private final static String WAY_NODES_KEY = "nodes";
	private final static String LAT_KEY = "lat";
	private final static String LON_KEY = "lon";
	
	private final static String NAME_KEY = "name";
	private final static String DEFAULT_ANGLE_KEY = "default_angle";
	
	private DataLoader dataLoader;
	
	private JSONObject indoorMapObject = null;
	private ArrayList<JSONObject> levelGeomObjects = new ArrayList<JSONObject>();
	
	private MapTemplate mapTemplate;
	private HashSet<String> activeNamespaces = new HashSet<String>();
	
	private long mapId;
	private AffineTransform lonlatToXY;
	private double angleRad;
	private double height;
	private double width;
	
/////////////////////////////////////////////////////////////////
	private HashMap<Long,OsmNode> nodeMap = new HashMap<Long,OsmNode>();
	private HashMap<Long,OsmWay> wayMap = new HashMap<Long,OsmWay>();
	private HashMap<Long,OsmLevel> levelMap = new HashMap<Long,OsmLevel>();
	private HashMap<Long,OsmMap> mapMap = new HashMap<Long,OsmMap>();
	private HashMap<Long,OsmMultipoly> polyMap = new HashMap<Long,OsmMultipoly>();
	
	
	public OsmNode getOsmNode(Long id) {
		OsmNode osmNode = nodeMap.get(id);
		if(osmNode == null) {
			osmNode = new OsmNode(id);
		}
		return osmNode;
	}
	
	public OsmWay getOsmWay(Long id) {
		OsmWay osmWay = wayMap.get(id);
		if(osmWay == null) {
			osmWay = new OsmWay(id);
		}
		return osmWay;
	}
	
	public OsmLevel getOsmLevel(Long id) {
		OsmLevel osmLevel = levelMap.get(id);
		if(osmLevel == null) {
			osmLevel = new OsmLevel(id);
		}
		return osmLevel;
	}
	
	public OsmMap getOsmMap(Long id) {
		OsmMap osmMap = mapMap.get(id);
		if(osmMap == null) {
			osmMap = new OsmMap(id);
		}
		return osmMap;
	}
	
	public OsmMultipoly getOsmMultipoly(Long id) {
		OsmMultipoly osmMultipoly = polyMap.get(id);
		if(osmMultipoly == null) {
			osmMultipoly = new OsmMultipoly(id);
		}
		return osmMultipoly;
	}
		
	
	
	
	
///////////////////////////////////////////////////////////////////
	
	
	public MapProvision(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	public void createAndUpload(String mapTemplateName, String structureId, String name) throws Exception {
		
		//get the map template
		loadTemplate(mapTemplateName);
		
		//store input
		loadVenueInfo(structureId,name);
		
		//create the transform
		createTransform();
		
		//make the data
		indoorMapObject = createMapObject();
		
		//add the levels
		JSONArray indoorLevelList = new JSONArray();
		indoorMapObject.put("lvl",indoorLevelList);
		for(JSONObject osmLevel:osmLevelList) {
			createLevel(osmLevel,indoorLevelList);
		}
		
if(true) throw new Exception("Test finished");		
		
		FileAccess fileAccess = dataLoader.getFileAccess();
		
		//uploade
		//get version
		int version = fileAccess.getVersion(MAP_UPLOAD_NAME,String.valueOf(mapId));

		//add version to data
		indoorMapObject.put("v",version);
		indoorMapObject.put("mt",mapTemplate.getName());
		indoorMapObject.put("ns",getActiveNamespaceList());
		fileAccess.upload(MAP_UPLOAD_NAME,String.valueOf(mapId),version,indoorMapObject);
		
		for(JSONObject lg:this.levelGeomObjects) {
			lg.put("v",version);
			long lid = lg.getInt(ID_KEY);
			fileAccess.upload(LVLGEOM_UPLOAD_NAME,String.valueOf(lid),version,lg);
		}
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
	}

	/** This method creates the map object. */
	private JSONObject createMapObject() throws Exception {
		mapId = osmMapObject.getLong(ID_KEY);
		String name = PropertyUtils.getTagString(osmMapObject,NAME_KEY);
		JSONObject mapObject = new JSONObject();
		mapObject.put("id",mapId);
		mapObject.put("l","en"); //fix this!!!
		mapObject.put("ft","map1");
		mapObject.put("nm",name);
		
		mapObject.put("t",getTransformJson());
		
		mapObject.put("ar",new FormattedDecimal(angleRad,RADIANS_PRECISION));
		mapObject.put("h",(int)height);
		mapObject.put("w",(int)width);
		
		//version and map feature namespace list added later
		
		return mapObject;
	}
	
	/** This method creates a level object. */
	private void createLevel(JSONObject osmLevel, JSONArray indoorLevelList) throws Exception {
		long id = osmLevel.getLong("id");
		int zlevel = PropertyUtils.getTagInt(osmLevel,MapTemplate.KEY_ZLEVEL,0);
		String name = PropertyUtils.getTagString(osmLevel,NAME_KEY);
		
		//create the levle
		JSONObject indoorLevel = new JSONObject();
		indoorLevel.put("id",id);
		indoorLevel.put("z",zlevel);
		if(name == null) name = "unnamed";
		indoorLevel.put("nm",name);
		indoorLevelList.put(indoorLevel);
		
		//create the level geometry
		JSONObject levelGeoJson = new JSONObject();
		levelGeoJson.put("id",id);
		levelGeoJson.put("mid",mapId);
		levelGeoJson.put("l","en"); //fix this!!!
		levelGeoJson.put("ft","lvlg1");
		//version added later
		
		//add the geometry
		ArrayList<JSONObject> featureList = new ArrayList<JSONObject>();
		
		JSONArray members = osmLevel.getJSONArray(MEMBERS_KEY);
		
		//get nodes and ways
		int cnt = members.length();
		for(int i = 0; i < cnt; i++) {
			addFeatureGeoJson(members.getJSONObject(i),featureList);
		}
		
		//process the multipolygons
		for(OsmMultipoly mpr:multipolygons) {
			addMultipolyFeatureGeoJson(mpr,featureList);
		}
		
		//sort the geometry by zindex
		Collections.sort(featureList,new JsonIntFieldComparator("zorder",DEFAULT_ZLEVEL_FOR_SORT));
		
		levelGeoJson.put("type","FeatureCollection");
		JSONArray featureJsonArray = new JSONArray();
		levelGeoJson.put("features",featureJsonArray);
		for(JSONObject f:featureList) {
			featureJsonArray.put(f);
		}
		
		levelGeomObjects.add(levelGeoJson);
		
	}
	 
	/** This method creates a geometry object. */
	private void addFeatureGeoJson(JSONObject osmMember, ArrayList<JSONObject> featureList) throws Exception {
		
		long ref = osmMember.getLong(REF_KEY);
		String type = osmMember.getString(TYPE_KEY);
		String role = osmMember.getString(ROLE_KEY);
		
		boolean asShell = role.equalsIgnoreCase(MapTemplate.ROLE_SHELL);
		
		JSONObject geom = null;
		JSONObject feature = null;
		int geomType = FeatureTypeInfo.GEOM_TYPE_NONE;
		FeatureTypeInfo fti = null;
		JSONObject osmGeom = null;
		if((role.equalsIgnoreCase(MapTemplate.ROLE_FEATURE))||(asShell)) {
			
			//get the osm json
			if(NODE_TYPE.equalsIgnoreCase(type)) {
				osmGeom = nodeMap.get(ref);
				geomType = FeatureTypeInfo.GEOM_TYPE_POINT;
			}
			else if(WAY_TYPE.equalsIgnoreCase(type)) {
				osmGeom = wayMap.get(ref);
				//we don't know if it is a line or an area yet
				geomType = FeatureTypeInfo.GEOM_TYPE_NONE;
			}
			else {
				//unknown type
				return;
			}
			
			//get the feature type info
			if(asShell) {
				fti = mapTemplate.getShellFeatureType();
			}
			else {
				fti = mapTemplate.getFeatureTypeInfo(osmGeom);
			}
			if(fti == null) return;
			
			//create feature
			feature = new JSONObject();
			feature.put("type","Feature");
			long id = osmGeom.getLong(ID_KEY);
			feature.put("id",id);
			
			//construct geom object
			if(geomType == FeatureTypeInfo.GEOM_TYPE_POINT) {
				geom = getPointGeom(osmGeom);
			}
			else {
				//get the geomt type
				geomType = fti.getPathType(osmMember);
				if(geomType == FeatureTypeInfo.GEOM_TYPE_LINE) {
					geom = getLineGeom(osmGeom);
				}
				else if(geomType == FeatureTypeInfo.GEOM_TYPE_AREA) {
					geom = getAreaGeom(osmGeom);
				}
				else {
					//unknown option
					return;
				}
			}
			
			feature.put("geometry",geom);
			
			//get the properties
			JSONObject properties = getProperties(osmGeom,fti);
			feature.put("properties",properties);
			
			//make sure this namespace is in active namespaces
			this.activeNamespaces.add(fti.getNamespaceName());	
		}

		//add to feature list
		if(feature != null) {
			//check if this is part of a multipolygon relation
			MemberPolygon mp = multipolyMembers.get(ref);
			
			if((mp != null)&&(geomType == FeatureTypeInfo.GEOM_TYPE_AREA)) {
				//store the feature with the multipoly members
				//make sure it is an area
				mp.setFeature(feature);
			}
			else {
				//add the feature
				featureList.add(feature);
			}
		}
	}
	
	/** This method gets the multipolygon features. */
	private void addMultipolyFeatureGeoJson(OsmMultipoly mpr, ArrayList<JSONObject> featureList)
			throws Exception{
		JSONObject feature = mpr.createCombinedFeature();
		featureList.add(feature);
	}	
	

	
	/** This method creates a node type geometry. */
	private JSONObject getPointGeom(JSONObject node)  throws Exception {
		
		JSONObject geom = new JSONObject();
		geom.put("type","Point");
		
		//get point
		double lat = node.getDouble(LAT_KEY);
		double lon = node.getDouble(LON_KEY);
		Point2D point = new Point2D.Double(lon,lat);
		lonlatToXY.transform(point, point);
		
		JSONArray coords = getJsonPoint(point,COORD_PRECISION);
		geom.put("coordinates",coords);
		
		return geom;		
	}
	
		/** This metho creates a way type geometry. */
	private JSONObject getLineGeom(JSONObject way) throws Exception {

		JSONObject geom = new JSONObject();
		
		//get corod type
		geom.put("type","LineString");
		
		//get point list
		JSONArray wayNodes = way.getJSONArray(WAY_NODES_KEY);
		JSONArray pointArray = this.getJsonPointArray(wayNodes);
		if(pointArray == null) return null;
		
		geom.put("coordinates",pointArray);
		
		return geom;
	}
	
	/** This metho creates a way type geometry. */
	private JSONObject getAreaGeom(JSONObject way) throws Exception {

		JSONObject geom = new JSONObject();
		
		//get corod type
		geom.put("type","Polygon");
		
		//get point list
		JSONArray wayNodes = way.getJSONArray(WAY_NODES_KEY);
		JSONArray pointArray = this.getJsonPointArray(wayNodes);
		if(pointArray == null) return null;
		JSONArray polygonJSON = (new JSONArray()).put(pointArray);
		
		geom.put("coordinates",polygonJSON);
		
		return geom;
	}
	
	/** This method creates a json point in the format [x,y]. The number of decimal points
	 * should be specified with precision. */
	private JSONArray getJsonPoint(Point2D point, int precision) {
		JSONArray pointJson = new JSONArray();
		pointJson.put(new FormattedDecimal(point.getX(),COORD_PRECISION));
		pointJson.put(new FormattedDecimal(point.getY(),COORD_PRECISION));
		return pointJson;
	}
	
	private JSONArray getJsonPointArray(JSONArray wayNodes) throws Exception {
		int cnt = wayNodes.length();
		if(cnt <= 1) return null;
		
		Point2D point;
		long nodeId;
		JSONObject node = null;
		double lon;
		double lat;
		ArrayList<Point2D> pointList = new ArrayList<Point2D>();
		for(int i = 0; i < cnt; i++) {
			nodeId = wayNodes.getLong(i);
			node = nodeMap.get(nodeId);
			if(node == null) throw new Exception("Node missing: " + nodeId);
			
			lat = node.getDouble(LAT_KEY);
			lon = node.getDouble(LON_KEY);
			point = new Point2D.Double(lon,lat);
			lonlatToXY.transform(point, point);
			pointList.add(point);
		}
		
		return getJsonPointArray(pointList,COORD_PRECISION);
	}
	
	/** This method creates a json point in the format [x,y]. The number of decimal points
	 * should be specified with precision. */
	private JSONArray getJsonPointArray(ArrayList<Point2D> pointList, int precision) {
		JSONArray pointArrayJson = new JSONArray();
		for(Point2D p:pointList) {
			pointArrayJson.put(getJsonPoint(p,precision));
		}
		return pointArrayJson;
	}
	
	/** This method creates the transform json. */
	private JSONArray getTransformJson() throws Exception {
		double[] matrix = new double[6];
		AffineTransform xyToLatlon = lonlatToXY.createInverse();
		xyToLatlon.getMatrix(matrix);
		JSONArray json = new JSONArray();
		for(int i = 0; i < 6; i++) {
			json.put(new FormattedDecimal(matrix[i],TRANSFORM_PRECISION));
		}
		return json;
	}
	/** This method calculates the local coordinate system for the map. */
	private void createTransform() throws Exception {
		Collection<JSONObject> nodes = nodeMap.values();
		
		//get the map location
		double minLat = MAX_ANGLE;
		double maxLat = MIN_ANGLE;
		double minLon = MAX_ANGLE;
		double maxLon = MIN_ANGLE;
		
		for(JSONObject node:nodes) {
			final double lat = node.getDouble(LAT_KEY);
			final double lon = node.getDouble(LON_KEY);
			if(minLat > lat) minLat = lat;
			if(minLon > lon) minLon = lon;
			if(maxLat < lat) maxLat = lat;
			if(maxLon < lon) maxLon = lon;
		}
		
		double centerLat = (minLat + maxLat) / 2.0;
		double centerLon = (minLon + maxLon) / 2.0;
		
		//create the non-translated transform
		angleRad = Math.toRadians(PropertyUtils.getTagDouble(osmMapObject,DEFAULT_ANGLE_KEY,0));
		
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
		at.setToRotation(angleRad);
		lonlatToXY.preConcatenate(at);
		
		//transform with this intermediate transform to find translation
		
		//this is a number bigger than any coordinate we will get
		double minX = EARTH_CIRCUMFERENCE;
		double maxX = -EARTH_CIRCUMFERENCE;
		double minY = EARTH_CIRCUMFERENCE;
		double maxY = -EARTH_CIRCUMFERENCE;
		for(JSONObject node:nodes) {
			final double lat = node.getDouble(LAT_KEY);
			final double lon = node.getDouble(LON_KEY);
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
//		for(JSONObject node:nodes) {
//			final double lat = node.getDouble(LAT_KEY);
//			final double lon = node.getDouble(LON_KEY);
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
	/** This method gets a list of the map feature namespaces used in the map. */
	private JSONArray getActiveNamespaceList() {
		JSONArray namespaceList = new JSONArray();
		for(String namespaceName:activeNamespaces) {
			namespaceList.put(namespaceName);
		}
		return namespaceList;
	}
	
	private class JsonIntFieldComparator implements Comparator<JSONObject> {
		private String compareField;
		private int defaultValue;
		
		public JsonIntFieldComparator(String compareField, int defaultValue) {
			this.compareField = compareField;
			this.defaultValue = defaultValue;
		}
		
		@Override
		public int compare(JSONObject a, JSONObject b) {
			int aval = a.optInt(compareField,defaultValue);
			int bval = b.optInt(compareField,defaultValue);
			return Integer.compare(aval, bval);
		}
	}
	
	private class JsonIntTagComparator implements Comparator<JSONObject> {
		private String compareField;
		private int defaultValue;
		
		public JsonIntTagComparator(String compareField, int defaultValue) {
			this.compareField = compareField;
			this.defaultValue = defaultValue;
		}
		
		@Override
		public int compare(JSONObject a, JSONObject b) {
			int aval, bval;
			try {
				aval = PropertyUtils.getTagInt(a,compareField,defaultValue);
				bval = PropertyUtils.getTagInt(b,compareField,defaultValue);
				return Integer.compare(aval, bval);
			}
			catch(Exception ex) {
				//just give up
				return 0;
			}
		}
	}
	
}
