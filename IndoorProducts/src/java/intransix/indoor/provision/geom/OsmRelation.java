/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.indoor.provision.geom;

import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.MapTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public abstract class OsmRelation extends OsmObject {
	
	public final static String MEMBERS_KEY = "members";
	public final static String ROLE_KEY = "role";
	
	
	
	public OsmRelation(long id) {
		super(id);
	}
	
	public static void loadOsmRelation(long id,
			JSONObject json, 
			MapTemplate mapTemplate,
			MapProvision mapProvision) {
		
		final String type = getTagString(json,TYPE_KEY);
		if(type == null) return;
		
		if(type.equalsIgnoreCase(mapTemplate.TYPE_LEVEL)) {
			OsmLevel.loadOsmLevel(id,json,mapTemplate,mapProvision);
		}
		else if(type.equalsIgnoreCase(mapTemplate.TYPE_STRUCTURE)) {
			OsmMap.loadOsmMap(id,json,mapTemplate,mapProvision);
		}
		else if(type.equalsIgnoreCase(mapTemplate.TYPE_MULTIPOLYGON)) {
			OsmMultipoly.loadOsmMultipoly(id,json,mapTemplate,mapProvision);
		}
	}
	
	protected void loadMembers(JSONObject json, 
			MapTemplate mapTemplate,
			MapProvision mapProvision) {
	
		JSONArray members = json.optJSONArray(MEMBERS_KEY);
		if(members == null) return;
		
		int cnt = members.length();
		for(int i = 0; i < cnt; i++) {
			JSONObject member = members.optJSONObject(i);
			if(member == null) continue;
			
			long memberId = member.optLong(REF_KEY,INVALID_ID);
			if(memberId == INVALID_ID) continue;
			
			String type = member.optString(TYPE_KEY,null);
			if(type == null) continue;
			
			String role = member.optString(ROLE_KEY,null);
			if(role == null) continue;
			
			loadMember(memberId,type,role,mapTemplate,mapProvision);
		}
	}
	
	protected abstract void loadMember(long memberId, String type, String role,
			MapTemplate mapTemplate, MapProvision mapProvision);
}
