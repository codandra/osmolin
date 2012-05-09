/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.indoor.provision.geom;

import java.util.*;
import org.json.*;
import intransix.indoor.util.FormattedDecimal;

/**
 *
 * @author sutter
 */
public class MemberPolygon {
	
	//====================
	// Private Members
	//====================
	private long wayId;
	private boolean isInner;
	private JSONObject feature;
	
	//=====================
	// Public Methods
	//=====================
	
	/** This creates a member polygon loading the data available from the json. */
	public static MemberPolygon initPolygon(JSONObject json) throws Exception {
		String type = json.getString("type");
		if(!type.equalsIgnoreCase("way")) return null;
		
		MemberPolygon mp = new MemberPolygon();
		mp.wayId = json.getLong("ref");
		String role = json.getString("role");
		mp.isInner = role.equalsIgnoreCase("inner");
		return mp;
	}
	
	/** This gets the osm way id associated with this member polygon. */
	public long getWayId() {
		return wayId;
	}
	
	/** This returns true if the polygon is an inner polygon ring. */
	public boolean getIsInner() {
		return isInner;
	}
	
	/** This method sets the geojson feature associated with the MemberPolygon. */
	public void setFeature(JSONObject json) {
		this.feature = json;
	}
	
	/** This method returns the geojson feature associated with the MemberPolygon. */
	public JSONObject getFeature() {
		return feature;
	}
	
	public void setId(long id) throws Exception {
		feature.put("id",id);
	}
	
	/** This method returns true is the given member polygon is within the outer ring
	 * of this member polygon. */
	public boolean contains(MemberPolygon inside) throws Exception {
		JSONObject insideGeom = inside.getPolygonGeom();
		if(insideGeom == null) return false;
		JSONArray insideCoords = insideGeom.getJSONArray("coordinates");
		JSONArray insideRing = insideCoords.getJSONArray(0);
		
		//Just check a single point - HANDLE BORDER CASE!!!
		JSONArray insidePoint = insideRing.getJSONArray(0);
		return isInside(insidePoint);
	}
	
	/** This method merges the given member polygon as an inside polygon ring. 
	 * It also handles merging the properties by adding any missing property 
	 * from the local properties. */
	public void insert(MemberPolygon inside) throws Exception {
		//get the coordinate object
		JSONObject geom = getPolygonGeom();
		JSONArray coords = geom.getJSONArray("coordinates"); 
		//add the new linear ring
		JSONObject insideGeom = inside.getPolygonGeom();
		JSONArray insideCoords = insideGeom.getJSONArray("coordinates");
		coords.put(insideCoords);
		//import the properties
		importProperties(inside);
		
	}
	
	/** This method retrieves the geojson geometry object. */
	public JSONObject getPolygonGeom() throws Exception {
		if(feature == null) return null;
		return feature.getJSONObject("geometry");
	}
	
	/** Thie method taks a list of geojson polygons and combines them into a single
	 * multipolygon in this object. */
	public void setPolygons(ArrayList<JSONObject> polygons) throws Exception {
		JSONArray multipolygon = new JSONArray();
		for(JSONObject geom:polygons) {
			JSONArray coord = geom.getJSONArray("coordinates");
			multipolygon.put(coord);
		}
		
		//update the geometry
		JSONObject geom = getPolygonGeom();
		geom.put("coordinates",multipolygon);
		geom.put("type","MultiPolygon");
	}
	
	/** This method takes the properties from the given MemperPolygon and adds any missing ones 
	 * to the local object. */
	public void importProperties(MemberPolygon mp) throws Exception {
		JSONObject props = feature.getJSONObject("properties");
		JSONObject importProps = mp.feature.getJSONObject("properties");
		if(importProps == null) {
			//no properties to import
			return;
		}
		else if(props == null) {
			//no props, add them from import object
			feature.put("properties",importProps);
		}
		else {
			//combine props;
			Iterator<String> keyIter = importProps.keys();
			while(keyIter.hasNext()) {
				String key = keyIter.next();
				//add property if it is no already there
				if(!props.has(key)) {
					props.put(key,importProps.get(key));
				}
			}
		}
		
	}
	
	//==============================
	// Private methods
	//==============================
	
	private boolean isInside(JSONArray insidePoint) throws Exception {
		
		JSONObject geom = this.getPolygonGeom();
		JSONArray coords = geom.getJSONArray("coordinates");
		JSONArray mainRing = coords.getJSONArray(0);

		boolean isInside = false;
		JSONArray prev = null;
		int cnt = mainRing.length();
		for(int i = 0; i < cnt; i++) {
			JSONArray p = mainRing.getJSONArray(i);
			if(prev == null) {
				prev = p;
			}
			else {
				//get the value of y where the lines intersect
				if(halfLineHits(insidePoint,p,prev)) {
					isInside = !isInside;
				}
			}
			prev = p;
		}

		//odd nubmer of hits means inside
		return isInside;
	}
		
	/** Calculate a vertical line from the test point intersecting the segment
	* defined by p1 and p2. */
	private boolean halfLineHits(JSONArray refPoint, JSONArray lineStart, JSONArray lineEnd) throws Exception {
		double x0 = getDoubleValue(refPoint,0);
		double y0 = getDoubleValue(refPoint,1);
		double ax0 = getDoubleValue(lineStart,0);
		double ay0 = getDoubleValue(lineStart,1);
		double ax1 = getDoubleValue(lineEnd,0);
		double ay1 = getDoubleValue(lineEnd,1);

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
	
	/** This method reads a double value from the array. It is expected that the object is
	 * a FormattedDecimal. If it is not, this method jsut tries letting the JSONObject 
	 * convert it to a double. */
	private double getDoubleValue(JSONArray array, int index) throws Exception {
		//should be a formatted decimal. Otherwise it better be something that cast to double.
		Object obj = array.get(index);
		if(obj instanceof FormattedDecimal) {
			return ((FormattedDecimal)obj).getValue();
		}
		else {
			return array.getDouble(index);
		}
	}
}
