package data;

import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public class GeoJsonUtils {
	
	/** This method returns the centroid of an arbitrary geoJSON object. */
	public static Point2D getGeoJsonCentroid(JSONObject geoJson) throws Exception {
		String type = geoJson.optString("type",null);
		if(type == null) {
			throw new Exception("GeoJSON type parameter not found.");
		}
		
		if(type.equalsIgnoreCase("FeatureCollection")) {
			return getFeatureCollectionCentroid(geoJson);
		}
		else if(type.equalsIgnoreCase("Feature")) {
			return getFeatureCentroid(geoJson);
		}
		else if(type.equalsIgnoreCase("Geometry")) {
			return getGeometryCentroid(geoJson);
		}
		else if(type.equalsIgnoreCase("GeometryCollection")) {
			return getGeometryCollectionCentroid(geoJson);
		}
		else {
			throw new Exception("GeoJSON type not supported: " + type);
		}
	}

	/** This method returns the centroid of a geoJSON feature collection object. */
	public static Point2D getFeatureCollectionCentroid(JSONObject geoJson) throws Exception {
		JSONArray features = geoJson.getJSONArray("features");
		double totalX = 0;
		double totalY = 0;
		Point2D point;
		int cnt = features.length();
		if(cnt == 0) return null;
		
		JSONObject feature;
		for(int i = 0; i < cnt; i++) {
			feature = features.getJSONObject(i);
			point = getFeatureCentroid(feature);
			totalX += point.getX();
			totalY += point.getY();
		}
		return new Point2D.Double(totalX / cnt, totalY / cnt);
	}
	
	/** This method returns the centroid of a geoJSON feature object. */
	public static Point2D getFeatureCentroid(JSONObject geoJson) throws Exception {
		JSONObject geometry = geoJson.getJSONObject("geometry");
		return getGeometryCentroid(geometry);
	}
	
	/** This method returns the centroid of a geoJSON geometry object. */
	public static Point2D getGeometryCentroid(JSONObject geoJson) throws Exception {
		String type = geoJson.optString("type",null);
		if(type == null) {
			throw new Exception("Geometry type parameter not found.");
		}
		
		if(type == "GeometryCollection") {
			return getGeometryCollectionCentroid(geoJson);
		}
		else {
			JSONArray coordinates = geoJson.getJSONArray("coordinates");
			return getCoordinatesCentroid(coordinates);
		}
	}
	
	/** This method returns the centroid of a geoJSON geometry collection object. */
	public static Point2D getGeometryCollectionCentroid(JSONObject geoJson) throws Exception {
		JSONArray geometries = geoJson.getJSONArray("geometries");
		double totalX = 0;
		double totalY = 0;
		Point2D point;
		int cnt = geometries.length();
		if(cnt == 0) return null;
		
		JSONObject geometry;
		for(int i = 0; i < cnt; i++) {
			geometry = geometries.getJSONObject(i);
			point = getGeometryCentroid(geometry);
			totalX += point.getX();
			totalY += point.getY();
		}
		return new Point2D.Double(totalX / cnt, totalY / cnt);
	}
	
	/** This method calculates the centroid of a geojson coordinates entry in a geometry. */
	private static Point2D getCoordinatesCentroid(JSONArray coordinates) throws Exception {
		double totalX = 0;
		double totalY = 0;
		Point2D point;
		int cnt = coordinates.length();
		if(cnt == 0) return null;
		
		Object element;
		for(int i = 0; i < cnt; i++) {
			element = coordinates.get(i);
			if(element instanceof JSONArray) {
				//this is an array of coordinates
				point = getCoordinatesCentroid((JSONArray)element);
				totalX += point.getX();
				totalY += point.getY();
			}
			else {
				//this object was a point
				double x = coordinates.getDouble(0);
				double y = coordinates.getDouble(1);
				return new Point2D.Double(x,y);
			}
			
		}
		
		return new Point2D.Double(totalX / cnt, totalY / cnt);
	}
	
	
}
