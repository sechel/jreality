//author Benjamin Kutschan
//default polygon vertex shader
#version 330

in vec4 vertex_coordinates;

void main(void)
{
	gl_Position = vertex_coordinates;
}
