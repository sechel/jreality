//author Benjamin Kutschan
//default point shader, no spheres draw, vertex
#version 330

uniform mat4 projection;
uniform mat4 modelview;
//uniform vec4 camPosition;

in vec4 vertex_coordinates;

uniform float pointRadius;
uniform float screenSize;
uniform float screenSizeInSceneOverScreenSize;

out float x;
out float y;
out float z;
out float w;
out float screenPortion;


void main(void)
{


	vec4 posInCamSpace = modelview * vertex_coordinates;
	gl_Position = projection * posInCamSpace;
	
	
	float dist = length(posInCamSpace.xyz);
	//float magn = screenSize/100.0;
	//float pointSize2 = - (screenSize/100.0)*pointSize/posInCamSpace.z;
	float pointSize2 = - 2*pointRadius/(posInCamSpace.z*screenSizeInSceneOverScreenSize);
	screenPortion = screenSizeInSceneOverScreenSize*pointSize2;
	gl_PointSize = pointSize2;
	z = posInCamSpace.z;
	w = posInCamSpace.w;
	x = posInCamSpace.x;
	y = posInCamSpace.y;
}