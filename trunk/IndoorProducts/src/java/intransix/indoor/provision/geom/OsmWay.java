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
	
	private ArrayList<OsmNode> nodes = new ArrayList<OsmNode>();
	
	private ArrayList<OsmWay> holes = new ArrayList<OsmWay>();
	private OsmMultipoly multipoly = null;
	
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
	
	/** This sets the multipoly, if this way is part of a multipoly. */
	public void setMultipoly(OsmMultipoly multipoly) {
		this.multipoly = multipoly;
	}
	
	/** This method returns true is the given member polygon is within the outer ring
	 * of this member polygon. */
	public boolean contains(OsmWay insideWay) {
		if(insideWay.nodes.isEmpty()) return false;
		
		//make sure they are on the same level
		if((insideWay.zlevel != this.zlevel)||(insideWay.zcontext != this.zcontext)) {
			return false;
		}
		
		//Just check a single point - HANDLE BORDER CASE!!!
		OsmNode insideNode = insideWay.nodes.get(0);
		return contains(insideNode);
	}
	
	/** This method adds a way as a whole for this way. */
	public void addHole(OsmWay insideWay) {
		this.holes.add(insideWay);
	}
	
	//========================
	// Protected Methods
	//========================
	@Override
	protected JSONObject getGeometryJson(MapTemplate mapTemplate, AffineTransform lonlatToXY) throws Exception {
		JSONObject geomJson = new JSONObject();
		
		//if this is part of a multipolygon
		//get the json object from it. It may be non-null or a combination
		//of multiple ways
		if(multipoly != null) {
			return multipoly.getGeomeryForWayObject(this,mapTemplate,lonlatToXY);
		}
		
		//the object must have a type
		if(fti == null) return null;
		
		//load geom type
		int geomType = fti.getDefaultPathType();
		String typeString;
		JSONArray coordJson;
		if(geomType == FeatureTypeInfo.GEOM_TYPE_AREA) {
			//get type
			typeString = "Polygon";
			//get coordinates
			coordJson = getJsonPointArrayList(lonlatToXY, mapTemplate.COORDINATE_PRECISION);
		}
		else if(geomType == FeatureTypeInfo.ALLOWED_TYPE_LINE) {
			//get type
			typeString = "LineString";
			//get coordinates
			coordJson = getJsonPointArray(nodes,lonlatToXY, mapTemplate.COORDINATE_PRECISION);
		}
		else {
			//no geometry
			return null;
		}
		
		geomJson.put("type",typeString);
		geomJson.put("coordinates",coordJson);

		return geomJson;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method returns the properties. */
	JSONObject getProperties() {
		return properties;
	}
	
	/** This returns a list of point lists, corresponding to the main nodes
	 * along with any inside rings. This is used for a polygon. */
	JSONArray getJsonPointArrayList(AffineTransform lonlatToXY, int precision) throws Exception {		
		JSONArray pointJsonArrayList = new JSONArray();
		JSONArray pointJsonArray = getJsonPointArray(nodes,lonlatToXY,precision);
		pointJsonArrayList.put(pointJsonArray);
		for(OsmWay hole:holes) {
			 pointJsonArray = getJsonPointArray(hole.nodes,lonlatToXY,precision);
			 pointJsonArrayList.put(pointJsonArray);
		}
		return pointJsonArrayList;
	}
	
	/** This returns an json array of points corresponding to the passed node list. */
	JSONArray getJsonPointArray(ArrayList<OsmNode> nodeList, 
			AffineTransform lonlatToXY, int precision) throws Exception {
		
		JSONArray pointJsonArray = new JSONArray();
		for(OsmNode node:nodeList) {
			JSONArray point = node.getJsonPoint(lonlatToXY,precision);
			if(point != null) {
				pointJsonArray.put(point);
			}
		}
		return pointJsonArray;
	}
		
	//========================
	// Private Methods
	//========================
	
	private boolean contains(OsmNode inNode) {

		boolean isInside = false;
		OsmNode prevOutNode = null;
		for(OsmNode outNode:nodes) {
			if(prevOutNode == null) {
				prevOutNode = outNode;
			}
			else {
				//get the value of y where the lines intersect
				if(halfLineHits(inNode,outNode,prevOutNode)) {
					isInside = !isInside;
				}
			}
			prevOutNode = outNode;
		}

		//odd nubmer of hits means inside
		return isInside;
	}
		
	/** Calculate a vertical line from the test point intersecting the segment
	* defined by p1 and p2. */
	private boolean halfLineHits(OsmNode inNode, OsmNode outNode1, OsmNode outNode2) {
		double x0 = inNode.getLon();
		double y0 = inNode.getLat();
		double ax0 = outNode1.getLon();
		double ay0 = outNode1.getLat();
		double ax1 = outNode2.getLon();
		double ay1 = outNode2.getLat();

		if(ax0 == ax1) {
			return false;
		}
		else {
			double tint = (x0 - ax0)/ (ax1 - ax0);
			if((tint >= 0)&&(tint < 1)) {
				double yint = (ay1 - ay0) * tint + ay0;
				if(yint > y0) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}
	}

			
	
}
