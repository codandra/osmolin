package intransix.indoor.provision.geom;

import intransix.indoor.provision.mapinfo.FeatureTypeInfo;
import org.json.*;
import intransix.indoor.provision.mapinfo.MapTemplate;
import intransix.indoor.provision.mapinfo.PropertyKey;
import intransix.indoor.geom.*;


/**
 *
 * @author sutter
 */
public abstract class OsmFeature extends OsmObject implements Comparable<OsmFeature> {
	JSONObject properties;
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

	public void setZlevel(int zlevel) {
		this.zlevel = zlevel;
	}
	
	public void setZcontext(long zcontext) {
		this.zcontext = zcontext;
	}

	public FeatureTypeInfo getFeatureTypeInfo() {
		return fti;
	}

	/** This method returns true if the feature should be placed in the map. */
	public boolean placeInMap() {
		return ((getIsLoaded())&&(fti != null));
	}
	
	public JSONObject getJsonObject(MapTemplate mapTemplate, AffineTransform lonlatToXY) throws Exception {
		
		JSONObject featureJson = new JSONObject();
		featureJson.put("type","Feature");
		featureJson.put("id",getId());
		featureJson.put("geometry",getGeometryJson(mapTemplate,lonlatToXY));
		featureJson.put("properties",properties);

		return featureJson;
	}
	
	@Override
	public int compareTo(OsmFeature feature) {
		//return negative if this feature is before passed feature.
		//compare based on draw order. If the comparison happens, it is assumed
		//the objects are on the same level
		FeatureTypeInfo thisFti = this.getFeatureTypeInfo();
		FeatureTypeInfo otherFti = feature.getFeatureTypeInfo();
		
		//return o if no drawing order is present
		if((thisFti == null)||(otherFti == null)) return 0;
		
		return (thisFti.getZorder() - otherFti.getZorder());
	}
	
	//=========================
	// Protected Methods
	//=========================

	protected boolean loadFeatureProperties(JSONObject json, MapTemplate mapTemplate) {
		
		fti = mapTemplate.getFeatureTypeInfo(json);
		
		//read properties if this is a type node. Otherwise just leave blank
		if(fti != null) {
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
		}

		return true;
	}
	
	protected abstract JSONObject getGeometryJson(MapTemplate mapTemplate, AffineTransform lonlatToXY) throws Exception;


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


}
