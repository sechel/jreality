//author Benjamin Kutschan
//default line fragment shader
#version 330

out vec4 glFragColor;
uniform vec4 lineDiffuseColor;

in vec4 edgeColor;
in vec4 vertexColor;
uniform int has_vertex_colors;
uniform int vertexColors;
uniform int has_edge_colors;

void main(void)
{
	glFragColor = lineDiffuseColor;
	
	if(has_edge_colors == 1)
		glFragColor = edgeColor;
	if(has_vertex_colors == 1 && vertexColors == 1)
		glFragColor = vertexColor;
}