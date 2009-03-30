varying vec3 T;
varying vec3 N;
varying vec3 L;
varying vec3 v;
varying vec2 texCoord0; 
varying vec3 tangent;

uniform sampler2D diffuseMap;
uniform sampler2D normalMap;
uniform samplerCube envMap;

vec3 normalize(vec3 v) {
	return v / sqrt(dot(v, v));
}

vec4 expand(vec4 v) {
	return (v - 0.5) * 2.0;
}

vec3 expand(vec3 v) {
	return (v - 0.5) * 2.0;
}

void main (void) {	
   	vec3 B = normalize(cross(T, N));
	mat3 TBN = mat3(T, B, N);
	vec3 NBump = expand(texture2D(normalMap, texCoord0).xyz);
	NBump = normalize(NBump);
	NBump = NBump * TBN;

	vec3 eyeRay = normalize(reflect(v, NBump));
	vec4 env = textureCube(envMap, eyeRay);
	vec4 diffuse = dot(NBump, L) * gl_FrontMaterial.diffuse * texture2D(diffuseMap, texCoord0);
	
	gl_FragColor = diffuse * 0.5;
}

