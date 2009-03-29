varying vec3 T;
varying vec3 N;
varying vec3 B;
varying vec3 L;
varying vec3 v;
varying vec2 texCoord0; 
varying vec2 texCoord1; 

uniform sampler2D diffuseMap;
uniform sampler2D normalMap;

#define shininess 10.0

vec3 normalize(vec3 v) {
	return v / sqrt((v * v));
}

void main (void)
{
	vec3 eyePos = vec3(1.0, 1.0, 1.0);
	
	mat3 TBN = mat3(T, B, N);
	vec3 NBump = texture2D(normalMap, texCoord0).xyz;
	NBump = normalize(TBN * NBump);

	vec3 E = normalize(eyePos - v);
	vec3 R = normalize(2.0 * dot(NBump,L) * NBump-L); 
	float diffuse = dot(NBump, L);
	vec3 diff = vec3(0.5,0.5,0.5);//texture2D(diffuseMap, texCoord0).xyz;
	gl_FragColor = vec4(diff * diffuse, 1.0);
}

