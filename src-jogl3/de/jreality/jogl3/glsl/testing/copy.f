//author Benjamin Kutschan
//default polygon fragment shader
#version 330

out vec4 gl_FragColor;
smooth in vec4 texCoord;

uniform sampler2D image;

void main(void)
{
	gl_FragColor = texture( image, texCoord.st);
}
