//author Benjamin Kutschan
//default polygon fragment shader
#version 330

out vec4 gl_FragColor;
float shade = .5;
in vec3 lightInflux;
in vec3 lightInfluxBackFace;
uniform vec4 diffuseColor;
uniform sampler2D image;
uniform int hasTex;
in vec2 texCoord;

uniform int has_vertex_texturecoordinates;

void main(void)
{
	//vec4 texCoord = textureMatrix * vec4(gl_PointCoord, 0, 1);
	vec4 texColor = texture( image, texCoord.st);
	
	vec4 color2 = diffuseColor; //vec4(1, 0, 0, 1);
	if(has_vertex_texturecoordinates==1 && hasTex == 1)
		color2 = texColor;
	if(gl_FrontFacing)
		gl_FragColor = vec4(color2.rgb * lightInflux, color2.a);
	else
		gl_FragColor = vec4(color2.rgb * lightInfluxBackFace, color2.a);
	
	if(color2.a==0)
		discard;
}