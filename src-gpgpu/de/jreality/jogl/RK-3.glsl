// RK step 2

uniform float h;

uniform samplerRect vorticity; // t+h/2
uniform samplerRect K2;
uniform samplerRect particles;

vec3 biotSavart(const vec3, const samplerRect);
vec3 biotSavartEdge(const vec3, const vec3, const vec3, const float, const vec3, const float);

void main(void) {
  vec2 pos = gl_TexCoord[0].st;
  vec3 pt = textureRect(particles, pos).xyz;
  vec3 k_2 = textureRect(K2, pos).xyz;
  vec3 bs = biotSavart(pt+h/2.*k_2, vorticity);
  gl_FragColor = vec4(bs, 1);
} // -> K3


