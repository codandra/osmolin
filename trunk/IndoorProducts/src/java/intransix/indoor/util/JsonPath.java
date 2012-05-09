/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package intransix.indoor.util;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import org.json.*;

/**
 *
 * @author sutter
 */
public class JsonPath {
/*
	public final static int MOVE_TO = 0;
	public final static int LINE_TO = 1;
	public final static int QUAD_TO = 2;
	public final static int CUBE_TO = 3;
	public final static int CLOSE = 4;

	public static JSONArray getPathJson(MPath mPath) throws Exception {

		JSONArray jsonArray = new JSONArray();

		Path2D path = (Path2D)mPath.getShape();
		PathIterator pi = path.getPathIterator(null);
		double[] points = new double[6];
		int command;
		int numCount;
		for(; !pi.isDone(); pi.next()) {
			//process path commands
			int segType = pi.currentSegment(points);
			switch(segType) {
				case PathIterator.SEG_MOVETO:
					command = MOVE_TO;
					numCount = 2;
					break;
				case PathIterator.SEG_LINETO:
					command = LINE_TO;
					numCount = 2;
					break;
				case PathIterator.SEG_CUBICTO:
					command = CUBE_TO;
					numCount = 6;
					break;
				case PathIterator.SEG_QUADTO:
					command = QUAD_TO;
					numCount = 4;
					break;
				case PathIterator.SEG_CLOSE:
					command = CLOSE;
					numCount = 0;
					break;
				default:
					throw new Exception("Unknown or unimplmeneted curve type.");
			}

			JSONArray commandArray = new JSONArray();
			commandArray.put(command);
			for(int i = 0; i < numCount; i++) {
				commandArray.put(new FormattedDecimal(points[i],3));
			}
			jsonArray.put(commandArray);
		}

		return jsonArray;
	}
*/ 
}
