/**
 * @namespace This is a namespace that contains some static network request functions.
 */
indoor.Request = {}

indoor.Request.getLevelUrl = function(levelId) {
	return indoor.HOST_URL + "/file/lvlgeom/" + levelId;
//return "geojson3.json";
}

/**
 * This method downloads a theme.
 */
indoor.Request.loadTheme = function(mapTemplate,family,themeName,onDownload) {
	var url = indoor.HOST_URL + "/file/theme/" + mapTemplate + "_" + family + "_" + themeName;
	indoor.Request.doRequest(url,onDownload,alert,"GET");
}

/**
 * This method downloads a community, given by the community ID.
 */
indoor.Request.loadMap = function(mapId,onDownload) {
	var url = indoor.HOST_URL + "/file/indoormap/" + mapId;
	indoor.Request.doRequest(url,onDownload,alert,"GET");
}

/**
 * This method does ah HTTP request to the given URL. On success, the requeted data
 * is passed to the function onDownload. On failure, a msg is passed to the function
 * onFailure. The argument httpMethod determimes the request method ("get" or "post")
 * and the argument body gives the http body to be sent with the request.
 */
indoor.Request.doRequest= function(url,onDownload,onFailure,httpMethod,body) {
	var xmlhttp;
	var doIe = false;
	if(window.XDomainRequest) {
		// code for IE6, IE5
		xmlhttp=new XDomainRequest();
		doIe = true;
	}
	else if(window.XMLHttpRequest) {
		// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp=new XMLHttpRequest();
	}
	else {
		alert("This browser is not supported.");
		return;
	}

	if(!doIe) {
		//standard xmlhttp
		xmlhttp.dataManager = this;

		xmlhttp.onreadystatechange=function() {
			var msg;
			if (xmlhttp.readyState==4 && xmlhttp.status==200) {
				var data = eval('(' + xmlhttp.responseText + ')');
				if(data.error) {
					msg = "Error in request: " + data.error;
					onFailure(msg);
				}
				else {
					onDownload(data);
				}
			}
			else if(xmlhttp.readyState==4  && xmlhttp.status >= 400) {
				msg = "Error in http request. Status: " + xmlhttp.status;
				onFailure(msg);
			}
		}
	}
	else {
		//microsort xdomainrequest
		xmlhttp.onload = function() {
			var data = eval('(' + xmlhttp.responseText + ')');
			if(data.error) {
				msg = "Error in request: " + data.error;
				onFailure(msg);
			}
			else {
				onDownload(data);
			}
		}
		xmlhttp.onerror = function() {
			msg = "Error in http request. Status: " + xmlhttp.status;
			onFailure(msg);
		}
	}

	xmlhttp.open(httpMethod,url,true);
	if(!doIe) {
		//this is only for opera and android 3.1 talking to ruby - remove this at some point
		xmlhttp.setRequestHeader("Accept", "application/json,*/*");
	}

	if(httpMethod == "POST") {
		xmlhttp.setRequestHeader("Content-Type","text/plain");
	}
	xmlhttp.send(body);
}


