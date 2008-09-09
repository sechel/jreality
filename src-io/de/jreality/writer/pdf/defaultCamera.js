var canvas = null;
var myEventHdlr = new RenderEventHandler();
myEventHdlr.onEvent = function(event)
{
	runtime.removeEventHandler(myEventHdlr);
	canvas = event.canvas;
	camera = scene.cameras.getByIndex(0);
	canvas.setCamera(camera);
}
runtime.addEventHandler(myEventHdlr);
runtime.setCurrentTool("Rotate");

/*
scene.lightScheme = "headlamp";
scene.renderMode = "solid";
scene.gridMode = "solid";
scene.showAxes = false;
scene.showGrid = false;
scene.lightScaleFactor = 1.0;
scene.renderDoubleSided = true;
scene.smoothing = true;
scene.ambientIlluminationColor.set(0.2, 0.2, 0.2);
*/
scene.update();