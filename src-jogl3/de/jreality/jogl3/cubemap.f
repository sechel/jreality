//author Benjamin Kutschan
//default polygon cubemap shader
#version 330

out vec4 gl_FragColor;

uniform sampler2D front;
uniform sampler2D back;
uniform sampler2D left;
uniform sampler2D right;
uniform sampler2D up;
uniform sampler2D down;

in float X;

//int texNo = 2;
in vec3 texCoord;
void main(void)
{	
	gl_FragColor = vec4(1, 0, 1, 1);//lila
	
	if(X<.5 && X>-0.5){
		gl_FragColor = texture( front, texCoord.st);
	}
	if(X<1.1 && X>0.5){
		gl_FragColor = texture( back, texCoord.st);
	}
	if(X<2.5 && X>1.5){
		gl_FragColor = texture( left, texCoord.st);
	}
	if(X<3.5 && X>2.5){
		gl_FragColor = texture( right, texCoord.st);
	}
	if(X<4.5 && X > 3.5){
		gl_FragColor = texture( up, texCoord.st);
	}
	if(X<5.5 && X>4.5){
		gl_FragColor = texture( down, texCoord.st);
	}
}