package data;

import javax.servlet.http.HttpServletRequest;

/**
 * This class reads the file info from the path. It is in the format servlet/name/key/version,
 * with the version optional. The servlet url pattern is servlet/*
 * @author sutter
 */
class RestFileInfo {
	final static int INVALID_VERSION = -1;

	String name;
	String key;
	int version;

	static RestFileInfo loadFileInfo(HttpServletRequest request) throws Exception {
		RestFileInfo fileInfo = new RestFileInfo();

		String path = request.getPathInfo();
		if(path.charAt(0) == '/') {
			path = path.substring(1);
		}

		String[] params = path.split("/");
		if(params.length < 2) throw new Exception("Invalid request format");

		//url pattern "/name/key/version"
		fileInfo.name = params[0];
		fileInfo.key = params[1];
		if(params.length > 2) {
			String versionString = params[2];
			fileInfo.version = Integer.parseInt(versionString);
		}
		else {
			fileInfo.version = INVALID_VERSION;
		}

		return fileInfo;
	}
}

