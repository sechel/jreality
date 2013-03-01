//author Benjamin Kutschan
//default polygon fragment shader
#version 330

out vec4 gl_FragColor;
//needed?
float shade = .5;

uniform vec4 diffuseColor;
vec4 diffuse;
uniform float diffuseCoefficient;

uniform vec4 ambientColor;
uniform float ambientCoefficient;

uniform vec4 specularColor;
uniform float specularCoefficient;
uniform float specularExponent;

uniform sampler2D image;
uniform int has_Tex;
uniform int _combineMode;

in vec2 texCoord;
in vec4 camSpaceCoord;
in vec3 camSpaceNormal;

//GLOBAL LIGHTS
uniform sampler2D sys_globalLights;
uniform int sys_numGlobalDirLights;
uniform int sys_numGlobalPointLights;
uniform int sys_numGlobalSpotLights;

//LOCAL LIGHTS
uniform sampler2D sys_localLights;
uniform int sys_numLocalDirLights;
uniform int sys_numLocalPointLights;
uniform int sys_numLocalSpotLights;

uniform int has_vertex_texturecoordinates;

vec3 lightInflux = vec3(0, 0, 0);

uniform int has_face_colors;
uniform int has_vertex_colors;
in vec4 faceVertexColor;

//cubemap
uniform float _reflectionMapAlpha;
uniform int has_reflectionMap;
uniform sampler2D front;
uniform sampler2D back;
uniform sampler2D left;
uniform sampler2D right;
uniform sampler2D up;
uniform sampler2D down;

uniform mat4 _inverseCamRotation;
uniform int reflectionMap;

//depth checking for transparency
uniform sampler2D _depth;
uniform int _width;
uniform int _height;
uniform float transparency;

float attenuation(vec3 att, float dist){
	return 1/(att.x+att.y*dist+att.z*dist*dist);
}

float spot(float exp, vec4 dir, vec4 relPos, float coneAngle){
	float angle = acos(dot(-normalize(relPos.xyz), dir.xyz));
	return pow(cos(angle), coneAngle);
}

void calculateLightInfluxGeneral(vec3 normal, int numDir, int numPoint, int numSpot, sampler2D lights){	
	
	//size of the light texture
	int lightTexSize = numDir*3+numPoint*3+numSpot*5;
	
	for(int i = 0; i < numDir; i++){
		vec4 dir = texture(lights, vec2((3*i+1+0.5)/lightTexSize, 0));
		
		vec4 col = texture(lights, vec2((3*i+0.5)/lightTexSize, 0));
		float intensity = texture(lights, vec2((3*i+2+0.5)/lightTexSize, 0)).r;
		vec4 ambient = ambientColor*col*intensity;
		lightInflux = lightInflux + ambientCoefficient * ambient.xyz;
		
		float dott = dot(normal, normalize(dir.xyz));
		if(dott > 0){
			vec4 diffuse2 = dott*diffuse*col*intensity;
			
			float spec = dot(normal, normalize(normalize(dir.xyz)-normalize(camSpaceCoord.xyz)));
			
			vec4 specular =specularColor*intensity*pow(spec, specularExponent);
			
			vec4 new = specularCoefficient*specular+diffuseCoefficient*diffuse2;
			lightInflux = lightInflux + new.xyz;
		}
	}
	for(int i = 0; i < numPoint; i++){
		//calculate distance between light and vertex, possible also in HYPERBOLIC geom
		vec4 pos = texture(lights, vec2((3*numDir+3*i+1+0.5)/lightTexSize, 0));
		vec4 col = texture(lights, vec2((3*numDir+3*i+0.5)/lightTexSize, 0));
		vec4 tmp = texture(lights, vec2((3*numDir+3*i+2+0.5)/lightTexSize, 0));
		float intensity = tmp.a;
		vec3 att = tmp.xyz;
		vec4 RelPos = pos - camSpaceCoord;
		float dist = length(RelPos);
		float atten = 1/(att.x+att.y*dist+att.z*dist*dist);
		
		vec4 ambient = ambientColor*col*intensity;
		lightInflux = lightInflux + atten*ambientCoefficient * ambient.xyz;
		
		float dott = dot(normal, normalize(RelPos.xyz));
		if(dott > 0){
			vec4 diffuse2 = dott*diffuse*col*intensity;
			
			float spec = dot(normal, normalize(normalize(RelPos.xyz)-normalize(camSpaceCoord.xyz)));
			
			vec4 specular =specularColor*intensity*pow(spec, specularExponent);
			
			
			vec4 new = specularCoefficient*specular+diffuseCoefficient*diffuse2;
			lightInflux = lightInflux + atten*new.xyz;
		}
	}
	for(int i = 0; i < numSpot; i++){
		vec4 pos = texture(lights, vec2((3*numDir+3*numPoint+5*i+2+0.5)/lightTexSize, 0));
		vec4 coneAngles = texture(lights, vec2((3*numDir+3*numPoint+5*i+3+0.5)/lightTexSize, 0));
		vec4 RelPos = pos - camSpaceCoord;
		vec4 dir = texture(lights, vec2((3*numDir+3*numPoint+5*i+1+0.5)/lightTexSize, 0));
		float angle = acos(dot(-normalize(RelPos.xyz), dir.xyz));
		if(angle < coneAngles.x){
			vec4 col = texture(lights, vec2((3*numDir+3*numPoint+5*i+0.5)/lightTexSize, 0));
			vec3 att = texture(lights, vec2((3*numDir+3*numPoint+5*i+4+0.5)/lightTexSize, 0)).xyz;
			float dist = length(RelPos);
			float atten = 1/(att.x+att.y*dist+att.z*dist*dist);
			float intensity = coneAngles.a;
			vec4 ambient = ambientColor*col*intensity;
			float factor = pow(cos(angle), coneAngles.z);
			lightInflux = lightInflux + factor*atten*ambientCoefficient * ambient.xyz;
			
			//light is on the right side of the face
			float dott = dot(normal, normalize(RelPos.xyz));
			if(dott > 0){
				vec4 diffuse2 = dott*diffuse*col*intensity;
				float spec = dot(normal, normalize(normalize(RelPos.xyz)-normalize(camSpaceCoord.xyz)));
				vec4 specular =specularColor*intensity*pow(spec, specularExponent);
			
				vec4 new = specularCoefficient*specular+diffuseCoefficient*diffuse2;
				lightInflux = lightInflux + factor*atten*new.xyz;
			}
		}
	}
}

void calculateGlobalLightInflux(vec3 normal){
	lightInflux = lightInflux + ambientColor.xyz*ambientCoefficient;
	calculateLightInfluxGeneral(normal, sys_numGlobalDirLights, sys_numGlobalPointLights, sys_numGlobalSpotLights, sys_globalLights);
}
void calculateLocalLightInflux(vec3 normal){
	calculateLightInfluxGeneral(normal, sys_numLocalDirLights, sys_numLocalPointLights, sys_numLocalSpotLights, sys_localLights);
}

void main(void)
{
	float S = gl_FragCoord.s/_width;
	float T = gl_FragCoord.t/_height;
	float d = texture(_depth, vec2(S,T)).x;
	if(abs(1-d - gl_FragCoord.z) > 0.00000001)
		discard;
	
	//calculateLightInflux();
	//TODO check for availability of texture, check for face colors, what is diffuseColor?
	//vec4 texCoord = textureMatrix * vec4(gl_PointCoord, 0, 1);
	vec4 texColor = texture( image, texCoord.st);
	
//	vec4 color2 = vec4(1, 1, 1, 1);
//	if(has_vertex_texturecoordinates==1 && has_Tex == 1){
//		color2 = texColor;
//	}
	//if(has_vertex_texturecoordinates == 1 && texColor.a==0)
	//	discard;
	
	diffuse = diffuseColor;
	if(has_face_colors == 1 || has_vertex_colors == 1)
		diffuse = faceVertexColor;
	
	lightInflux = vec3(0, 0, 0);
	vec3 normal = normalize(camSpaceNormal);
	if(gl_FrontFacing){
		calculateGlobalLightInflux(normal);
		calculateLocalLightInflux(normal);
	}else{
		calculateGlobalLightInflux(-normal);
		calculateLocalLightInflux(-normal);
	}
	gl_FragColor = vec4(lightInflux, diffuse.a);
	if(has_vertex_texturecoordinates==1 && has_Tex == 1){
		if(_combineMode == 0x2100)//GL_MODULATE
			gl_FragColor = texColor*vec4(lightInflux, diffuse.a);
		if(_combineMode == 0x1E01)//GL_REPLACE
			gl_FragColor = texColor;
		if(_combineMode == 0x8570)//GL_COMBINE
			gl_FragColor = texColor;
		if(_combineMode == 0x2101){//GL_DECAL
			gl_FragColor.a = diffuse.a;
			gl_FragColor.rgb = (1-texColor.a)*lightInflux + texColor.a*texColor.rgb;
		}
		if(_combineMode == 0x0BE2){//GL_BLEND
			gl_FragColor.a = diffuse.a*texColor.a;
			gl_FragColor.rgb = (vec3(1,1,1)-texColor.rgb)*lightInflux + texColor.rgb;
		}
		if(_combineMode == 0x0104){//GL_ADD
			gl_FragColor.a = diffuse.a*texColor.a;
			gl_FragColor.rgb = lightInflux + texColor.rgb;
		}
	}
	
	if(gl_FragColor.a == 0)
		discard;
	
	if(has_reflectionMap == 1){
		//do environment reflections
		vec3 A = -normalize(camSpaceCoord.xyz);
		vec3 C = -A + 2*dot(A,normal)*normal;
		
		//do this computation in jogl3.JOGLRenderState
		mat4 iRotation = inverse(_inverseCamRotation);
		mat3 rotation = mat3(vec3(iRotation[0][0], iRotation[0][1], iRotation[0][2]), vec3(iRotation[1][0], iRotation[1][1], iRotation[1][2]), vec3(iRotation[2][0], iRotation[2][1], iRotation[2][2]));
		C = rotation*C;
		
		float x = C.x;
    	float y = C.y;
    	float z = C.z;
    	//we are in the front texture
    	if(z>abs(x) && z>abs(y)){
    		float X = x/z;
    		float Y = -y/z;
    		gl_FragColor = (1-_reflectionMapAlpha)*gl_FragColor+_reflectionMapAlpha*texture( back, vec2(X/2+.5,Y/2+.5));
   		}else if(z < -abs(x) && z < -abs(y)){
    		float X = x/z;
    		float Y = y/z;
    		gl_FragColor = (1-_reflectionMapAlpha)*gl_FragColor+_reflectionMapAlpha*texture( front, vec2(X/2+.5,Y/2+.5));
    	}else if(abs(y)>abs(x)){
    		//floor
    		if(y<0){
    			float X = -x/y;
    			float Z = z/y;
    			gl_FragColor = (1-_reflectionMapAlpha)*gl_FragColor+_reflectionMapAlpha*texture( down, vec2(X/2+.5,Z/2+.5));
    		//top
    		}else{
    			float X = x/y;
    			float Z = -z/y;
    			gl_FragColor = (1-_reflectionMapAlpha)*gl_FragColor+_reflectionMapAlpha*texture( up, vec2(X/2+.5,-Z/2+.5));
    		}
    	//left or right texture
    	}else{
    		//right
    		if(x>0){
    			float Y = -y/x;
    			float Z = z/x;
    			gl_FragColor = (1-_reflectionMapAlpha)*gl_FragColor+_reflectionMapAlpha*texture( right, vec2(-Z/2+.5,Y/2+.5));
    		//left
    		}else if(x<0){
    			float Y = y/x;
    			float Z = -z/x;
    			gl_FragColor = (1-_reflectionMapAlpha)*gl_FragColor+_reflectionMapAlpha*texture( left, vec2(Z/2+.5,Y/2+.5));
    		}
    	}
	}
	gl_FragColor.a = transparency;
}
