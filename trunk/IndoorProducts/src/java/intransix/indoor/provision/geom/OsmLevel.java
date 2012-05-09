/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.indoor.provision.geom;

import java.util.*;
import intransix.indoor.provision.MapProvision;
import intransix.indoor.provision.mapinfo.MapTemplate;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class OsmLevel extends OsmRelation {
	
	public final static int INVALID_ZLEVEL = Integer.MIN_VALUE;
	
	String name;
	int zlevel;
	OsmFeature shell;
	HashSet<OsmFeature> features = new HashSet<OsmFeature>();
	
	public OsmLevel(long id) {
		super(id);
	}
	
	public static void loadOsmLevel(long id,
			JSONObject json, 
			MapTemplate mapTemplate,
			MapProvision mapProvision) {
		
		OsmLevel level = mapProvision.getOsmLevel(id);
		
		//get the zlevel
		level.zlevel = getTagInt(json,mapTemplate.KEY_ZLEVEL,0);
		
		//load the name
		level.name = getTagString(json,NAME_KEY);
		if(level.name == null) level.name = String.valueOf(id);
		
		//load the level
		level.loadMembers(json, mapTemplate, mapProvision);
		
		//flag as loaded
		level.loaded = true;
	}
	
	@Override
	protected void loadMember(long memberId, String type, String role,
			MapTemplate mapTemplate, MapProvision mapProvision) {
		
		if(role.equalsIgnoreCase(mapTemplate.ROLE_SHELL)) {
			//try to load the parent
			if(type.equalsIgnoreCase(NODE_TYPE)) {
				this.shell = mapProvision.getOsmNode(memberId);
			}
			else if(type.equalsIgnoreCase(OsmObject.WAY_TYPE)) {
				this.shell = mapProvision.getOsmWay(memberId);
			}
			else {
				this.shell = null;
			}
		}
		else if(role.equalsIgnoreCase(mapTemplate.ROLE_FEATURE)) {
			//try to load the feature
			OsmFeature feature = null;
			if(type.equalsIgnoreCase(NODE_TYPE)) {
				feature = mapProvision.getOsmNode(memberId);
			}
			else if(type.equalsIgnoreCase(OsmObject.WAY_TYPE)) {
				feature = mapProvision.getOsmWay(memberId);
			}
			if(feature != null) {
				feature.zlevel = zlevel;
				this.features.add(feature);
			}
		}
	}
	
}
