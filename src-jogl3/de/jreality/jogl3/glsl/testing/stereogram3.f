//author Benjamin Kutschan
//default polygon fragment shader
#version 150

out vec4 glFragColor;
smooth in vec4 texCoord;

uniform sampler2D image;
uniform sampler2D background;
uniform sampler2D color;
uniform int slice;
uniform int numSlices;
uniform int seed;
//uniform sampler2D image0;
//uniform sampler2D image1;
//uniform sampler2D image2;
//uniform sampler2D image3;

void main(void)
{
	vec4 diff0 = texture(image, texCoord.st) - texture(image, texCoord.st+vec2(0.002, 0));
	vec4 diff1 = texture(image, texCoord.st) - texture(image, texCoord.st+vec2(-0.002, 0));
	vec4 diff2 = texture(image, texCoord.st) - texture(image, texCoord.st+vec2(0, 0.002));
	vec4 diff3 = texture(image, texCoord.st) - texture(image, texCoord.st+vec2(0, -0.002));
	
	float d0 = diff0.x;
	float d1 = diff1.x;
	float d2 = diff2.x;
	float d3 = diff3.x;
	
	//if(d0>.1) d0=0;
	//if(d1>.1) d1=0;
	//if(d2>.1) d2=0;
	//if(d3>.1) d3=0;
	
	float total = d0+d1+d2+d3;
	float t = 1-10*total;
	if(t<0.99)
		t=0;
	
	glFragColor = vec4(t, t, t, 1);// * texture(color, texCoord.st);
}
