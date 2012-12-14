//author Benjamin Kutschan
//default polygon fragment shader
#version 330

out vec4 gl_FragColor;
//needed?
float shade = .5;
uniform vec4 diffuseColor;
uniform sampler2D image;
uniform int hasTex;

in vec2 texCoord;
in vec4 camSpaceCoord;
in vec3 camSpaceNormal;

//LIGHTS
uniform sampler2D globalLights;
uniform int numGlobalDirLights;
uniform int numGlobalPointLights;
uniform int numGlobalSpotLights;

uniform int has_vertex_texturecoordinates;

vec3 lightInflux = vec3(0, 0, 0);
vec3 lightInfluxBackFace = vec3(0, 0, 0);

void calc(void){	
	//size of the light texture
	int lightTexSize = numGlobalDirLights*2+numGlobalPointLights*3+numGlobalSpotLights*4;
	lightInflux = vec3(0, 0, 0);
	for(int i = 0; i < numGlobalDirLights; i++){
		vec4 dir = texture(globalLights, vec2((2*i+1+0.5)/lightTexSize, 0));
		float dott = dot(camSpaceNormal, dir.xyz);
		if(dott > 0){
			vec4 col = texture( globalLights, vec2((2*i+0.5)/lightTexSize, 0));
			vec4 new = dott*col;
			vec3 new2 = new.xyz;
			lightInflux = lightInflux + new2;
		}
	}
	for(int i = 0; i < numGlobalPointLights; i++){
		//calculate distance between light and vertex, possible also in HYPERBOLIC geom
		vec4 pos = texture(globalLights, vec2((2*numGlobalDirLights+3*i+1+0.5)/lightTexSize, 0));
		vec4 RelPos = pos - camSpaceCoord;
		float dott = dot(camSpaceNormal, normalize(RelPos.xyz));
		
		if(dott > 0){
			vec4 col = texture( globalLights, vec2((2*numGlobalDirLights+3*i+0.5)/lightTexSize, 0));
			vec3 att = texture(globalLights, vec2((2*numGlobalDirLights+3*i+2+0.5)/lightTexSize, 0)).xyz;
		
			float dist = length(RelPos);
			float atten = 1/(att.x+att.y*dist+att.z*dist*dist);
			
			vec4 new = atten*dott*col;
			vec3 new2 = new.xyz;
			lightInflux = lightInflux + new2;
		}
	}
	
	lightInfluxBackFace = vec3(0, 0, 0);
	for(int i = 0; i < numGlobalDirLights; i++){
		vec4 dir = texture(globalLights, vec2((2*i+1+0.5)/lightTexSize, 0));
		float dott = dot(-camSpaceNormal, dir.xyz);
		if(dott > 0){
			vec4 col = texture( globalLights, vec2((2*i+0.5)/lightTexSize, 0));
			vec4 new = dott*col;
			vec3 new2 = new.xyz;
			lightInfluxBackFace = lightInfluxBackFace + new2;
		}
	}
	for(int i = 0; i < numGlobalPointLights; i++){
		//calculate distance between light and vertex, possible also in HYPERBOLIC geom
		vec4 pos = texture(globalLights, vec2((2*numGlobalDirLights+3*i+1+0.5)/lightTexSize, 0));
		vec4 RelPos = pos - camSpaceCoord;
		float dott = dot(-camSpaceNormal, normalize(RelPos.xyz));
		
		if(dott > 0){
			vec4 col = texture( globalLights, vec2((2*numGlobalDirLights+3*i+0.5)/lightTexSize, 0));
			vec3 att = texture(globalLights, vec2((2*numGlobalDirLights+3*i+2+0.5)/lightTexSize, 0)).xyz;
		
			float dist = length(RelPos);
			float atten = 1/(att.x+att.y*dist+att.z*dist*dist);
			
			vec4 new = atten*dott*col;
			vec3 new2 = new.xyz;
			lightInfluxBackFace = lightInfluxBackFace + new2;
		}
	}
}


void main(void)
{
	calc();
	//TODO check for availability of texture, check for face colors, what is diffuseColor?
	//vec4 texCoord = textureMatrix * vec4(gl_PointCoord, 0, 1);
	vec4 texColor = texture( image, texCoord.st);
	
	vec4 color2 = diffuseColor; //vec4(1, 0, 0, 1);
	if(has_vertex_texturecoordinates==1 && hasTex == 1)
		color2 = texColor;
	if(gl_FrontFacing)
		gl_FragColor = vec4(color2.rgb * lightInflux, color2.a);
	else
		gl_FragColor = vec4(color2.rgb * lightInfluxBackFace, color2.a);
	
	if(color2.a==0)
		discard;
}
