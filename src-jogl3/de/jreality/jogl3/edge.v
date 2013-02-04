//author Benjamin Kutschan
//default line vertex shader
#version 330

uniform mat4 projection;
uniform mat4 modelview;

in vec4 vertex_coordinates;

void main(void)
{
	gl_Position = projection * modelview * vertex_coordinates;
}