//author Benjamin Kutschan
//default polygon vertex shader
#version 330
flat out int instanceID;
in int vertex_id;
uniform sampler2D uniforms;
mat4 modelview;
int polygonShader_smoothShading;
int has_vertex_normals;
int has_face_normals;
int has_face_colors;
int has_vertex_colors;
int has_vertex_texturecoordinates;

uniform mat4 projection;

uniform mat4 textureMatrix;



//shadow map samplers later

//VERTEX ATTRIBUTES
in vec4 vertex_coordinates;


in vec3 vertex_normals;


in vec3 face_normals;


in vec4 face_colors;


in vec4 vertex_colors;


in vec2 vertex_texturecoordinates;

out vec2 texCoord;
out vec4 camSpaceCoord;
out vec3 camSpaceNormal;

out vec4 faceVertexColor;

//!!!!!!!!  if some variable is not initialized properly, don't forget to exclude it
//!!!!!!!!  from the automatic handling by JOGLGeometryInstance.updateAppearance()

void main(void){
instanceID = vertex_id;
modelview = mat4(texelFetch(uniforms, ivec2(0, instanceID), 0), texelFetch(uniforms, ivec2(1, instanceID), 0), texelFetch(uniforms, ivec2(2, instanceID), 0), texelFetch(uniforms, ivec2(3, instanceID), 0));
polygonShader_smoothShading = floatBitsToInt(texelFetch(uniforms, ivec2(4, instanceID), 0)[0]);
has_vertex_normals = floatBitsToInt(texelFetch(uniforms, ivec2(4, instanceID), 0)[1]);
has_face_normals = floatBitsToInt(texelFetch(uniforms, ivec2(4, instanceID), 0)[2]);
has_face_colors = floatBitsToInt(texelFetch(uniforms, ivec2(4, instanceID), 0)[3]);
has_vertex_colors = floatBitsToInt(texelFetch(uniforms, ivec2(5, instanceID), 0)[0]);
has_vertex_texturecoordinates = floatBitsToInt(texelFetch(uniforms, ivec2(5, instanceID), 0)[1]);

{
	if(has_vertex_colors == 1)
		faceVertexColor = vertex_colors;
	if(has_face_colors == 1 && !(has_vertex_colors == 1 && polygonShader_smoothShading == 1))
		faceVertexColor = face_colors;
	if(has_vertex_texturecoordinates==1)
		texCoord = (textureMatrix * vec4(vertex_texturecoordinates, 0, 1)).st;
	vec3 normals = vec3(0.57735, 0.57735, 0.57735);
	if(polygonShader_smoothShading==0 && has_face_normals==1)
		normals = face_normals;
	else if(polygonShader_smoothShading==1 && has_vertex_normals==1)
		normals = vertex_normals;
	else if(has_vertex_normals==1)
		normals = vertex_normals;
	else if(has_face_normals == 1)
		normals = face_normals;
	gl_Position = projection * modelview * vertex_coordinates;
	
	mat3 rotation = mat3(vec3(modelview[0][0], modelview[0][1], modelview[0][2]), vec3(modelview[1][0], modelview[1][1], modelview[1][2]), vec3(modelview[2][0], modelview[2][1], modelview[2][2]));
	camSpaceNormal = normalize(rotation*normals);
	camSpaceCoord = modelview*vertex_coordinates;
}
}
