//------------------------
// include a ViewerVR scene, that feels like the original ViewerVR.
// That is to move with curser keys or aswd, jump with space
// and turn around with right click on the mouse
//------------------------
/*
var JUMP_HEIGHT = 20;
var speed = 0.2;
var goVector = new Vector3(1, 0, 0);
var jumpVector = new Vector3(0,JUMP_HEIGHT,0);
var jumpAnim = scene.animations.getByIndex(0);
*/
/*
keyEventHandler = new KeyEventHandler();
keyEventHandler.onEvent = function(event){
	canvas = event.canvas;
	camera = scene.cameras.getByName("##cam##");
	var camDir = camera.targetPosition.subtract(camera.position);
	camDir.scale(speed); 
	
	if(event.characterCode == 32){
		scene.activateAnimation(jumpAnim);
	}
	if(event.characterCode == 31 || event.characterCode == 119){
		//vrCamera.positionLocal.set(vrCamera.positionLocal.add(goVector));
	}
	if(event.characterCode == 30 || event.characterCode == 115){
		//vrCamera.positionLocal.set(vrCamera.position.subtract(goVector));
	}
}
runtime.addEventHandler(keyEventHandler);
*/

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
	lowerColor = new Color(1.0, 1.0, 1.0);
	canvas.background.setColor(upperColor, lowerColor);
}
runtime.addEventHandler(myEventHdlr);
runtime.setCurrentTool("Walk");

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