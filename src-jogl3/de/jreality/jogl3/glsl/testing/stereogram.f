//author Benjamin Kutschan
//default polygon fragment shader
#version 150

out vec4 glFragColor;
smooth in vec4 texCoord;

uniform sampler2D image;
uniform sampler2D background;
uniform int slice;
uniform int numSlices;
//uniform sampler2D image0;
//uniform sampler2D image1;
//uniform sampler2D image2;
//uniform sampler2D image3;

void main(void)
{
	float displacement = texture(image, texCoord.st).r;
	//glFragColor = texture(background, vec2(texCoord.s+displacement, texCoord.t));
	
	if(floor(texCoord.s*numSlices) == slice)
		glFragColor = vec4((slice+1)*1.0/numSlices, 0, 0, 1);
	else
		glFragColor = texture(background, texCoord.st);
}
