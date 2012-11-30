//author Benjamin Kutschan
//default polygon vertex shader
#version 330

uniform mat4 projection;
uniform mat4 modelview;
uniform mat4 textureMatrix;

uniform vec4 camPosition;
//uniform vec4 globalLightColor;

uniform int smoothShading = 1;

//shadow map samplers later


uniform sampler2D globalLights;
uniform int numGlobalDirLights;
uniform int numGlobalPointLights;
uniform int numGlobalSpotLights;

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

//!!!!!!!!  if some variable is not initialized properly, don't forget to exclude it
//!!!!!!!!  from the automatic handling by JOGLGeometryInstance.updateAppearance()

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
	//size of the light texture
	int lightTexSize = numGlobalDirLights*2+numGlobalPointLights*3+numGlobalSpotLights*4;
	lightInflux = vec3(0, 0, 0);
	for(int i = 0; i < numGlobalDirLights; i++){
		vec4 dir = texture(globalLights, vec2((2*i+1+0.5)/lightTexSize, 0));
		float dott = dot(rotation*normals, dir.xyz);
		if(dott > 0){
			vec4 col = texture( globalLights, vec2((2*i+0.5)/lightTexSize, 0));
			vec4 dir = texture(globalLights, vec2((2*i+1+0.5)/lightTexSize, 0));
			//vec4 new = dot(rotation*normals, directionalLightDirections[i])*directionalLightColors[i];
			vec4 new = dott*col;
			vec3 new2 = new.xyz;
			lightInflux = lightInflux + new2;
		}
	}
	for(int i = 0; i < numGlobalPointLights; i++){
		//calculate distance between light and vertex, possible also in HYPERBOLIC geom
		vec4 pos = texture(globalLights, vec2((2*numGlobalDirLights+3*i+1+0.5)/lightTexSize, 0));
		vec4 RelPos = pos - modelview * vertex_coordinates;
		float dott = dot(rotation*normals, normalize(RelPos.xyz));
		
		if(dott > 0){
			vec4 col = texture( globalLights, vec2((2*numGlobalDirLights+3*i+0.5)/lightTexSize, 0));
			vec3 att = texture(globalLights, vec2((2*numGlobalDirLights+3*i+2+0.5)/lightTexSize, 0)).xyz;
		
			float dist = sqrt(vertex_coordinates.x-pos.x * vertex_coordinates.x-pos.x + vertex_coordinates.y-pos.y * vertex_coordinates.y-pos.y + vertex_coordinates.z-pos.z * vertex_coordinates.z-pos.z);
			float atten = 1/(att.x+att.y*dist+att.z*dist*dist);
			
			vec4 new = dott*col;
			vec3 new2 = new.xyz;
			lightInflux = lightInflux + new2;
		}
		//shade += dot(rotation*normals, directionalLightDirections[i]);
	}
	normals = -normals;
	lightInfluxBackFace = vec3(0, 0, 0);
	for(int i = 0; i < numGlobalDirLights; i++){
		vec4 dir = texture(globalLights, vec2((2*i+1+0.5)/lightTexSize, 0));
		float dott = dot(rotation*normals, dir.xyz);
		if(dott > 0){
			vec4 col = texture( globalLights, vec2((2*i+0.5)/lightTexSize, 0));
			vec4 dir = texture(globalLights, vec2((2*i+1+0.5)/lightTexSize, 0));
			//vec4 new = dot(rotation*normals, directionalLightDirections[i])*directionalLightColors[i];
			vec4 new = dott*col;
			vec3 new2 = new.xyz;
			lightInfluxBackFace = lightInfluxBackFace + new2;
		}
	}
	for(int i = 0; i < numGlobalPointLights; i++){
		//calculate distance between light and vertex, possible also in HYPERBOLIC geom
		vec4 pos = texture(globalLights, vec2((2*numGlobalDirLights+3*i+1+0.5)/lightTexSize, 0));
		vec4 RelPos = pos - modelview * vertex_coordinates;
		float dott = dot(rotation*normals, normalize(RelPos.xyz));
		
		if(dott > 0){
			vec4 col = texture( globalLights, vec2((2*numGlobalDirLights+3*i+0.5)/lightTexSize, 0));
			vec3 att = texture(globalLights, vec2((2*numGlobalDirLights+3*i+2+0.5)/lightTexSize, 0)).xyz;
		
			float dist = sqrt(vertex_coordinates.x-pos.x * vertex_coordinates.x-pos.x + vertex_coordinates.y-pos.y * vertex_coordinates.y-pos.y + vertex_coordinates.z-pos.z * vertex_coordinates.z-pos.z);
			float atten = 1/(att.x+att.y*dist+att.z*dist*dist);
			
			vec4 new = dott*col;
			vec3 new2 = new.xyz;
			lightInfluxBackFace = lightInfluxBackFace + new2;
		}
		//shade += dot(rotation*normals, directionalLightDirections[i]);
	}
}
