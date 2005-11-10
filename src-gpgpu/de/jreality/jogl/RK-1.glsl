// RK step 0

uniform samplerRect vorticity;
uniform samplerRect particles;

vec3 biotSavart(const vec3, const samplerRect);
vec3 biotSavartEdge(const vec3, const vec3, const vec3, const float, const vec3, const float);

void main(void) {
  vec3 pt = textureRect(particles, gl_TexCoord[0].st).xyz;
  vec3 bs = biotSavart(pt, vorticity);
  gl_FragColor = vec4(bs, 1);
} // -> K1


