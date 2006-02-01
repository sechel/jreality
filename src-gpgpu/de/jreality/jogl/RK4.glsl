// runge-kutta 2-3 merge

uniform float h;

uniform samplerRect values;

vec3 evaluateT0(const vec3 point);
vec3 evaluateT0_H2(const vec3 point);
vec3 evaluateT0_H(const vec3 point);

void main(void) {
  vec2 pos = gl_TexCoord[0].st;
  vec3 pt = textureRect(values, pos).xyz;
  vec3 k1 = h * evaluateT0(pt);
  vec3 k2 = h * evaluateT0_H2(pt+k1/2.);
  vec3 k3 = h * evaluateT0_H2(pt+k2/2.);
  vec3 k4 = h * evaluateT0_H(pt+k3);
  gl_FragColor = vec4(pt + (k_1 + 2.*(k_2 + k_3) + k_4)/6., 1);
}

