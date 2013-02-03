//author Benjamin Kutschan
//default line vertex shader
#version 330

uniform mat4 projection;
uniform mat4 modelview;

in vec4 vertex_coordinates;
in vec4 _vertex_coordinates;
in vec4 tube_coords;

void main(void)
{
	//edge endpoints in camera space
	vec3 v1 = (modelview*vertex_coordinates).xyz;
	vec3 v2 = (modelview*_vertex_coordinates).xyz;
	
	//
	vec3 newZ = normalize(v1-(v2-v1)*dot(v1, v2-v1)/dot(v2-v1, v2-v1));
	vec3 newY = normalize(cross(normalize(v2-v1), newZ));
	mat4 trafo = mat4(v2-v1, 0, newY, 0, newZ, 0, v1, 1);
	
	vec4 scaledTubeCoords = vec4(tube_coords.x, tube_coords.y*0.05, tube_coords.z*0.05, 1);
	
	gl_Position = projection * trafo * scaledTubeCoords;
	//gl_Position = projection * modelview * vec4(((vertex_coordinates.xyz +_vertex_coordinates.xyz)/2.0+tube_coords.xyz), 1);
	//gl_Position = vertex_coordinates;
}