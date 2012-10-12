		
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
		"wall": {
			"strokeColor": "#415a81", 
			"strokeWidth": 3, 
			"fillColor":"#415a81"
		},
		"door": {
			"strokeColor": "#55ac5a", 
			"strokeWidth": 3, 
			"fillColor":"#55ac5a"
		},
		"unit": {
			"strokeColor": "#415a81", 
			"strokeWidth": 3, 
			"fillColor":"#8ca9df"
		},
		"corridor": {
			"strokeColor": "#415a81", 
			"strokeWidth": 0, 
			"fillColor":"#d2dbe9"
		},
		"hall":{
			"strokeColor": "#415a81", 
			"strokeWidth": 0, 
			"fillColor":"#8ca9df"
		},
		"room": {
			"strokeColor": "#415a81", 
			"strokeWidth": 0, 
			"fillColor":"#8ca9df",
			"fillOpacity":.3
		},
		"room|bathroom:yes": {
			"strokeColor": "#415a81", 
			"strokeWidth": 0, 
			"fillColor":"#73bec5",
			"externalGraphic":"res/bathroom.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		},
		"room|bathroom:male": {
			"strokeColor": "#415a81", 
			"strokeWidth": 0, 
			"fillColor":"#73bec5",
			"externalGraphic":"res/bathroom_male.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		},
		"room|bathroom:female": {
			"strokeColor": "#415a81", 
			"strokeWidth": 0, 
			"fillColor":"#73bec5",
			"externalGraphic":"res/bathroom_female.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		},
		"room|bathroom:unisex": {
			"strokeColor": "#415a81", 
			"strokeWidth": 0, 
			"fillColor":"#73bec5",
			"externalGraphic":"res/bathroom.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		},
		"kiosk": {
			"strokeColor": "#415a81", 
			"strokeWidth": 3, 
			"fillColor":"#798fb8"
		},
		"stairs":{
			"strokeColor": "#415a81", 
			"strokeWidth": 1, 
			"fillColor":"#b4c3dc",
			"externalGraphic":"res/stairs.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		},
		"escalator": {
			"strokeColor": "#415a81", 
			"strokeWidth": 1, 
			"fillColor":"#b4c3dc",
			"externalGraphic":"res/escalator.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		},
		"escalator|direction:both": {
			"strokeColor": "#415a81", 
			"strokeWidth": 1, 
			"fillColor":"#b4c3dc",
			"externalGraphic":"res/escalator.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		},
		"escalator|direction:up": {
			"strokeColor": "#415a81", 
			"strokeWidth": 1, 
			"fillColor":"#b4c3dc",
			"externalGraphic":"res/escalator_up.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		},
		"escalator|direction:down": {
			"strokeColor": "#415a81", 
			"strokeWidth": 1, 
			"fillColor":"#b4c3dc",
			"externalGraphic":"res/escalator_down.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		},
		"escalator|direction:arriving": {
			"strokeColor": "#415a81", 
			"strokeWidth": 1, 
			"fillColor":"#b4c3dc",
			"externalGraphic":"res/escalator.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		},
		"elevator":{
			"strokeColor": "#415a81", 
			"strokeWidth": 1, 
			"fillColor":"#b4c3dc",
			"externalGraphic":"res/elevator.png",
			"graphicHeight":25,
			"graphicWidth":25,
			"fillOpacity":.3
		}		
	});
	return styles;
};
