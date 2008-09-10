var canvas = null;
var myEventHdlr = new RenderEventHandler();
myEventHdlr.onEvent = function(event)
{
	runtime.removeEventHandler(myEventHdlr);
	canvas = event.canvas;
	camera = scene.cameras.getByName("##cam##");
	if (camera != null) {
		bb = scene.computeBoundingBox();
		camera.targetPosition.set(bb.center);
		canvas.setCamera(camera);
	}
	upperColor = new Color(1.0, 1.0, 1.0);
	lowerColor = new Color(1.0, 1.0, 0.71);
	canvas.background.setColor(upperColor, lowerColor);
}
runtime.addEventHandler(myEventHdlr);
runtime.setCurrentTool("Spin");

scene.lightScheme = "file";
scene.renderMode = "solid";
scene.gridMode = "solid";
scene.showAxes = false;
scene.showGrid = false;
scene.lightScaleFactor = 1.0;
scene.renderDoubleSided = true;
scene.smoothing = true;
scene.ambientIlluminationColor.set(0.2, 0.2, 0.2);
scene.update();