//author Benjamin Kutschan
//default polygon vertex shader
#version 330

uniform mat4 projection;
uniform mat4 modelview;
uniform mat4 textureMatrix;

uniform vec4 camPosition;
uniform vec4 globalLightColor;

uniform int smoothShading = 1;

//change this to a 1D-Texture
uniform vec4[] pointLightColors;
uniform vec4[] pointLightPositions;
uniform float[] pointLightConeAngles;
//shadow map samplers later
uniform int numDirLights;
uniform vec4[4] directionalLightColors;
uniform vec3[4] directionalLightDirections;

//rename to reproduce the vertex attribute names
in vec4 vertex_coordinates;
in vec3 vertex_normals;
uniform int has_vertex_normals;
in vec3 face_normals;
uniform int has_face_normals;
in vec2 vertex_texturecoordinates;
uniform int has_vertex_texturecoordinates;

//out float shade;
out vec3 lightInflux;
out vec3 lightInfluxBackFace;
//vec3 lightDir = vec3(0, .7, -.7);

out vec2 texCoord;

void main(void)
{
	if(has_vertex_texturecoordinates==1)
		texCoord = (textureMatrix * vec4(vertex_texturecoordinates, 0, 1)).st;
	vec3 normals = vec3(0.57735, 0.57735, 0.57735);
	if(smoothShading==0 && has_face_normals==1)
		normals = face_normals;
	else if(smoothShading==1 && has_vertex_normals==1)
		normals = vertex_normals;
	else if(has_vertex_normals==1)
		normals = vertex_normals;
	else if(has_face_normals == 1)
		normals = face_normals;
	gl_Position = projection * modelview * vertex_coordinates;
	
	mat3 rotation = mat3(vec3(modelview[0][0], modelview[0][1], modelview[0][2]), vec3(modelview[1][0], modelview[1][1], modelview[1][2]), vec3(modelview[2][0], modelview[2][1], modelview[2][2]));
	
	//shade = 0;
	lightInflux = vec3(0, 0, 0);
	for(int i = 0; i < numDirLights; i++){
		vec4 new = dot(rotation*normals, directionalLightDirections[i])*directionalLightColors[i];
		vec3 new2 = new.xyz;
		lightInflux = lightInflux + new2;
		//shade += dot(rotation*normals, directionalLightDirections[i]);
	}
	normals = -normals;
	lightInfluxBackFace = vec3(0, 0, 0);
	for(int i = 0; i < numDirLights; i++){
		vec4 new = dot(rotation*normals, directionalLightDirections[i])*directionalLightColors[i];
		vec3 new2 = new.xyz;
		lightInfluxBackFace = lightInfluxBackFace + new2;
		//shade += dot(rotation*normals, directionalLightDirections[i]);
	}
}