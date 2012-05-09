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
	String type;
	int zorder;
	FeatureTypeInfo fti;
	
	public OsmFeature(long id) {
		super(id);
	}
	
	public boolean loadFeatureProperties(JSONObject json, MapTemplate mapTemplate) {
		
		fti = mapTemplate.getFeatureTypeInfo(json);
		properties = new JSONObject();
		
		try {
			type = fti.getTypeName();
			zorder = fti.getZorder();
		
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
			
	
}
