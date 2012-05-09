
indoor.IndoorMap = function(elementId,styleMap) {
	//create map
	this.mapElementId = elementId;
	this.map = null;
	
	//load style map base
//	this.defaultStyle = new Openlayers.Style();
//	this.styleMap = new OpenLayers.StyleMap(this.defaultStyle);
this.styleMap = styleMap;
	
	this.selectControl = null;
	this.popup = null;
	
	this.currentMap = null;
}

indoor.IndoorMap.prototype.openMap = function(mapId) {
	var indoorMap = this;
	var onDownload = function(mapData) {
		indoorMap.mapLoaded(mapData);
	}
	indoor.Request.loadMap(mapId,onDownload);
}

indoor.IndoorMap.prototype.mapLoaded = function(mapData) {
	if(mapData != null) {
		this.map = new OpenLayers.Map({
			"div": this.mapElementId,
			"maxExtent":new OpenLayers.Bounds(0,0,mapData.w,mapData.h)
		});
		
		this.mapData = mapData;
		
		//load style map
//		this.loadTheme(mapData.ns);

var indoorMap = this;
		
		//load levels
		var levels = mapData.lvl;
		var initialLayer = null;
		if(levels) {
			for(var i = 0; i < levels.length; i++) {
				var level = levels[i];
				var layer = new OpenLayers.Layer.Vector(level.nm, {
                    "strategies": [new OpenLayers.Strategy.Fixed()],                
                    "protocol": new OpenLayers.Protocol.HTTP({
                        "url": indoor.Request.getLevelUrl(level.id),
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

//FIX THEMES!!!
indoor.IndoorMap.prototype.loadTheme = function(namespaces) {
	var indoorMap = this;
	var onDownload = function(theme) {
		indoorMap.themeLoaded(theme);
	}
	for(var i = 0; i < namespaces.length; i++) {
		indoor.Request.loadMap(namespaces[i],onDownload);
	}
}

indoor.IndoorMap.prototype.themeLoaded = function(theme) {
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
