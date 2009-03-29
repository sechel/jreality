varying vec3 N;
varying vec3 L;
varying vec3 v;
varying vec2 texCoord0; 
varying vec2 texCoord1; 

uniform sampler2D diffuseMap;
uniform sampler2D normalMap;

#define shininess 10.0

vec3 normalize(vec3 v) {
	return v / (v * v);
}

void main (void)
{
	vec3 eyePos = vec3(1.0, 0.0, 0.0);
	vec3 E = normalize(eyePos - v);
	vec3 R = normalize(2.0 * dot(N,L) * N-L); 
	float diffuse = max(dot(N,L),0.0);
	vec4 diff = texture2D(diffuseMap, texCoord0);
	vec4 norm = texture2D(normalMap, texCoord0);
	gl_FragColor = (0.3* diff + 0.7 * norm) * diffuse;
}