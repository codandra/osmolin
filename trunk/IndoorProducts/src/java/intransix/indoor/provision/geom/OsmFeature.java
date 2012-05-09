package intransix.indoor.provision.geom;

import intransix.indoor.provision.PropertyUtils;
import intransix.indoor.provision.mapinfo.FeatureTypeInfo;
import org.json.*;
import intransix.indoor.provision.mapinfo.MapTemplate;
import intransix.indoor.provision.mapinfo.PropertyKey;

/**
 *
 * @author sutter
 */
public class OsmFeature extends OsmObject {
	JSONObject properties;
	int geomType;
	int zlevel;
	long zcontext;
	FeatureTypeInfo fti;
	
	public OsmFeature(long id) {
		super(id);
	}

	public int getZlevel() {
		return zlevel;
	}
	
	public long getZcontext() {
		 return zcontext;
	}

	public void setZInfo(long zcontext, int zlevel) {
		this.zcontext = zcontext;
		this.zlevel = zlevel;
	}

	public FeatureTypeInfo getFeatureTypeInfo() {
		return fti;
	}

	/** This method returns true if the feature should be placed in the map. */
	public boolean placeInMap() {
		return ((getIsLoaded())&&(fti != null));
	}

	public boolean loadFeatureProperties(JSONObject json, MapTemplate mapTemplate) {
		
		fti = mapTemplate.getFeatureTypeInfo(json);
		properties = new JSONObject();
		
		try {
			properties.put("type",fti.getTypeName());
			properties.put("zorder",fti.getZorder());
		
			for(PropertyKey pk:fti.getProperties()) {
				String val = getTagString(json,pk.osmKey);
				if(val != null) {
					if(!pk.replace) {
						if(properties.has(pk.appKey)) continue;
					}
					properties.put(pk.appKey,val);
				}
			}
		}
		catch(Exception ex) {
			return false;
		}

		return true;
	}


//
//
//
//	/** This method creates a geometry object. */
//	private void addFeatureGeoJson(JSONObject osmMember, ArrayList<JSONObject> featureList) throws Exception {
//
//		long ref = osmMember.getLong(REF_KEY);
//		String type = osmMember.getString(TYPE_KEY);
//		String role = osmMember.getString(ROLE_KEY);
//
//		boolean asShell = role.equalsIgnoreCase(MapTemplate.ROLE_SHELL);
//
//		JSONObject geom = null;
//		JSONObject feature = null;
//		int geomType = FeatureTypeInfo.GEOM_TYPE_NONE;
//		FeatureTypeInfo fti = null;
//		JSONObject osmGeom = null;
//		if((role.equalsIgnoreCase(MapTemplate.ROLE_FEATURE))||(asShell)) {
//
//			//get the osm json
//			if(NODE_TYPE.equalsIgnoreCase(type)) {
//				osmGeom = nodeMap.get(ref);
//				geomType = FeatureTypeInfo.GEOM_TYPE_POINT;
//			}
//			else if(WAY_TYPE.equalsIgnoreCase(type)) {
//				osmGeom = wayMap.get(ref);
//				//we don't know if it is a line or an area yet
//				geomType = FeatureTypeInfo.GEOM_TYPE_NONE;
//			}
//			else {
//				//unknown type
//				return;
//			}
//
//			//get the feature type info
//			if(asShell) {
//				fti = mapTemplate.getShellFeatureType();
//			}
//			else {
//				fti = mapTemplate.getFeatureTypeInfo(osmGeom);
//			}
//			if(fti == null) return;
//
//			//create feature
//			feature = new JSONObject();
//			feature.put("type","Feature");
//			long id = osmGeom.getLong(ID_KEY);
//			feature.put("id",id);
//
//			//construct geom object
//			if(geomType == FeatureTypeInfo.GEOM_TYPE_POINT) {
//				geom = getPointGeom(osmGeom);
//			}
//			else {
//				//get the geomt type
//				geomType = fti.getPathType(osmMember);
//				if(geomType == FeatureTypeInfo.GEOM_TYPE_LINE) {
//					geom = getLineGeom(osmGeom);
//				}
//				else if(geomType == FeatureTypeInfo.GEOM_TYPE_AREA) {
//					geom = getAreaGeom(osmGeom);
//				}
//				else {
//					//unknown option
//					return;
//				}
//			}
//
//			feature.put("geometry",geom);
//
//			//get the properties
//			JSONObject properties = getProperties(osmGeom,fti);
//			feature.put("properties",properties);
//
//			//make sure this namespace is in active namespaces
//			this.activeNamespaces.add(fti.getNamespaceName());
//		}
//
//		//add to feature list
//		if(feature != null) {
//			//check if this is part of a multipolygon relation
//			MemberPolygon mp = multipolyMembers.get(ref);
//
//			if((mp != null)&&(geomType == FeatureTypeInfo.GEOM_TYPE_AREA)) {
//				//store the feature with the multipoly members
//				//make sure it is an area
//				mp.setFeature(feature);
//			}
//			else {
//				//add the feature
//				featureList.add(feature);
//			}
//		}
//	}
//
//	/** This method gets the multipolygon features. */
//	private void addMultipolyFeatureGeoJson(OsmMultipoly mpr, ArrayList<JSONObject> featureList)
//			throws Exception{
//		JSONObject feature = mpr.createCombinedFeature();
//		featureList.add(feature);
//	}
//
//
//
//	/** This method creates a node type geometry. */
//	private JSONObject getPointGeom(JSONObject node)  throws Exception {
//
//		JSONObject geom = new JSONObject();
//		geom.put("type","Point");
//
//		//get point
//		double lat = node.getDouble(LAT_KEY);
//		double lon = node.getDouble(LON_KEY);
//		Point2D point = new Point2D.Double(lon,lat);
//		lonlatToXY.transform(point, point);
//
//		JSONArray coords = getJsonPoint(point,COORD_PRECISION);
//		geom.put("coordinates",coords);
//
//		return geom;
//	}
//
//		/** This metho creates a way type geometry. */
//	private JSONObject getLineGeom(JSONObject way) throws Exception {
//
//		JSONObject geom = new JSONObject();
//
//		//get corod type
//		geom.put("type","LineString");
//
//		//get point list
//		JSONArray wayNodes = way.getJSONArray(WAY_NODES_KEY);
//		JSONArray pointArray = this.getJsonPointArray(wayNodes);
//		if(pointArray == null) return null;
//
//		geom.put("coordinates",pointArray);
//
//		return geom;
//	}
//
//	/** This metho creates a way type geometry. */
//	private JSONObject getAreaGeom(JSONObject way) throws Exception {
//
//		JSONObject geom = new JSONObject();
//
//		//get corod type
//		geom.put("type","Polygon");
//
//		//get point list
//		JSONArray wayNodes = way.getJSONArray(WAY_NODES_KEY);
//		JSONArray pointArray = this.getJsonPointArray(wayNodes);
//		if(pointArray == null) return null;
//		JSONArray polygonJSON = (new JSONArray()).put(pointArray);
//
//		geom.put("coordinates",polygonJSON);
//
//		return geom;
//	}
//
//	/** This method creates a json point in the format [x,y]. The number of decimal points
//	 * should be specified with precision. */
//	private JSONArray getJsonPoint(Point2D point, int precision) {
//		JSONArray pointJson = new JSONArray();
//		pointJson.put(new FormattedDecimal(point.getX(),COORD_PRECISION));
//		pointJson.put(new FormattedDecimal(point.getY(),COORD_PRECISION));
//		return pointJson;
//	}
//
//	private JSONArray getJsonPointArray(JSONArray wayNodes) throws Exception {
//		int cnt = wayNodes.length();
//		if(cnt <= 1) return null;
//
//		Point2D point;
//		long nodeId;
//		JSONObject node = null;
//		double lon;
//		double lat;
//		ArrayList<Point2D> pointList = new ArrayList<Point2D>();
//		for(int i = 0; i < cnt; i++) {
//			nodeId = wayNodes.getLong(i);
//			node = nodeMap.get(nodeId);
//			if(node == null) throw new Exception("Node missing: " + nodeId);
//
//			lat = node.getDouble(LAT_KEY);
//			lon = node.getDouble(LON_KEY);
//			point = new Point2D.Double(lon,lat);
//			lonlatToXY.transform(point, point);
//			pointList.add(point);
//		}
//
//		return getJsonPointArray(pointList,COORD_PRECISION);
//	}
//
//	/** This method creates a json point in the format [x,y]. The number of decimal points
//	 * should be specified with precision. */
//	private JSONArray getJsonPointArray(ArrayList<Point2D> pointList, int precision) {
//		JSONArray pointArrayJson = new JSONArray();
//		for(Point2D p:pointList) {
//			pointArrayJson.put(getJsonPoint(p,precision));
//		}
//		return pointArrayJson;
//	}
			
	
}
