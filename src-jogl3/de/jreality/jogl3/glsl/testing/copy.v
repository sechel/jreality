//author Benjamin Kutschan
//default polygon vertex shader
#version 330

in vec4 vertex_coordinates;
in vec4 texture_coordinates;

smooth out vec4 texCoord;

void main(void)
{
	gl_Position = vertex_coordinates;
	texCoord = texture_coordinates;
}
