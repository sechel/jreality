//author Benjamin Kutschan
//default polygon fragment shader
#version 330

out vec4 gl_FragColor;
in vec4 color;
void main(void)
{
	gl_FragColor = color;
}
