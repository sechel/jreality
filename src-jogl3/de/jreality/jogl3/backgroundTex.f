//author Benjamin Kutschan
//default polygon cubemap shader
#version 330

out vec4 gl_FragColor;
uniform sampler2D image;
in vec2 texCoord;
void main(void)
{	
	gl_FragColor = texture(image, texCoord);
}