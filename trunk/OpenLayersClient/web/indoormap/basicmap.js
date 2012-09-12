indoor = {};

indoor.HOST_URL = "http://open.micello.com/mapdata";

indoor.getMapUrl = function(mapId) {
	return indoor.HOST_URL + "/file/indoormap/" + mapId;
}

indoor.getLevelUrl = function(levelId) {
	return indoor.HOST_URL + "/file/lvlgeom/" + levelId;
}

indoor.BasicMap = function(elementId,styleMap) {
	//create map
	this.mapElementId = elementId;
	this.styleMap = styleMap;
	
	this.map = null;
	this.selectControl = null;
	this.popup = null;
	
	this.currentMap = null;
}

indoor.BasicMap.prototype.openMap = function(mapId) {
	var indoorMap = this;
	var onDownload = function(response) {
		var data = eval('(' + response.responseText + ')');
		indoorMap.mapLoaded(data);
	}
	OpenLayers.Request.GET({
		url:indoor.getMapUrl(mapId),
		success:onDownload,
		failure:indoor.failedRequest
	})
}

indoor.BasicMap.prototype.mapLoaded = function(mapData) {
	if(mapData != null) {
		this.map = new OpenLayers.Map({
			"div": this.mapElementId,
			"maxExtent":new OpenLayers.Bounds(0,0,mapData.w,mapData.h)
		});
		
		this.mapData = mapData;
		
		//load levels
		var levels = mapData.lvl;
		var initialLayer = null;
		if(levels) {
			for(var i = 0; i < levels.length; i++) {
				var level = levels[i];
				var layer = new OpenLayers.Layer.Vector(level.nm, {
                    "strategies": [new OpenLayers.Strategy.Fixed()],                
                    "protocol": new OpenLayers.Protocol.HTTP({
                        "url": indoor.getLevelUrl(level.id),
                        "format": new OpenLayers.Format.GeoJSON()
                    }),
					"styleMap": this.styleMap,
					"isBaseLayer":true
                });
			
				this.map.addLayer(layer);
				if(level.z == 0) {
					initialLayer = layer;
				}	
			}
		}
		
		if(initialLayer != null) this.map.setBaseLayer(initialLayer);

		this.map.addControl(new OpenLayers.Control.LayerSwitcher());
		this.map.zoomToMaxExtent();
	}
}



//code for addign selection
//				layer.events.on({
//					'beforefeatureselected':indoorMap.selectTest,
//					'featureselected':indoorMap.onFeatureSelect,
//					'featureunselected':indoorMap.onFeatureUnselect
//				});
//				
//		this.selectControl = new OpenLayers.Control.SelectFeature(initialLayer);
//		this.map.addControl(this.selectControl);
//		this.selectControl.activate();
//		
//		this.map.events.on({
//			'changebaselayer':indoorMap.onBaseChanged
//		});
//
////FIX THIS!!!!!
//indoor.IndoorMap.prototype.onBaseChanged = function(evt) {
//	var map = evt.object;
//	var baseLayer = map.baseLayer;
//	var activePopup = map.activePopup;
//	if(activePopup) {
//		map.removePopup(activePopup);
//		activePopup.destroy();
//		map.activePopup = null;
//	}
//	
//	this.selectControl.setLayer(baseLayer);
//		}
//indoor.IndoorMap.prototype.selectTest = function(evt) {
//			if((evt.feature)&&(evt.feature.data)&&(evt.feature.data.name)) {
//				return true;
//			}
//			else return false;
//		}
//		
//indoor.IndoorMap.prototype.onPopupClose = function(evt) {
//			// 'this' is the popup.
//			var popup = this;
//			this.selectControl.unselect(popup.feature);
//		}
//indoor.IndoorMap.prototype.onFeatureSelect = function(evt) {
//			var layer = this;
//			var map = layer.map;
//			var feature = evt.feature;
//			this.popup = new OpenLayers.Popup.FramedCloud("featurePopup",
//									feature.geometry.getBounds().getCenterLonLat(),
//									new OpenLayers.Size(100,100),
//									"<h2>" + feature.data.name + "</h2> This is feature " + feature.id,
//									null, true, this.onPopupClose);
//			feature.popup = this.popup;
//			this.popup.feature = feature;
//			this.map.addPopup(this.popup);
//		}
//indoor.IndoorMap.prototype.onFeatureUnselect = function(evt) {
//			var feature = evt.feature;
//			var layer = this;
//			var map = layer.map;
//			var popup = feature.popup;
//			if (feature.popup) {
//				feature.popup.feature = null;
//				map.removePopup(feature.popup);
//				feature.popup.destroy();
//				feature.popup = null;
//			}
//		}
