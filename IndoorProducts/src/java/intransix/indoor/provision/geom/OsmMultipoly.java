package intransix.indoor.provision.geom;

import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.MapTemplate;
import org.json.*;
import java.util.ArrayList;

/**
 *
 * @author sutter
 */
public class OsmMultipoly extends OsmRelation {
	
	public final static String ROLE_OUTER = "outer";
	public final static String ROLE_INNER = "inner";
	
	private long minWayId;
	private ArrayList<MemberPolygon> insides = new ArrayList<MemberPolygon>();
	private ArrayList<MemberPolygon> outsides = new ArrayList<MemberPolygon>();
	
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
	
	
	@Override
	protected void loadMember(long memberId, String type, String role,
			MapTemplate mapTemplate, MapProvision mapProvision) {
		
		OsmFeature feature = null;
		if(type.equalsIgnoreCase(OsmObject.WAY_TYPE)) {
			feature = mapProvision.getOsmWay(memberId);
		}
		
		if(feature != null) {
			if(role.equalsIgnoreCase(ROLE_OUTER)) {
			}
			else if(role.equalsIgnoreCase(ROLE_INNER)) {
			}
		}
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
