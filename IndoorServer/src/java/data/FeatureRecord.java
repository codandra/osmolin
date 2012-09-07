package data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class FeatureRecord {

	public static final String ID_COLUMN = "id";
	public static final String LAYER_COLUMN = "layer";
	public static final String KEY_COLUMN = "feature_key";
	public static final String QUADKEY_COLUMN = "quadkey";
	public static final String FEATURE_COLUMN = "feature";

	private long id;
	private String layer;
	private String key;
	private String quadkey;
	private String feature;

	public String getLayer() { return layer;}
	public String getKey() { return key;}
	public String getQuadkey() { return quadkey;}
	public String getFeature() { return feature;}


	public static FeatureRecord loadFromResultSet(ResultSet rs) throws Exception {
		FeatureRecord featureRecord = new FeatureRecord();
		featureRecord.id = rs.getLong(ID_COLUMN);
		featureRecord.layer = rs.getString(LAYER_COLUMN);
		featureRecord.key = rs.getString(KEY_COLUMN);
		featureRecord.quadkey = rs.getString(QUADKEY_COLUMN);
		featureRecord.feature = rs.getString(FEATURE_COLUMN);					
		return featureRecord;
	}
}

