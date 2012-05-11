package intransix.indoor.provision.geom;

import intransix.indoor.geom.AffineTransform;
import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.FeatureTypeInfo;
import intransix.indoor.provision.mapinfo.MapTemplate;
import org.json.*;
import java.util.*;

/**
 *
 * @author sutter
 */
public class OsmMultipoly extends OsmRelation {
	
	public final static String ROLE_OUTER = "outer";
	public final static String ROLE_INNER = "inner";
	
	private long minWayId = Long.MAX_VALUE;
	private JSONObject properties = new JSONObject();
	private FeatureTypeInfo fti = null;
	private ArrayList<OsmWay> insides = new ArrayList<OsmWay>();
	private ArrayList<OsmWay> outsides = new ArrayList<OsmWay>();
	
	private HashMap<String,ArrayList<OsmWay>> polyMap = new HashMap<String,ArrayList<OsmWay>>();
	
	public OsmMultipoly(long id) {
		super(id);
	}
	
	public static void loadOsmMultipoly(long id,
			JSONObject json, 
			MapTemplate mapTemplate,
			MapProvision mapProvision) {
		
		OsmMultipoly mp = mapProvision.getOsmMultipoly(id);
		
		//load the level
		mp.loadMembers(json, mapTemplate, mapProvision);
		
		//flag as loaded
		mp.loaded = true;
	}
	
	/** This method must be called to combine the multipolygons, after the
	 * nodes, ways and levels have all be set. */
	public void finalizePoly() {
		//put each inside in the appropriate outside
		for(OsmWay inside:insides) {
			for(OsmWay outside:outsides) {
				if(outside.contains(inside)) {
					outside.addHole(inside);
				}
			}
		}
		
		//seperate the outsides by level
		for(OsmWay outside:outsides) {
			String key = OsmLevel.createZlevelKey(outside.getZcontext(),outside.getZlevel()); 
			ArrayList<OsmWay> ways = polyMap.get(key);
			if(ways == null) {
				ways = new ArrayList<OsmWay>();
				polyMap.put(key,ways);
			}
			ways.add(outside);
		}
	}
	
	/** This method returns a compound way for the given object, or null,
	 * so that the ways of the multipolygon are properly combined. */
	public JSONObject getGeomeryForWayObject(OsmWay way, 
			MapTemplate mapTemplate, AffineTransform lonlatToXY) throws Exception  {
		
		String key = OsmLevel.createZlevelKey(way.getZcontext(),way.getZlevel());
		//get ways for this level
		ArrayList<OsmWay> levelList = polyMap.get(key);
		//return a non-null geom only for the way corresponding to the first in the list
		if((levelList != null)&&(levelList.indexOf(way) == 0)) {
			return getMultipolygonJson(levelList,mapTemplate,lonlatToXY);
		}
		else {
			return null;
		}
	}
	
	//============================
	// Protected Methods;
	//============================
	
	
	@Override
	protected void loadMember(long memberId, String type, String role,
			MapTemplate mapTemplate, MapProvision mapProvision) {
		
		OsmWay way = null;
		if(type.equalsIgnoreCase(OsmObject.WAY_TYPE)) {
			way = mapProvision.getOsmWay(memberId);
			if(way != null) {
				
				//get id - find the smallest one and use that
				if(way.getId() < minWayId) {
					minWayId = way.getId();
				}
				
				//use the first non-null feature type for the group
				FeatureTypeInfo wayFti = way.getFeatureTypeInfo();
				if((fti!= null)&&(wayFti != null)) {
					fti = wayFti;
				}
				
				//add any new properties to the properties for the group
				try {
					JSONObject wayProps = way.getProperties();
					Iterator iter = wayProps.keys();
					while(iter.hasNext()) {
						String key = (String)iter.next();
						if(!properties.has(key)) {
							properties.put(key,wayProps.get(key));
						}
					}
				}
				catch(Exception ex) {
					//nothign to do here, just continue
				}
				
				//store the way
				way.setMultipoly(this);
				if(role.equalsIgnoreCase(ROLE_OUTER)) {
					//we SHOULD store the outsides in order so any inner island
					//appears inside of its surrounds, other wise inner island holes
					//will not work right. This scenario is exceedingly rare anyway.
					//SO I WON'T DO IT FOR NOW
					this.outsides.add(way);
				}
				else if(role.equalsIgnoreCase(ROLE_INNER)) {
					this.insides.add(way);
				}
			}
		}
	}
	
	private JSONObject getMultipolygonJson(ArrayList<OsmWay> wayList, 
			MapTemplate mapTemplate, AffineTransform lonlatToXY)  throws Exception {
		
		JSONObject geomJson = new JSONObject();
		
		//get linestring for json
		JSONArray multipolyJson = new JSONArray();
		for(OsmWay way:wayList) {
			JSONArray wayJson = way.getJsonPointArrayList(lonlatToXY, mapTemplate.COORDINATE_PRECISION);
			if(wayJson != null) continue;
			multipolyJson.put(wayJson);
		}
		
		geomJson.put("type","MultiPolygon");
		geomJson.put("coordinates",multipolyJson);

		return geomJson;
	}
//	
//	public static OsmMultipoly getMultipolyRelation(JSONObject json) throws Exception {
//		OsmMultipoly mpr = new OsmMultipoly();
//		
//		mpr.relId = json.getLong("id");
//		
//		mpr.minWayId = Long.MAX_VALUE;
//		JSONArray membersJson = json.getJSONArray("members");
//		int cnt = membersJson.length();
//		for(int i = 0; i < cnt; i++) {
//			JSONObject memberJson = membersJson.getJSONObject(i);
//			MemberPolygon member = MemberPolygon.initPolygon(memberJson);
//			if(member != null) {
//				if(member.getIsInner()) {
//					mpr.insides.add(member);
//				}
//				else {
////I SHOULD ORDER THESE SO IF ONE OUTSIDE IS INSIDE ANOTHER, THE INSIDE COMES FIRST
////TO PROPERLY HANDLE AN ISLAND IN A DONUT
//					mpr.outsides.add(member);
//				}
//				if(member.getWayId() < mpr.minWayId) mpr.minWayId = member.getWayId();
//			}
//		}
//		
//		return mpr;
//	}
//	
//	public ArrayList<MemberPolygon> getInsides() {
//		return insides;
//	}
//	
//	public ArrayList<MemberPolygon> getOutsides() {
//		return outsides;
//	}
//	
//	public JSONObject createCombinedFeature() throws Exception {
//		//add ins to outs
//		for(MemberPolygon inside:insides) {
//			for(MemberPolygon outside:outsides) {
//				if(outside.contains(inside)) {
//					outside.insert(inside);
//					break;
//				}
//			}
//		}
//		//create a single feature from the outsides
//		ArrayList<JSONObject> polygons = new ArrayList<JSONObject>();
//		MemberPolygon main = null;
//		for(MemberPolygon outside:outsides) {
//			//get the polygons
//			polygons.add(outside.getPolygonGeom());
//			//copmbine the properties
//			if(main == null) {
//				main = outside;
//				continue;
//			}
//			main.importProperties(outside);
//		}
//		//get the output object
//		main.setPolygons(polygons);
//		
//		main.setId(this.minWayId);
//		
//		return main.getFeature();
//	}
}
