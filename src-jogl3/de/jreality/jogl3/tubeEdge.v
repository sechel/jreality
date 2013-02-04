//author Benjamin Kutschan
//default line vertex shader
#version 330

uniform mat4 projection;
uniform mat4 modelview;

in vec4 vertex_coordinates;
in vec4 _vertex_coordinates;
in vec4 tube_coords;

uniform float tubeRadius;

out vec3 camSpaceNormal;

void main(void)
{
	//edge endpoints in camera space
	vec3 v1 = (modelview*vertex_coordinates).xyz;
	vec3 v2 = (modelview*_vertex_coordinates).xyz;
	vec3 newZ = normalize(v1-(v2-v1)*dot(v1, v2-v1)/dot(v2-v1, v2-v1));
	vec3 newY = normalize(cross(normalize(v2-v1), newZ));
	mat4 trafo = mat4(v2-v1, 0, newY, 0, newZ, 0, v1, 1);
	
	vec4 scaledTubeCoords = vec4(tube_coords.x, tube_coords.y*tubeRadius, tube_coords.z*tubeRadius, 1);
	
	gl_Position = projection * trafo * scaledTubeCoords;
	
	vec3 Normal = vec3(0, tube_coords.yz);
	mat3 rotation = mat3(vec3(trafo[0][0], trafo[0][1], trafo[0][2]), vec3(trafo[1][0], trafo[1][1], trafo[1][2]), vec3(trafo[2][0], trafo[2][1], trafo[2][2]));
	camSpaceNormal = normalize(rotation*Normal);

	//gl_Position = projection * modelview * vec4(((vertex_coordinates.xyz +_vertex_coordinates.xyz)/2.0+tube_coords.xyz), 1);
	//gl_Position = vertex_coordinates;
}