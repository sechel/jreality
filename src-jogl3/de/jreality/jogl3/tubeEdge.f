//author Benjamin Kutschan
//default line fragment shader
#version 330

out vec4 gl_FragColor;
uniform vec4 diffuseColor;

uniform vec4 specularColor;

in vec3 camSpaceNormal;

void main(void)
{
	gl_FragColor = specularColor*dot(camSpaceNormal, vec3(1, 1, 1));
}