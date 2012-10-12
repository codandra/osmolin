
/**
 * Class: ModCanvasRenderer 
 * A renderer based on the 2D 'canvas' drawing element.
 * 
 * Inherits:
 *  - <OpenLayers.Renderer.Canvas>
 */
OpenLayers.Renderer.ModCanvas = OpenLayers.Class(OpenLayers.Renderer.Canvas, {
	
	/**
     * Constructor: OpenLayers.Renderer.Canvas
     *
     * Parameters:
     * containerID - {<String>}
     * options - {Object} Optional properties to be set on the renderer.
     */
    initialize: function(containerID, options) {
        OpenLayers.Renderer.Canvas.prototype.initialize.apply(this, containerID, options);
		
        this.iconGeom = new OpenLayers.Geometry.Point(0,0);
		this.iconMap = null;
    },
	
	/** 
     * Method: drawGeometry
     * Used when looping (in redraw) over the features; draws
     * the canvas. 
     *
     * Parameters:
     * geometry - {<OpenLayers.Geometry>} 
     * style - {Object} 
     */
    drawGeometry: function(geometry, style, featureId) {
		OpenLayers.Renderer.Canvas.prototype.drawGeometry.apply(this, geometry, style, featureId);
		
        switch (geometry.CLASS_NAME) {
            case "OpenLayers.Geometry.LineString":
            case "OpenLayers.Geometry.LinearRing":
            case "OpenLayers.Geometry.Polygon":
				if(style.extendedGraphic) {
                    iconMap.push([geometry.getCentroid(), style, featureId]);
                }
            default:
                break;
        }
    },

	/**
     * Method: redraw
     * The real 'meat' of the function: any time things have changed,
     *     redraw() can be called to loop over all the data and (you guessed
     *     it) redraw it.  Unlike Elements-based Renderers, we can't interact
     *     with things once they're drawn, to remove them, for example, so
     *     instead we have to just clear everything and draw from scratch.
     */
    redraw: function() {
		this.iconMap = [];
		OpenLayers.Renderer.Canvas.prototype.redraw.apply(this);
		
        if (!this.locked) {
            var item;
            var xy;
			for (var i=0, len=this.iconMap.length; i<len; ++i) {
                item = this.iconMap[i];
                xy = item[0];
				this.iconGeom.x = xy[0];
				this.iconGeom.y = xy[1];
                this.drawExternalGraphic(iconGeom,item[1],item[2]);
            }
			this.iconMap = null;
        }    
    },

	CLASS_NAME: "OpenLayers.Renderer.ModCanvas"
})
