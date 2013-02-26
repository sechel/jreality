//author Benjamin Kutschan
//default polygon vertex shader
#version 330

in vec4 vertex_coordinates;
in vec4 vertex_colors;
out vec4 color;


void main(void)
{
	gl_Position = vertex_coordinates;
	color = vertex_colors;
}
