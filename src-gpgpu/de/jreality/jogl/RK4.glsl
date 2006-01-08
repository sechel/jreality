// runge-kutta 2-3 merge

uniform float h;

uniform samplerRect K1;
uniform samplerRect K2;
uniform samplerRect K3;
uniform samplerRect K4;
uniform samplerRect particles;

void main(void) {
  vec2 pos = gl_TexCoord[0].st;
  vec4 pt = vec4(textureRect(particles, pos).xyz, 0);
  vec4 k_1 = textureRect(K1, pos);
  vec4 k_2 = textureRect(K2, pos);
  vec4 k_3 = textureRect(K3, pos);
  vec4 k_4 = textureRect(K4, pos);
  gl_FragColor = pt + h/6.*(k_1 + 2.*(k_2 + k_3) + k_4);
}

