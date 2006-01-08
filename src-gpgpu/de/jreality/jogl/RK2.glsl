// runge-kutta 2-3 merge

uniform float h;

uniform samplerRect K2;
uniform samplerRect particles;

void main(void) {
  vec2 pos = gl_TexCoord[0].st;
  vec4 pt = vec4(textureRect(particles, pos).xyz, 0);
  vec4 k_2 = textureRect(K2, pos);
  gl_FragColor = pt + h*k_2;
}

