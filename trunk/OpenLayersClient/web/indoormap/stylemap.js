		
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
	var rule_label_name = new OpenLayers.Rule({
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
			"fontSize": 12,
			"fontWeight": "600"
		}
	});
	var rule_label_ref = new OpenLayers.Rule({
		"filter": new OpenLayers.Filter.Logical({
			"type": OpenLayers.Filter.Logical.AND,
			"filters": [
				new OpenLayers.Filter.Comparison({
					"type": OpenLayers.Filter.Comparison.EQUAL_TO,
					"property": "name",
					"value": null
				}),
				new OpenLayers.Filter.Comparison({
					"type": OpenLayers.Filter.Comparison.NOT_EQUAL_TO,
					"property": "ref",
					"value": null
				})
			]
		}),
		"symbolizer": {
			"label": "${ref}",
			"fontColor": "#000000",
			"fontOpacity": 1,
			"fontFamily": "Arial",
			"fontSize": 12,
			"fontWeight": "600"
		}
	});
	defaultStyle.addRules([rule_basic, rule_label_name,rule_label_ref]);
	
	var styles = new OpenLayers.StyleMap(defaultStyle);
	styles.addUniqueValueRules("default", "buildingpart", {
		"unit": {
			"strokeColor": "#415a81", 
			"strokeWidth": 2, 
			"fillColor":"#8ca9df"
		},
		"corridor": {
			"strokeColor": "#415a81", 
			"strokeWidth": 2, 
			"fillColor":"#d2dbe9"
		},
		"hall":{
			"strokeColor": "#415a81", 
			"strokeWidth": 2, 
			"fillColor":"#8ca9df"
		},
		"room": {
			"strokeColor": "#415a81", 
			"strokeWidth": 2, 
			"fillColor":"#8ca9df"			
		},
		"bathroom": {
			"strokeColor": "#415a81", 
			"strokeWidth": 2, 
			"fillColor":"#55ac5a",
			"externalGraphic":"res/bathroom.png"
		},
		"kiosk": {
			"strokeColor": "#415a81", 
			"strokeWidth": 2, 
			"fillColor":"#798fb8"
		},
		"stairs":{
			"strokeColor": "#415a81", 
			"strokeWidth": 2, 
			"fillColor":"#b4c3dc",
			"externalGraphic":"res/stairs.png",
			"graphicOpacity":.8			
			
		},
		"escalator": {
			"externalGraphic":"res/escalator.png"
		},
		"elevator":{
			"strokeColor": "#415a81", 
			"strokeWidth": 2, 
			"fillColor":"#b4c3dc",
			"externalGraphic":"res/elevator.png"	
		}		
	});
	styles.addUniqueValueRules("default", "buildingpart:verticalpassage", {
		"stairway":{
			"strokeColor": "#415a81", 
			"strokeWidth": 2, 
			"fillColor":"#b4c3dc",
			"externalGraphic":"res/stairs.png"			
		},
		"elevator":{
			"strokeColor": "#415a81", 
			"strokeWidth": 2, 
			"fillColor":"#b4c3dc",
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
