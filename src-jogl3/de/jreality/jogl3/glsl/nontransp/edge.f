//author Benjamin Kutschan
//default line fragment shader
#version 330

out vec4 gl_FragColor;
uniform vec4 diffuseColor;

void main(void)
{
	gl_FragColor = diffuseColor;
}