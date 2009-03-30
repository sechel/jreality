varying vec3 T;
varying vec3 N;
varying vec3 B;
varying vec3 L;  
varying vec3 v;   

varying vec2 texCoord0; 

uniform vec3 eyePos;

void main(void) {
   	vec3 lightPos = gl_LightSource[0].position.xyz;
   	v = vec3(gl_ModelViewMatrix * gl_Vertex);
   	
   	L = normalize(lightPos - v);
   	N = normalize(gl_NormalMatrix * gl_Normal);
   	T = normalize(gl_NormalMatrix * gl_MultiTexCoord1.xyz);
   	//T *= sign(gl_MultiTexCoord1.w);
   	
	texCoord0 = vec2(gl_TextureMatrix[0] * gl_MultiTexCoord0);
	texCoord0.y *= -1.0;
	
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
             