//author Benjamin Kutschan
//default polygon vertex shader
#version 330
flat out float instanceI;
in float vertex_id;
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
instanceI = vertex_id;
int id = int(vertex_id);
modelview = mat4(texelFetch(uniforms, ivec2(0, id), 0), texelFetch(uniforms, ivec2(1, id), 0), texelFetch(uniforms, ivec2(2, id), 0), texelFetch(uniforms, ivec2(3, id), 0));

{
	
	gl_Position = projection * modelview * vertex_coordinates;
	
	//mat3 rotation = mat3(vec3(modelview[0][0], modelview[0][1], modelview[0][2]), vec3(modelview[1][0], modelview[1][1], modelview[1][2]), vec3(modelview[2][0], modelview[2][1], modelview[2][2]));
	//camSpaceNormal = normalize(rotation*normals);
	//camSpaceCoord = modelview*vertex_coordinates;
}
}
