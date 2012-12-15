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

uniform sampler2D localLights;
uniform int numLocalDirLights;
uniform int numLocalPointLights;
uniform int numLocalSpotLights;

uniform int has_vertex_texturecoordinates;

vec3 lightInflux = vec3(0, 0, 0);


void calculateLightInfluxGeneral(vec3 normal, int numDir, int numPoint, int numSpot, sampler2D lights){	
	//size of the light texture
	int lightTexSize = numDir*2+numPoint*3+numSpot*5;
	
	for(int i = 0; i < numDir; i++){
		vec4 dir = texture(lights, vec2((2*i+1+0.5)/lightTexSize, 0));
		float dott = dot(normal, dir.xyz);
		if(dott > 0){
			vec4 col = texture( lights, vec2((2*i+0.5)/lightTexSize, 0));
			vec4 new = dott*col;
			vec3 new2 = new.xyz;
			lightInflux = lightInflux + new2;
		}
	}
	for(int i = 0; i < numPoint; i++){
		//calculate distance between light and vertex, possible also in HYPERBOLIC geom
		vec4 pos = texture(lights, vec2((2*numDir+3*i+1+0.5)/lightTexSize, 0));
		vec4 RelPos = pos - camSpaceCoord;
		float dott = dot(normal, normalize(RelPos.xyz));
		if(dott > 0){
			vec4 col = texture(lights, vec2((2*numDir+3*i+0.5)/lightTexSize, 0));
			vec3 att = texture(lights, vec2((2*numDir+3*i+2+0.5)/lightTexSize, 0)).xyz;
		
			float dist = length(RelPos);
			float atten = 1/(att.x+att.y*dist+att.z*dist*dist);
			
			vec4 new = atten*dott*col;
			vec3 new2 = new.xyz;
			lightInflux = lightInflux + new2;
		}
	}
	//this causes quite some overhead, when we have many spot lights, even if they are not visible for this pixel
	//the check, whether the light is on the right side of the triangle might better be done after checking whether it is inside the cone
	for(int i = 0; i < numSpot; i++){
		vec4 pos = texture(lights, vec2((2*numDir+3*numPoint+5*i+2+0.5)/lightTexSize, 0));
		vec4 RelPos = pos - camSpaceCoord;
		float dott = dot(normal, normalize(RelPos.xyz));
		//light is on the right side of the face
		if(dott > 0){
			vec4 dir = texture(lights, vec2((2*numDir+3*numPoint+5*i+1+0.5)/lightTexSize, 0));
			vec4 coneAngles = texture(lights, vec2((2*numDir+3*numPoint+5*i+3+0.5)/lightTexSize, 0));
			float angle = acos(dot(-normalize(RelPos.xyz), dir.xyz));
			if(angle < coneAngles.x){
				vec4 col = texture(lights, vec2((2*numDir+3*numPoint+5*i+0.5)/lightTexSize, 0));
				float factor = pow(cos(angle), coneAngles.z);
				vec3 att = texture(lights, vec2((2*numDir+3*numPoint+5*i+4+0.5)/lightTexSize, 0)).xyz;
				float dist = length(RelPos);
				float atten = 1/(att.x+att.y*dist+att.z*dist*dist);
				vec4 new = atten*factor*dott*col;
				vec3 new2 = new.xyz;
				lightInflux = lightInflux + new2;
			}
		}
	}
}

void calculateGlobalLightInflux(vec3 normal){
	calculateLightInfluxGeneral(normal, numGlobalDirLights, numGlobalPointLights, numGlobalSpotLights, globalLights);
}
void calculateLocalLightInflux(vec3 normal){
	calculateLightInfluxGeneral(normal, numLocalDirLights, numLocalPointLights, numLocalSpotLights, localLights);
}

void main(void)
{
	//calculateLightInflux();
	//TODO check for availability of texture, check for face colors, what is diffuseColor?
	//vec4 texCoord = textureMatrix * vec4(gl_PointCoord, 0, 1);
	vec4 texColor = texture( image, texCoord.st);
	
	vec4 color2 = diffuseColor; //vec4(1, 0, 0, 1);
	if(has_vertex_texturecoordinates==1 && hasTex == 1)
		color2 = texColor;
	if(color2.a==0)
		discard;
	lightInflux = vec3(0, 0, 0);
	if(gl_FrontFacing){
		calculateGlobalLightInflux(camSpaceNormal);
		calculateLocalLightInflux(camSpaceNormal);
		gl_FragColor = vec4(color2.rgb * lightInflux, color2.a);
	}else{
		calculateGlobalLightInflux(-camSpaceNormal);
		calculateLocalLightInflux(-camSpaceNormal);
		gl_FragColor = vec4(color2.rgb * lightInflux, color2.a);
	}
	
}
