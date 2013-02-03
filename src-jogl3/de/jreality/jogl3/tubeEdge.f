//author Benjamin Kutschan
//default line fragment shader
#version 330

out vec4 gl_FragColor;
uniform vec4 diffuseColor;

void main(void)
{
	gl_FragColor = diffuseColor;
	//gl_FragColor = vec4(1, 0, 0, 1);
	//gl_FragDepth = gl_FragCoord.z*0.99999;
}