//author Benjamin Kutschan
//default point shader, no spheres draw, vertex
#version 330

uniform mat4 projection;
uniform mat4 modelview;

in vec4 vertex_coordinates;
in float vertex_relativeRadii;
in vec4 vertex_colors;
in vec4 sphere_coords;

uniform float pointRadius;

out vec4 camSpaceCoord;
out vec3 camSpaceNormal;
out vec4 color;

void main(void)
{
	color = vertex_colors;
	vec4 vertex = vertex_coordinates + pointRadius * vec4(sphere_coords.xyz, 0);
	
	camSpaceCoord = modelview * vertex;
	
	mat3 rotation = mat3(vec3(modelview[0][0], modelview[0][1], modelview[0][2]), vec3(modelview[1][0], modelview[1][1], modelview[1][2]), vec3(modelview[2][0], modelview[2][1], modelview[2][2]));
	camSpaceNormal = normalize(rotation*sphere_coords.xyz);
	gl_Position = projection * camSpaceCoord;
}