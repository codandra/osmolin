package intransix.indoor.provision.geom;

import java.util.*;
import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.MapTemplate;
import org.json.*;

/**
 *
 * @author sutter
 */
public class OsmMap extends OsmRelation {
	
	String name;
	HashSet<OsmLevel> levels = new HashSet<OsmLevel>();
	OsmFeature parent;
	
	public OsmMap(long id) {
		super(id);
	}
	
	public static void loadOsmMap(long id,
			JSONObject json, 
			MapTemplate mapTemplate,
			MapProvision mapProvision) {
		
		OsmMap map = mapProvision.getOsmMap(id);
		
		//get the name
		map.name = getTagString(json,NAME_KEY);
		if(map.name == null) map.name = String.valueOf(id);
		
		//load the members
		map.loadMembers(json,mapTemplate,mapProvision);
		
		//flag as loaded
		map.loaded = true;
		
	}
	
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
			this.levels.add(level);
		}
	}
}
