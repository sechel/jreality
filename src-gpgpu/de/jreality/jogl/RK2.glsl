// runge-kutta 2-3 merge

uniform float h;

uniform samplerRect K2;
uniform samplerRect particles;

void main(void) {
  vec2 pos = gl_TexCoord[0].st;
  vec3 pt = textureRect(particles, pos).xyz;
  vec3 k_2 = textureRect(K2, pos).xyz;
  gl_FragColor = vec4(pt + h*k_2, 1);
}

