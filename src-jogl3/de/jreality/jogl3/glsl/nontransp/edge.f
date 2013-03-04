//author Benjamin Kutschan
//default line fragment shader
#version 330

out vec4 glFragColor;
uniform vec4 diffuseColor;

void main(void)
{
	glFragColor = diffuseColor;
}