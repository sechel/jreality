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
	if(slice==0){
		if(floor(texCoord.s*numSlices) == 0)
			glFragColor = texture(background, texCoord.st);
		else
			discard;
	}else{
		int i = slice - 1;
		float lookup = 1.0*i/numSlices + (texCoord.s - 1.0*slice/numSlices)*(numSlices/(numSlices-1.0));
		
		float displacement = 0.1*(1-texture(image, vec2(lookup, texCoord.t)).r);
		//glFragColor = texture(background, vec2(texCoord.s+displacement, texCoord.t));
		if(floor(texCoord.s*numSlices) == slice){
			glFragColor = texture(background, vec2(texCoord.s-1.0/numSlices-displacement, texCoord.t));
			//glFragColor = 0.5*vec4((slice+1)*1.0/numSlices, 0, 0, 1) + 0.5*texture(image, texCoord.st);
		}else
			glFragColor = texture(background, texCoord.st);
	}
}
