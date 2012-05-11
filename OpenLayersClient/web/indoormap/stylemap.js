		
function getStyleMap() {
	var defaultStyle = new OpenLayers.Style();
	var rule_basic = new OpenLayers.Rule({
		symbolizer: {
			"strokeColor": "#a0a0a0", 
			"strokeWidth": 1, 
			"strokeOpactiy":1,
			"fillColor":"#c0c0c0",
			"fillOpactiy":1.0
		}
	});
	var rule_label = new OpenLayers.Rule({
		"filter": new OpenLayers.Filter.Comparison({
			"type": OpenLayers.Filter.Comparison.NOT_EQUAL_TO,
			"property": "name",
			"value": null
		}),
		"symbolizer": {
			"label": "${name}",
			"fontColor": "#000000",
			"fontOpacity": 1,
			"fontFamily": "Arial",
			"fontSize": 16,
			"fontWeight": "600"
		}
	});
	defaultStyle.addRules([rule_basic, rule_label]);
	
	var styles = new OpenLayers.StyleMap(defaultStyle);
	styles.addUniqueValueRules("default", "buildingpart", {
		"room": {
			"strokeColor": "#222222", 
			"strokeWidth": 2, 
			"fillColor":"#ff8080"			
		},
		"bathroom": {
			"strokeColor": "#222222", 
			"strokeWidth": 2, 
			"fillColor":"#00ff00",
			"externalGraphic":"res/bathroom.png"
		},
		"kiosk": {
			"strokeColor": "#444444", 
			"strokeWidth": 2, 
			"fillColor":"#ffc0c0"
		},
		"corridor": {
			"strokeColor": "#666666", 
			"strokeWidth": 2, 
			"fillColor":"#ffc0c0"
		},
		"unit": {
			"strokeColor": "#888888", 
			"strokeWidth": 2, 
			"fillColor":"#e0ffc0"
		},
		"hall":{
			"strokeColor": "#888888", 
			"strokeWidth": 2, 
			"fillColor":"#e0ffc0"
		},
		"stairs":{
			"strokeColor": "#888888", 
			"strokeWidth": 2, 
			"fillColor":"#e0ffc0",
			"externalGraphic":"res/stairs.png",
			"graphicOpacity":.5			
			
		},
		"escalator": {
			"externalGraphic":"res/escalator.png"
		},
		"elevator":{
			"strokeColor": "#888888", 
			"strokeWidth": 2, 
			"fillColor":"#e0ffc0",
			"externalGraphic":"res/elevator.png"	
		}		
	});
	styles.addUniqueValueRules("default", "buildingpart:verticalpassage", {
		"stairway":{
			"strokeColor": "#888888", 
			"strokeWidth": 2, 
			"fillColor":"#e0ffc0",
			"externalGraphic":"res/stairs.png"			
		},
		"elevator":{
			"strokeColor": "#888888", 
			"strokeWidth": 2, 
			"fillColor":"#e0ffc0",
			"externalGraphic":"res/elevator.png"	
		},
		"escalator": {
			"externalGraphic":"res/escalator.png"
		}
	});
	styles.addUniqueValueRules("default", "shell", {
		"yes": {
			"strokeColor": "#000000", 
			"strokeWidth": 2, 
			"fillColor":"#eeeeee"
		}		
				
	});

	return styles;
};
